package com.inventra.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.inventra.app.data.local.entity.StockPurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockPurchaseDao {

    @Insert
    suspend fun insertPurchase(purchase: StockPurchaseEntity)

    @Query("SELECT * FROM stock_purchases WHERE productId = :productId ORDER BY date DESC")
    fun getPurchasesForProduct(productId: String): Flow<List<StockPurchaseEntity>>

    @Query("SELECT * FROM stock_purchases ORDER BY date DESC LIMIT :limit")
    fun getRecentPurchases(limit: Int = 20): Flow<List<StockPurchaseEntity>>

    @Query("SELECT * FROM stock_purchases WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getPurchasesBetween(startDate: Long, endDate: Long): List<StockPurchaseEntity>

    @Query("SELECT SUM(totalCost) FROM stock_purchases WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpentBetween(startDate: Long, endDate: Long): Double?
}
