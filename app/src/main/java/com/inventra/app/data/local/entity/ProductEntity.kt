package com.inventra.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String = "",
    val productCode: String = "",
    val unit: String = "piece",
    val minStockLevel: Double = 0.0,
    // Denormalized WAC fields — updated on every purchase/sale for O(1) lookup
    val currentStockQty: Double = 0.0,
    val currentAvgCost: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
