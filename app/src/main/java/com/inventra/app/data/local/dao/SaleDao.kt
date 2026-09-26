package com.inventra.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.inventra.app.data.local.entity.SaleEntity
import com.inventra.app.domain.model.DailyTrendQueryResult
import com.inventra.app.domain.model.ProductSalesQueryResult
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Insert
    suspend fun insertSale(sale: SaleEntity)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE productId = :productId ORDER BY date DESC")
    fun getSalesForProduct(productId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSalesBetweenFlow(startDate: Long, endDate: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getSalesBetween(startDate: Long, endDate: Long): List<SaleEntity>

    @Query("SELECT * FROM sales ORDER BY date DESC LIMIT :limit")
    fun getRecentSales(limit: Int = 10): Flow<List<SaleEntity>>

    // Aggregate queries for analytics
    @Query("SELECT COALESCE(SUM(totalRevenue), 0) FROM sales WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalRevenueBetween(startDate: Long, endDate: Long): Double

    @Query("SELECT COALESCE(SUM(profit), 0) FROM sales WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalProfitBetween(startDate: Long, endDate: Long): Double

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM sales WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalUnitsSoldBetween(startDate: Long, endDate: Long): Double

    @Query("SELECT COALESCE(SUM(totalCost), 0) FROM sales WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalCostBetween(startDate: Long, endDate: Long): Double

    // Per-product aggregates for the analytics "Products" tab
    @Query("""
        SELECT productId,
               SUM(quantity) AS totalQty,
               SUM(totalRevenue) AS totalRevenue,
               SUM(profit) AS totalProfit
        FROM sales
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY productId
        ORDER BY totalRevenue DESC
    """)
    suspend fun getProductSalesSummary(startDate: Long, endDate: Long): List<ProductSalesQueryResult>

    // Daily trend — groups by calendar day in UTC
    @Query("""
        SELECT strftime('%Y-%m-%d', date / 1000, 'unixepoch') AS day,
               SUM(totalRevenue) AS totalRevenue,
               SUM(profit) AS totalProfit,
               SUM(quantity) AS totalQty
        FROM sales
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY day
        ORDER BY day ASC
    """)
    suspend fun getDailySalesTrend(startDate: Long, endDate: Long): List<DailyTrendQueryResult>
}
