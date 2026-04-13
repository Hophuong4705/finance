package com.example.cuoikyltdd.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cuoikyltdd.data.local.dao.TaxDataDao
import com.example.cuoikyltdd.data.local.dao.TransactionDao
import com.example.cuoikyltdd.data.local.entity.TaxEntity
import com.example.cuoikyltdd.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, TaxEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun taxDataDao(): TaxDataDao
}