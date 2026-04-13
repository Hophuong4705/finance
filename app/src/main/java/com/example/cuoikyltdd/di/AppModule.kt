// Hồ Sỹ Phương - 23CNTT3 - Final Project
package com.example.cuoikyltdd.di

import android.content.Context
import androidx.room.Room
import com.example.cuoikyltdd.data.local.AppDatabase
import com.example.cuoikyltdd.data.local.dao.TaxDataDao
import com.example.cuoikyltdd.data.local.dao.TransactionDao
import com.example.cuoikyltdd.data.remote.ApiService
import com.example.cuoikyltdd.data.repository.FinanceRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        // 🔥 ĐÃ FIX LỖI getInstance: Sử dụng Room.databaseBuilder chuẩn của Hilt
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "finance_database" // Tên file database được lưu trong máy
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao =
        db.transactionDao()

    @Provides
    fun provideTaxDao(db: AppDatabase): TaxDataDao =
        db.taxDataDao()

    @Provides
    @Singleton
    fun provideFinanceRepository(
        transactionDao: TransactionDao,
        taxDao:         TaxDataDao,
        apiService:     ApiService
    ): FinanceRepositoryImpl =
        FinanceRepositoryImpl(transactionDao, taxDao, apiService)
}