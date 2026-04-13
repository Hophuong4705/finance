package com.example.cuoikyltdd.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val date: Long,
    val source: String,
    val type: String,
    val note: String,
    val isSynced: Boolean = false
)