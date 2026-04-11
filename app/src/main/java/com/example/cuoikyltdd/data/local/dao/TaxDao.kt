package com.example.cuoikyltdd.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cuoikyltdd.data.local.entity.TaxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTaxData(taxEntity: TaxEntity)

    @Query("SELECT * FROM tax_data WHERE id = 1")
    fun getTaxData(): Flow<TaxEntity?>
}