package com.inventra.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inventra.app.data.local.dao.CategoryDao
import com.inventra.app.data.local.dao.ProductDao
import com.inventra.app.data.local.dao.SaleDao
import com.inventra.app.data.local.dao.StockPurchaseDao
import com.inventra.app.data.local.entity.CategoryEntity
import com.inventra.app.data.local.entity.ProductEntity
import com.inventra.app.data.local.entity.SaleEntity
import com.inventra.app.data.local.entity.StockPurchaseEntity

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        StockPurchaseEntity::class,
        SaleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class InventraDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun stockPurchaseDao(): StockPurchaseDao
    abstract fun saleDao(): SaleDao

    companion object {
        const val DATABASE_NAME = "inventra.db"
    }
}
