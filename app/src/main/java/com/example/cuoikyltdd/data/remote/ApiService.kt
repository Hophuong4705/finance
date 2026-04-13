// Hồ Sỹ Phương - 23CNTT3 - Final Project

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
    // ── Auth ────────────────────────────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ── Tỷ giá ──────────────────────────────────────────────────────────────
    @GET("rates")
    suspend fun getExchangeRates(): Response<ExchangeRateResponse>

    // ── Giao dịch (Transactions) ───────────────────────────────────────────
    @POST("transactions")
    suspend fun createTransaction(@Body request: TransactionDto): Response<SyncResponse>

    @DELETE("transactions/by-date/{date}")
    suspend fun deleteTransactionByDate(@Path("date") date: Long): Response<SyncResponse>

    @POST("transactions/sync")
    suspend fun syncTransactions(@Body transactions: List<TransactionDto>): Response<SyncResponse>

    // ── Thông báo (Notifications) ──────────────────────────────────────────
    @POST("notifications/sync")
    suspend fun syncNotifications(@Body notifications: List<NotificationDto>): Response<SyncResponse>

    @DELETE("notifications")
    suspend fun deleteAllNotifications(): Response<SyncResponse>

    // 🔥 ĐÃ FIX CHUẨN 100%: Xóa "api/" bị dư và đổi thành Response<SyncResponse>
    @PUT("auth/change-password")
    suspend fun changePassword(@Body body: Map<String, String>): Response<SyncResponse>
}