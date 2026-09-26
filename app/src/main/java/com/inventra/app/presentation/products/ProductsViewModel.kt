package com.inventra.app.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventra.app.data.local.entity.ProductEntity
import com.inventra.app.data.repository.InventraRepository
import com.inventra.app.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: InventraRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Product>> = _searchQuery
        .flatMapLatest { q ->
            if (q.isBlank()) repository.allProducts else repository.searchProducts(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    fun onSearchQueryChange(q: String) { _searchQuery.value = q }
    fun onCategorySelected(cat: String?) { _selectedCategory.value = cat }

    val filteredProducts: StateFlow<List<Product>>
        get() = products // Category filtering is done in the composable for simplicity

    fun saveProduct(
        id: String?,
        name: String,
        category: String,
        productCode: String,
        unit: String,
        minStockLevel: Double
    ) {
        viewModelScope.launch {
            if (id == null) {
                repository.saveProduct(
                    ProductEntity(name = name, category = category, productCode = productCode,
                        unit = unit, minStockLevel = minStockLevel)
                )
            } else {
                val existing = repository.getProductById(id) ?: return@launch
                repository.updateProduct(
                    ProductEntity(id = id, name = name, category = category, productCode = productCode,
                        unit = unit, minStockLevel = minStockLevel,
                        currentStockQty = existing.currentStockQty,
                        currentAvgCost = existing.currentAvgCost,
                        createdAt = existing.createdAt)
                )
            }
        }
    }

    suspend fun getProduct(id: String): Product? = repository.getProductById(id)
}
