package com.inventra.app.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventra.app.data.repository.InventraRepository
import com.inventra.app.domain.model.Product
import com.inventra.app.domain.model.Sale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddSaleResult { object Idle : AddSaleResult(); object Success : AddSaleResult(); data class Error(val msg: String) : AddSaleResult() }

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: InventraRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSales: StateFlow<List<Sale>> = repository.getRecentSales(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _result = MutableStateFlow<AddSaleResult>(AddSaleResult.Idle)
    val result: StateFlow<AddSaleResult> = _result.asStateFlow()

    fun computeProfitPreview(product: Product, qty: Double, sellingPrice: Double): Double =
        (sellingPrice - product.currentAvgCost) * qty

    fun addSale(productId: String, quantity: Double, sellingPrice: Double, date: Long, notes: String) {
        viewModelScope.launch {
            if (quantity <= 0) { _result.value = AddSaleResult.Error("Quantity must be > 0"); return@launch }
            if (sellingPrice < 0) { _result.value = AddSaleResult.Error("Selling price cannot be negative"); return@launch }
            val outcome = repository.addSale(productId, quantity, sellingPrice, date, notes)
            _result.value = outcome.fold(
                onSuccess = { AddSaleResult.Success },
                onFailure = { AddSaleResult.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun resetResult() { _result.value = AddSaleResult.Idle }
}
