package com.example.cuoikyltdd.data.repository

import com.example.cuoikyltdd.data.local.dao.TaxDataDao
import com.example.cuoikyltdd.data.local.dao.TransactionDao
import com.example.cuoikyltdd.data.local.entity.TaxEntity
import com.example.cuoikyltdd.data.local.entity.TransactionEntity
import com.example.cuoikyltdd.data.remote.ApiService
import com.example.cuoikyltdd.data.remote.dto.ExchangeRateResponse
import com.example.cuoikyltdd.data.remote.dto.TransactionDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val taxDao: TaxDataDao,
    private val apiService: ApiService
) {

    // 1. Chuyển thành hàm để ReportViewModel gọi được .getAllTransactions()
    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    // 2. Chuyển thành hàm để khớp với logic lấy cấu hình thuế
    fun getTaxConfig(): Flow<TaxEntity?> {
        return taxDao.getTaxData()
    }

    // 3. Thêm giao dịch và đồng bộ
    suspend fun addTransaction(transaction: TransactionEntity) {
        // Đảm bảo dùng copy để reset trạng thái sync khi thêm mới
        transactionDao.insertTransaction(transaction.copy(isSynced = false))
        syncPendingTransactions()
    }

    // 4. Xóa giao dịch
    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    // 5. Cơ chế Offline-first: Đồng bộ lên Server
    suspend fun syncPendingTransactions() {
        try {
            val unsyncedList = transactionDao.getUnsyncedTransactions()
            if (unsyncedList.isNotEmpty()) {
                val dtoList = unsyncedList.map {
                    TransactionDto(
                        amount = it.amount,
                        date = it.date,
                        source = it.source,
                        type = it.type,
                        note = it.note
                    )
                }

                val response = apiService.syncTransactions(dtoList)
                if (response.isSuccessful && response.body()?.success == true) {
                    val syncedList = unsyncedList.map { it.copy(isSynced = true) }
                    transactionDao.updateTransactions(syncedList)
                }
            }
        } catch (e: Exception) {
            // Lỗi mạng: Dữ liệu vẫn lưu local với isSynced = false để lần sau sync tiếp
            e.printStackTrace()
        }
    }

    // 6. Lấy tỷ giá từ Server
    suspend fun fetchExchangeRates(): Result<ExchangeRateResponse> {
        return try {
            val response = apiService.getExchangeRates()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Lỗi API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}