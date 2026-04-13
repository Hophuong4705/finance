package com.example.cuoikyltdd.data.remote

import com.example.cuoikyltdd.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class NotificationDto(
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean
)

interface ApiService {
    // ── Auth (Xác thực) ──────────────────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @PUT("auth/change-password")
    suspend fun changePassword(@Body body: Map<String, String>): Response<SyncResponse>


    // ── Tỷ giá ──────────────────────────────────────────────────────────────
    @GET("rates")
    suspend fun getExchangeRates(): Response<ExchangeRateResponse>


    // ── Giao dịch (Transactions) ───────────────────────────────────────────

    // 🔥 ĐÃ THÊM: Dùng khi người dùng đăng nhập ở máy mới, cần kéo dữ liệu cũ từ MongoDB về Room
    @GET("transactions")
    suspend fun getAllTransactions(): Response<List<TransactionDto>>

    // Đẩy 1 giao dịch mới (Ít dùng nếu đã dùng cơ chế Sync list)
    @POST("transactions")
    suspend fun createTransaction(@Body request: TransactionDto): Response<SyncResponse>

    // 🔥 LƯU Ý BACKEND: Trên Node.js phải có route: router.delete('/by-date/:date', ...)
    // Tham số date ở đây là Long (milliseconds)
    @DELETE("transactions/by-date/{date}")
    suspend fun deleteTransactionByDate(@Path("date") date: Long): Response<SyncResponse>

    // Đẩy mảng các giao dịch bị kẹt (isSynced = false) lên server
    @POST("transactions/sync")
    suspend fun syncTransactions(@Body transactions: List<TransactionDto>): Response<SyncResponse>


    // ── Thông báo (Notifications) ──────────────────────────────────────────
    @POST("notifications/sync")
    suspend fun syncNotifications(@Body notifications: List<NotificationDto>): Response<SyncResponse>

    @DELETE("notifications")
    suspend fun deleteAllNotifications(): Response<SyncResponse>
}