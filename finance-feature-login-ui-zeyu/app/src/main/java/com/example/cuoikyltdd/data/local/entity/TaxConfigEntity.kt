package com.example.cuoikyltdd.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tax_data")
data class TaxEntity(
    @PrimaryKey val id: Int = 1,
    val personalDeduction: Double,
    val dependentDeduction: Double,
    val numberOfDependents: Int,
    val insuranceDeduction: Double
)