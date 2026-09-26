package com.inventra.app.presentation.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventra.app.data.local.entity.StockPurchaseEntity
import com.inventra.app.data.repository.InventraRepository
import com.inventra.app.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StockInResult { object Idle : StockInResult(); object Success : StockInResult(); data class Error(val msg: String) : StockInResult() }

@HiltViewModel
class StockViewModel @Inject constructor(
    private val repository: InventraRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _result = MutableStateFlow<StockInResult>(StockInResult.Idle)
    val result: StateFlow<StockInResult> = _result.asStateFlow()

    fun previewNewAvgCost(product: Product, incomingQty: Double, incomingCost: Double): Double =
        repository.computeNewAvgCost(product.currentStockQty, product.currentAvgCost, incomingQty, incomingCost)

    fun addStock(
        productId: String,
        quantity: Double,
        costPerUnit: Double,
        date: Long,
        supplier: String,
        notes: String
    ) {
        viewModelScope.launch {
            if (quantity <= 0) { _result.value = StockInResult.Error("Quantity must be greater than 0"); return@launch }
            if (costPerUnit < 0) { _result.value = StockInResult.Error("Cost cannot be negative"); return@launch }
            repository.addStockPurchase(
                StockPurchaseEntity(
                    productId = productId, quantity = quantity, costPerUnit = costPerUnit,
                    totalCost = quantity * costPerUnit, date = date, supplier = supplier, notes = notes
                )
            )
            _result.value = StockInResult.Success
        }
    }

    fun resetResult() { _result.value = StockInResult.Idle }
}
