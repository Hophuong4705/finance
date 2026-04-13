package com.example.cuoikyltdd.di

import android.content.Context
import androidx.room.Room
import com.example.cuoikyltdd.data.local.AppDatabase
import com.example.cuoikyltdd.data.local.dao.TaxDataDao
import com.example.cuoikyltdd.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "finance_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideTaxDao(db: AppDatabase): TaxDataDao = db.taxDataDao()
}