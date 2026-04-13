package com.example.cuoikyltdd.data.remote.dto

data class TransactionDto(
    val amount: Double,
    val date: Long,
    val source: String,
    val type: String,
    val note: String
)

data class SyncResponse(
    val success: Boolean,
    val message: String
)