package com.inventra.app.data.repository

import com.inventra.app.data.local.dao.CategoryDao
import com.inventra.app.data.local.dao.ProductDao
import com.inventra.app.data.local.dao.SaleDao
import com.inventra.app.data.local.dao.StockPurchaseDao
import com.inventra.app.data.local.entity.CategoryEntity
import com.inventra.app.data.local.entity.ProductEntity
import com.inventra.app.data.local.entity.SaleEntity
import com.inventra.app.data.local.entity.StockPurchaseEntity
import com.inventra.app.domain.model.AnalyticsSummary
import com.inventra.app.domain.model.DailySalesTrend
import com.inventra.app.domain.model.InventoryItem
import com.inventra.app.domain.model.Product
import com.inventra.app.domain.model.ProductSalesSummary
import com.inventra.app.domain.model.Sale
import com.inventra.app.domain.model.StockPurchase
import com.inventra.app.domain.model.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventraRepository @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val stockPurchaseDao: StockPurchaseDao,
    private val saleDao: SaleDao
) {
    // ── Products ──────────────────────────────────────────────────────────────

    val allProducts: Flow<List<Product>> =
        productDao.getAllActiveProducts().map { list -> list.map { it.toDomain() } }

    val lowStockProducts: Flow<List<Product>> =
        productDao.getLowStockProducts().map { list -> list.map { it.toDomain() } }

    val categories: Flow<List<String>> = productDao.getAllCategories()

    fun searchProducts(query: String): Flow<List<Product>> =
        productDao.searchProducts(query).map { list -> list.map { it.toDomain() } }

    suspend fun getProductById(id: String): Product? =
        productDao.getProductById(id)?.toDomain()

    suspend fun saveProduct(product: ProductEntity) = productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    // ── Stock In (WAC update) ─────────────────────────────────────────────────
    //
    // Weighted Average Cost formula on restock:
    //   new_avg = (existing_qty × existing_avg + incoming_qty × incoming_cost)
    //             ─────────────────────────────────────────────────────────────
    //                         (existing_qty + incoming_qty)

    suspend fun addStockPurchase(purchase: StockPurchaseEntity) {
        val product = productDao.getProductById(purchase.productId) ?: return
        val existingValue = product.currentStockQty * product.currentAvgCost
        val newQty = product.currentStockQty + purchase.quantity
        val newAvgCost = if (newQty > 0)
            (existingValue + purchase.quantity * purchase.costPerUnit) / newQty
        else
            purchase.costPerUnit
        productDao.updateStockStats(purchase.productId, newQty, newAvgCost)
        stockPurchaseDao.insertPurchase(purchase)
    }

    fun computeNewAvgCost(currentQty: Double, currentAvg: Double, incomingQty: Double, incomingCost: Double): Double {
        val totalQty = currentQty + incomingQty
        return if (totalQty > 0) (currentQty * currentAvg + incomingQty * incomingCost) / totalQty
        else incomingCost
    }

    // ── Sales ─────────────────────────────────────────────────────────────────
    //
    // On sale: snapshot avg_cost from the product, compute profit, reduce stock.
    // The WAC does NOT change on a sale — only stock quantity changes.

    suspend fun addSale(
        productId: String,
        quantity: Double,
        sellingPricePerUnit: Double,
        date: Long,
        notes: String
    ): Result<Unit> {
        val product = productDao.getProductById(productId)
            ?: return Result.failure(IllegalArgumentException("Product not found"))
        if (product.currentStockQty < quantity)
            return Result.failure(IllegalArgumentException("Insufficient stock (${product.currentStockQty} available)"))

        val totalRevenue = quantity * sellingPricePerUnit
        val avgCost = product.currentAvgCost
        val totalCost = quantity * avgCost
        val profit = totalRevenue - totalCost

        saleDao.insertSale(
            SaleEntity(
                productId = productId,
                quantity = quantity,
                sellingPricePerUnit = sellingPricePerUnit,
                totalRevenue = totalRevenue,
                avgCostAtSale = avgCost,
                totalCost = totalCost,
                profit = profit,
                date = date,
                notes = notes
            )
        )
        productDao.updateStockStats(productId, product.currentStockQty - quantity, avgCost)
        return Result.success(Unit)
    }

    suspend fun deleteSale(sale: SaleEntity) {
        // Restore stock quantity when a sale is deleted; avg cost is unchanged
        val product = productDao.getProductById(sale.productId) ?: return
        productDao.updateStockStats(sale.productId, product.currentStockQty + sale.quantity, product.currentAvgCost)
        saleDao.deleteSale(sale)
    }

    fun getRecentSales(limit: Int = 15): Flow<List<Sale>> =
        saleDao.getRecentSales(limit).map { list ->
            val productIds = list.map { it.productId }.distinct()
            val products = productDao.getProductsByIds(productIds).associateBy { it.id }
            list.map { it.toDomain(products[it.productId]?.name ?: "—", products[it.productId]?.unit ?: "") }
        }

    fun getSalesBetweenFlow(startDate: Long, endDate: Long): Flow<List<Sale>> =
        saleDao.getSalesBetweenFlow(startDate, endDate).map { list ->
            val productIds = list.map { it.productId }.distinct()
            val products = productDao.getProductsByIds(productIds).associateBy { it.id }
            list.map { it.toDomain(products[it.productId]?.name ?: "—", products[it.productId]?.unit ?: "") }
        }

    // ── Analytics ─────────────────────────────────────────────────────────────

    suspend fun getAnalyticsSummary(startDate: Long, endDate: Long): AnalyticsSummary {
        val revenue = saleDao.getTotalRevenueBetween(startDate, endDate)
        val profit = saleDao.getTotalProfitBetween(startDate, endDate)
        val cost = saleDao.getTotalCostBetween(startDate, endDate)
        val units = saleDao.getTotalUnitsSoldBetween(startDate, endDate)
        return AnalyticsSummary(revenue, profit, cost, units)
    }

    suspend fun getProductSalesSummary(startDate: Long, endDate: Long): List<ProductSalesSummary> {
        val results = saleDao.getProductSalesSummary(startDate, endDate)
        val productIds = results.map { it.productId }
        val products = productDao.getProductsByIds(productIds).associateBy { it.id }
        return results.map { r ->
            ProductSalesSummary(
                productId = r.productId,
                productName = products[r.productId]?.name ?: "—",
                totalQty = r.totalQty,
                totalRevenue = r.totalRevenue,
                totalProfit = r.totalProfit
            )
        }
    }

    suspend fun getDailySalesTrend(startDate: Long, endDate: Long): List<DailySalesTrend> {
        val fmt = SimpleDateFormat("dd MMM", Locale.getDefault())
        return saleDao.getDailySalesTrend(startDate, endDate).map { r ->
            // r.day is "yyyy-MM-dd" from SQLite strftime
            val parts = r.day.split("-")
            val dateMs = if (parts.size == 3) {
                val cal = java.util.Calendar.getInstance()
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
                cal.timeInMillis
            } else System.currentTimeMillis()
            DailySalesTrend(
                label = fmt.format(Date(dateMs)),
                date = dateMs,
                totalRevenue = r.totalRevenue,
                totalProfit = r.totalProfit,
                totalQty = r.totalQty
            )
        }
    }

    // ── Inventory ─────────────────────────────────────────────────────────────

    fun getInventoryItems(): Flow<List<InventoryItem>> =
        productDao.getAllActiveProducts().map { list ->
            list.map { InventoryItem(it.toDomain()) }
        }

    // ── Categories ────────────────────────────────────────────────────────────

    suspend fun saveCategory(name: String) =
        categoryDao.insertCategory(CategoryEntity(name = name))
}
