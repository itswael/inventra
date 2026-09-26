package com.inventra.app.di

import android.content.Context
import androidx.room.Room
import com.inventra.app.data.local.InventraDatabase
import com.inventra.app.data.local.dao.CategoryDao
import com.inventra.app.data.local.dao.ProductDao
import com.inventra.app.data.local.dao.SaleDao
import com.inventra.app.data.local.dao.StockPurchaseDao
import com.inventra.app.data.repository.InventraRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InventraDatabase =
        Room.databaseBuilder(context, InventraDatabase::class.java, InventraDatabase.DATABASE_NAME)
            .build()

    @Provides fun provideProductDao(db: InventraDatabase): ProductDao = db.productDao()
    @Provides fun provideCategoryDao(db: InventraDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideStockPurchaseDao(db: InventraDatabase): StockPurchaseDao = db.stockPurchaseDao()
    @Provides fun provideSaleDao(db: InventraDatabase): SaleDao = db.saleDao()

    @Provides
    @Singleton
    fun provideRepository(
        productDao: ProductDao,
        categoryDao: CategoryDao,
        stockPurchaseDao: StockPurchaseDao,
        saleDao: SaleDao
    ): InventraRepository = InventraRepository(productDao, categoryDao, stockPurchaseDao, saleDao)
}
