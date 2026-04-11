package com.example.cuoikyltdd.data.remote

import com.example.cuoikyltdd.data.remote.dto.ExchangeRateResponse
import com.example.cuoikyltdd.data.remote.dto.SyncResponse
import com.example.cuoikyltdd.data.remote.dto.TransactionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/transactions/sync")
    suspend fun syncTransactions(@Body transactions: List<TransactionDto>): Response<SyncResponse>

    @GET("api/exchange-rates")
    suspend fun getExchangeRates(): Response<ExchangeRateResponse>
}