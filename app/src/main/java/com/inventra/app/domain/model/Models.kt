package com.inventra.app.domain.model

import com.inventra.app.data.local.entity.ProductEntity
import com.inventra.app.data.local.entity.SaleEntity
import com.inventra.app.data.local.entity.StockPurchaseEntity

// Domain models — thin wrappers with computed helpers

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val productCode: String,
    val unit: String,
    val minStockLevel: Double,
    val currentStockQty: Double,
    val currentAvgCost: Double,
    val isActive: Boolean,
    val createdAt: Long
) {
    val currentStockValue: Double get() = currentStockQty * currentAvgCost
    val isLowStock: Boolean get() = currentStockQty <= minStockLevel && minStockLevel > 0
    val isOutOfStock: Boolean get() = currentStockQty <= 0
}

data class StockPurchase(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Double,
    val costPerUnit: Double,
    val totalCost: Double,
    val date: Long,
    val supplier: String,
    val notes: String
)

data class Sale(
    val id: String,
    val productId: String,
    val productName: String,
    val productUnit: String,
    val quantity: Double,
    val sellingPricePerUnit: Double,
    val totalRevenue: Double,
    val avgCostAtSale: Double,
    val totalCost: Double,
    val profit: Double,
    val date: Long,
    val notes: String
) {
    val profitMarginPct: Double
        get() = if (totalRevenue > 0) (profit / totalRevenue) * 100 else 0.0
}

// Analytics aggregation models

data class AnalyticsSummary(
    val totalRevenue: Double,
    val totalProfit: Double,
    val totalCost: Double,
    val totalUnitsSold: Double,
    val profitMarginPct: Double = if (totalRevenue > 0) (totalProfit / totalRevenue) * 100 else 0.0
)

data class ProductSalesSummary(
    val productId: String,
    val productName: String,
    val totalQty: Double,
    val totalRevenue: Double,
    val totalProfit: Double,
    val profitMarginPct: Double = if (totalRevenue > 0) (totalProfit / totalRevenue) * 100 else 0.0
)

data class DailySalesTrend(
    val label: String,   // "Mon", "26 Sep", etc.
    val date: Long,
    val totalRevenue: Double,
    val totalProfit: Double,
    val totalQty: Double
)

data class InventoryItem(
    val product: Product,
    val stockValue: Double = product.currentStockValue,
    val stockStatus: StockStatus = when {
        product.isOutOfStock -> StockStatus.OUT
        product.isLowStock -> StockStatus.LOW
        else -> StockStatus.OK
    }
)

enum class StockStatus { OK, LOW, OUT }

enum class AnalyticsPeriod { TODAY, WEEK, MONTH, YEAR }

// Room query result data classes
data class ProductSalesQueryResult(
    val productId: String,
    val totalQty: Double,
    val totalRevenue: Double,
    val totalProfit: Double
)

data class DailyTrendQueryResult(
    val day: String,
    val totalRevenue: Double,
    val totalProfit: Double,
    val totalQty: Double
)

// Extension mappers
fun ProductEntity.toDomain() = Product(
    id = id, name = name, category = category, productCode = productCode,
    unit = unit, minStockLevel = minStockLevel, currentStockQty = currentStockQty,
    currentAvgCost = currentAvgCost, isActive = isActive, createdAt = createdAt
)

fun SaleEntity.toDomain(productName: String, productUnit: String) = Sale(
    id = id, productId = productId, productName = productName, productUnit = productUnit,
    quantity = quantity, sellingPricePerUnit = sellingPricePerUnit, totalRevenue = totalRevenue,
    avgCostAtSale = avgCostAtSale, totalCost = totalCost, profit = profit,
    date = date, notes = notes
)

fun StockPurchaseEntity.toDomain(productName: String) = StockPurchase(
    id = id, productId = productId, productName = productName, quantity = quantity,
    costPerUnit = costPerUnit, totalCost = totalCost, date = date,
    supplier = supplier, notes = notes
)
