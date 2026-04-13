package com.example.cuoikyltdd.domain.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val date: Long,
    val source: String,
    val type: String,
    val note: String
)