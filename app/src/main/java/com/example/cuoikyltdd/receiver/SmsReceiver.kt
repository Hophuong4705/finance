package com.example.cuoikyltdd.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.cuoikyltdd.AppGlobalState
import com.example.cuoikyltdd.data.local.dao.TransactionDao
import com.example.cuoikyltdd.data.local.entity.TransactionEntity
import com.example.cuoikyltdd.domain.usecase.ParseNotificationUseCase
import com.example.cuoikyltdd.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var transactionDao: TransactionDao

    // Sử dụng UseCase (Nếu có @Inject trong UseCase thì chuyển thành lateinit var)
    private val parseUseCase = ParseNotificationUseCase()

    private val financialKeywords = listOf(
        "GD:", "SD:", "ND:", "PS:",
        "SO DU", "BIEN DONG", "GIAO DICH",
        "THANH TOAN", "CHUYEN TIEN", "NHAN TIEN",
        "TK ", "VND", "VNĐ",
        "Số dư", "Giao dịch", "Chuyển tiền",
        "Thanh toán", "Nhận tiền", "Biến động",
        "+VND", "-VND", "+ VND", "- VND",
        "+5,000", "-5,000",
        "đ.", "d.", "dong"
    )

    private val bankSenders = listOf(
        "MB", "MBBANK", "MB BANK",
        "MOMO", "MO MO",
        "VCB", "VIETCOMBANK",
        "TCB", "TECHCOMBANK",
        "BIDV", "TPBANK", "VPBANK",
        "ACB", "AGRIBANK", "SACOMBANK",
        "TIMO", "VIETINBANK", "VIB",
        "MSB", "SEABANK", "HDBANK"
    )

    override fun onReceive(context: Context, intent: Intent) {
        // 🔥 ĐẢM BẢO QUAN TRỌNG: Chỉ xử lý khi đúng Action SMS
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender  = messages[0].originatingAddress ?: ""
        val body    = messages.joinToString("") { it.messageBody ?: "" }

        if (body.isBlank()) return

        // 🔥 KIỂM TRA ĐIỀU KIỆN LỌC (Làm nhẹ gánh hệ thống)
        val isBankSender = bankSenders.any { sender.uppercase().contains(it) }
        val isFinancialContent = financialKeywords.any { keyword ->
            body.contains(keyword, ignoreCase = true)
        }

        if (!isBankSender && !isFinancialContent) {
            Log.d("SMS_RECEIVER", "Bỏ qua SMS không liên quan tài chính từ: $sender")
            return
        }

        // 🔥 BẮT BUỘC: Giữ WakeLock để Android không giết tiến trình khi đang lưu DB
        val pendingResult = goAsync()

        // Tạo scope mới cho mỗi lần chạy để đảm bảo an toàn
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                // Log thử vào trong App
                launch(Dispatchers.Main) {
                    AppGlobalState.addNotification("📱 SMS từ $sender", body.take(120))
                }

                // Chạy thuật toán bóc tách dữ liệu
                val parsed = parseUseCase(sender, body)

                if (parsed == null) {
                    Log.e("SMS_RECEIVER", "Không bóc tách được số tiền từ SMS: $body")
                    launch(Dispatchers.Main) {
                        AppGlobalState.addNotification(
                            "⚠️ SMS tài chính — Chưa tách được số tiền",
                            "Từ: $sender\n${body.take(100)}"
                        )
                    }
                    return@launch
                }

                // 1. Lưu vào Room Database ngầm
                transactionDao.insertTransaction(
                    TransactionEntity(
                        amount   = parsed.amount,
                        date     = System.currentTimeMillis(),
                        source   = parsed.source,
                        type     = parsed.type,
                        note     = parsed.note,
                        isSynced = false // Đánh dấu false để sau này đẩy lên Mongoose
                    )
                )

                Log.d("SMS_RECEIVER", "✅ Đã lưu DB: ${parsed.amount} - ${parsed.source}")

                // 2. Kích hoạt thông báo nổi ra ngoài màn hình khóa
                NotificationHelper.pushTransaction(
                    context = context,
                    source  = parsed.source,
                    amount  = parsed.amount,
                    type    = parsed.type,
                    note    = parsed.note
                )

                // 3. Cập nhật thông báo trong AppGlobalState
                val sign = if (parsed.type == "INCOME") "+" else "-"
                val fmt  = DecimalFormat("#,###", DecimalFormatSymbols(Locale("vi", "VN"))).format(parsed.amount)

                launch(Dispatchers.Main) {
                    AppGlobalState.addNotification(
                        "✅ ${parsed.source} — ${sign}${fmt}đ",
                        parsed.note.ifBlank { "Biến động số dư" }
                    )
                }

            } catch (e: Exception) {
                Log.e("SMS_RECEIVER_ERR", "Lỗi xử lý SMS: ${e.message}")
                launch(Dispatchers.Main) {
                    AppGlobalState.addNotification("❌ Lỗi lưu giao dịch từ SMS", e.message ?: "Unknown error")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}