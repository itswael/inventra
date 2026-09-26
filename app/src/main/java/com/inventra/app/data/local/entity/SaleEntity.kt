package com.inventra.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class SaleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val quantity: Double,
    val sellingPricePerUnit: Double,
    val totalRevenue: Double,
    // Weighted average cost captured at time of sale — the key for accurate profit
    val avgCostAtSale: Double,
    val totalCost: Double,
    val profit: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
