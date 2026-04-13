// Hồ Sỹ Phương - 23CNTT3 - Final Project

package com.example.cuoikyltdd.ui.screens.dashboard

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuoikyltdd.AppGlobalState
import com.example.cuoikyltdd.data.local.entity.TransactionEntity
import com.example.cuoikyltdd.data.remote.ApiService
import com.example.cuoikyltdd.data.remote.NotificationDto
import com.example.cuoikyltdd.data.remote.dto.ExchangeRateResponse
import com.example.cuoikyltdd.data.repository.FinanceRepositoryImpl
import com.example.cuoikyltdd.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.example.cuoikyltdd.SharedPrefsHelper

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FinanceRepositoryImpl,
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val transactions = repository.getAllTransactions().stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _exchangeRate = mutableStateOf<ExchangeRateResponse?>(null)
    val exchangeRate: State<ExchangeRateResponse?> = _exchangeRate

    init {
        fetchRates()
        syncNow()
    }

    private fun fetchRates() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.fetchExchangeRates()
            if (result.isSuccess) {
                withContext(Dispatchers.Main) {
                    _exchangeRate.value = result.getOrNull()
                }
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("SYNC", "Bắt đầu tiến trình đồng bộ Mongoose...")
                repository.syncPendingTransactions()
                syncNotificationsToCloud()
                Log.d("SYNC", "Đồng bộ hoàn tất!")
            } catch (e: Exception) {
                // Bắt lỗi ngoại lệ nếu Server trả về lỗi 401 (Tài khoản bị xóa)
                if (e.message?.contains("401") == true || e.message?.contains("ACCOUNT_DELETED") == true) {
                    handleForceLogout()
                } else {
                    Log.e("SYNC_ERR", "Lỗi đồng bộ (Có thể do Offline): ${e.message}")
                }
            }
        }
    }

    private fun syncNotificationsToCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dtos = AppGlobalState.notifications.map { notif ->
                    NotificationDto(
                        title   = notif.title,
                        message = notif.message,
                        time    = notif.time,
                        isRead  = notif.isRead
                    )
                }

                val response = apiService.syncNotifications(dtos)
                if (response.isSuccessful) {
                    Log.d("SYNC_NOTIF", "🔥 Đã đẩy thành công thông báo lên Mongoose!")
                } else if (response.code() == 401) {
                    // 🔥 Bắt trực tiếp mã 401 từ Response
                    handleForceLogout()
                }
            } catch (e: Exception) {
                if (e.message?.contains("401") == true) {
                    handleForceLogout()
                } else {
                    Log.e("SYNC_NOTIF_ERR", "Đang Offline, chưa gửi thông báo được: ${e.message}")
                }
            }
        }
    }

    fun addTransaction(amount: Double, note: String, type: String, source: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entity = TransactionEntity(
                    amount   = amount,
                    date     = System.currentTimeMillis(),
                    source   = source,
                    type     = type,
                    note     = note,
                    isSynced = false
                )

                repository.addTransaction(entity)
                Log.d("DB_SUCCESS", "Đã lưu giao dịch vào máy!")

                withContext(Dispatchers.Main) {
                    NotificationHelper.pushTransaction(
                        context = context,
                        source  = source,
                        amount  = amount,
                        type    = type,
                        note    = note.ifBlank { source }
                    )

                    syncNow()
                }
            } catch (e: Exception) {
                Log.e("DB_CRASH", "Lỗi khi lưu vào máy: ${e.message}")
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteTransaction(transaction)

                withContext(Dispatchers.Main) {
                    val amountFmt = java.text.DecimalFormat("#,###").format(transaction.amount)
                    NotificationHelper.pushSimple(
                        context = context,
                        title   = "🗑️ Đã xóa giao dịch",
                        message = "${transaction.source} — ${amountFmt}đ"
                    )

                    syncNotificationsToCloud()
                }
            } catch (e: Exception) {
                Log.e("DB_CRASH", "Lỗi khi xóa khỏi máy: ${e.message}")
            }
        }
    }

    // ==================================================================================
    // 🔥 CƯỠNG CHẾ ĐĂNG XUẤT VÀ TỰ HỦY DỮ LIỆU KHI PHÁT HIỆN TÀI KHOẢN MA
    // ==================================================================================
    private suspend fun handleForceLogout() {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "🚨 Tài khoản không tồn tại! Đang tự động hủy dữ liệu ảo...", Toast.LENGTH_LONG).show()

            // 1. Xóa phiên đăng nhập (Văng ra màn hình Login nhờ AppGlobalState)
            SharedPrefsHelper.logout()
            AppGlobalState.isLoggedIn = false
            AppGlobalState.clearNotifications()

            // 2. Xóa sạch dữ liệu cục bộ trong máy
            try {
                repository.clearAllLocalData() // Gọi hàm dọn rác trong Repository
            } catch (e: Exception) {
                Log.e("CLEANUP", "Lỗi khi xóa Room DB: ${e.message}")
            }
        }
    }

    // ==================================================================================
    // 🔥 HÀM ĐỔI MẬT KHẨU (GỌI TỪ ChangePasswordScreen)
    // ==================================================================================
    suspend fun changePassword(oldPass: String, newPass: String): Boolean {
        return try {
            val requestBody = mapOf("oldPassword" to oldPass, "newPassword" to newPass)
            val response = apiService.changePassword(requestBody)

            // Chỉ cần check isSuccessful là đủ 100% an toàn (Vì sai pass Server sẽ trả về lỗi 400)
            response.isSuccessful

        } catch (e: Exception) {
            Log.e("CHANGE_PASS_ERR", "Lỗi khi đổi mật khẩu: ${e.message}")
            false
        }
    }
}