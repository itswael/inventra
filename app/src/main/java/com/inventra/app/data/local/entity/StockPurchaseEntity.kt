package com.inventra.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "stock_purchases",
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
data class StockPurchaseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val quantity: Double,
    val costPerUnit: Double,
    val totalCost: Double,
    val date: Long = System.currentTimeMillis(),
    val supplier: String = "",
    val notes: String = ""
)
