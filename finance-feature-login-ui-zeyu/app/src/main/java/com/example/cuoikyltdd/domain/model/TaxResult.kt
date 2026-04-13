package com.example.cuoikyltdd.domain.model

data class TaxResult(
    val totalIncome: Double,
    val totalDeduction: Double,
    val taxableIncome: Double,
    val taxPayable: Double
)