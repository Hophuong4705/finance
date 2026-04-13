package com.example.cuoikyltdd.service // Đảm bảo dòng này khớp với thư mục

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.cuoikyltdd.data.local.entity.TransactionEntity
import com.example.cuoikyltdd.data.repository.FinanceRepositoryImpl
import com.example.cuoikyltdd.domain.usecase.ParseNotificationUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationMonitorService : NotificationListenerService() {

    @Inject
    lateinit var parseUseCase: ParseNotificationUseCase

    @Inject
    lateinit var repository: FinanceRepositoryImpl

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val extras = it.notification.extras
            val title = extras.getString("android.title") ?: ""
            // Dùng getCharSequence thay vì getString để tránh lỗi "String!"
            val text = extras.getCharSequence("android.text")?.toString() ?: ""

            val parsedTransaction = parseUseCase(title, text)

            if (parsedTransaction != null) {
                scope.launch {
                    val entity = TransactionEntity(
                        amount = parsedTransaction.amount,
                        date = System.currentTimeMillis(), // SỬA: Dùng Long thay vì String
                        source = parsedTransaction.source,
                        type = parsedTransaction.type,
                        note = parsedTransaction.note,
                        isSynced = false
                    )
                    repository.addTransaction(entity)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}