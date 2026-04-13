
package com.example.cuoikyltdd.data.repository

import android.util.Log
import com.example.cuoikyltdd.data.local.dao.TaxDataDao
import com.example.cuoikyltdd.data.local.dao.TransactionDao
import com.example.cuoikyltdd.data.local.entity.TransactionEntity
import com.example.cuoikyltdd.data.remote.ApiService
import com.example.cuoikyltdd.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    @Suppress("unused") private val taxDao: TaxDataDao,
    private val apiService:     ApiService
) {
    // ── Local: Lấy dữ liệu từ Room (Dùng cho giao diện) ─────────────────────
    fun getAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    suspend fun addTransaction(entity: TransactionEntity) =
        transactionDao.insertTransaction(entity)

    // ── Xử lý Xóa: Xóa máy xong gọi API xóa Mongoose (An toàn Offline) ──────
    suspend fun deleteTransaction(entity: TransactionEntity) {
        // Luôn luôn xóa ở Local trước để người dùng thấy kết quả ngay
        transactionDao.deleteTransaction(entity)
        Log.d("REPO_DEL", "🗑️ Đã xóa Local Offline")

        try {
            val response = apiService.deleteTransactionByDate(entity.date)

            // 🔥 KIỂM TRA TÀI KHOẢN MA: Nếu xóa trên Cloud báo 401, ném lỗi để logout
            if (response.code() == 401) {
                throw Exception("401 ACCOUNT_DELETED")
            }

            if (response.isSuccessful) {
                Log.d("REPO_DEL", "✅ Đã xóa đồng bộ trên MongoDB Cloud")
            }
        } catch (e: Exception) {
            // Nếu là lỗi 401 thì quăng ra ngoài cho ViewModel xử lý
            if (e.message?.contains("401") == true) throw e
            Log.e("REPO_DEL_ERR", "⚠️ Mất mạng, chỉ xóa ở Local. Chi tiết: ${e.message}")
        }
    }

    // ── Remote: Lấy tỷ giá (An toàn Offline) ────────────────────────────────
    suspend fun fetchExchangeRates(): Result<ExchangeRateResponse> {
        return try {
            val response = apiService.getExchangeRates()

            if (response.code() == 401) throw Exception("401 ACCOUNT_DELETED")

            if (response.isSuccessful && response.body() != null)
                Result.success(response.body()!!)
            else
                Result.success(ExchangeRateResponse(25.480, 80.500))
        } catch (e: Exception) {
            if (e.message?.contains("401") == true) throw e
            Log.e("REPO_RATE_ERR", "Lỗi lấy tỷ giá: ${e.message}")
            Result.success(ExchangeRateResponse(25.480, 80.500))
        }
    }

    // ── Remote: ĐỒNG BỘ DỮ LIỆU TỪ MÁY LÊN CLOUD ────────────────────────────
    suspend fun syncPendingTransactions() {
        try {
            val unsynced = transactionDao.getUnsyncedTransactions()
            if (unsynced.isEmpty()) return

            val dtos = unsynced.map { tx ->
                TransactionDto(
                    amount = tx.amount,
                    date   = tx.date,
                    source = tx.source,
                    type   = tx.type,
                    note   = tx.note
                )
            }

            val response = apiService.syncTransactions(dtos)

            // 🔥 XỬ LÝ GHOST USER: Nếu Server báo 401 (Tài khoản đã bị xóa trên Cloud)
            if (response.code() == 401) {
                throw Exception("401 ACCOUNT_DELETED")
            }

            val result = response.body()
            if (response.isSuccessful && result != null) {
                if (result.success) {
                    Log.d("SYNC_OK", "✅ Đã đẩy dữ liệu kẹt lên MongoDB!")
                    val synced = unsynced.map { it.copy(isSynced = true) }
                    transactionDao.updateTransactions(synced)
                }
            }
        } catch (e: Exception) {

            if (e.message?.contains("401") == true) throw e
            Log.e("SYNC_ERR", "⚠️ Lỗi đồng bộ: ${e.message}")
        }
    }

    // ── XỬ LÝ GHOST USER: Dọn sạch Room DB khi tài khoản bị xóa trên Cloud ──
    // Hàm này sẽ được AuthViewModel gọi khi phát hiện mã lỗi 401
    suspend fun clearAllLocalData() {
        try {
            transactionDao.deleteAllTransactions()
            Log.d("REPO_CLEANUP", "🧹 Đã xóa sạch dữ liệu ảo trong Local (Room DB).")
        } catch (e: Exception) {
            Log.e("REPO_CLEANUP_ERR", "⚠️ Lỗi khi xóa dữ liệu Local: ${e.message}")
        }
    }
}