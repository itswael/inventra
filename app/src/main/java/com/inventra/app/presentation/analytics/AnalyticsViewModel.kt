package com.inventra.app.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventra.app.data.repository.InventraRepository
import com.inventra.app.domain.model.AnalyticsPeriod
import com.inventra.app.domain.model.AnalyticsSummary
import com.inventra.app.domain.model.DailySalesTrend
import com.inventra.app.domain.model.ProductSalesSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AnalyticsUiState(
    val period: AnalyticsPeriod = AnalyticsPeriod.WEEK,
    val summary: AnalyticsSummary = AnalyticsSummary(0.0, 0.0, 0.0, 0.0),
    val productSummaries: List<ProductSalesSummary> = emptyList(),
    val trends: List<DailySalesTrend> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: InventraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init { loadData(AnalyticsPeriod.WEEK) }

    fun onPeriodChanged(period: AnalyticsPeriod) = loadData(period)

    private fun loadData(period: AnalyticsPeriod) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, period = period)
            val (start, end) = periodRange(period)
            val summary = repository.getAnalyticsSummary(start, end)
            val products = repository.getProductSalesSummary(start, end)
            val trends = repository.getDailySalesTrend(start, end)
            _uiState.value = AnalyticsUiState(period, summary, products, trends, false)
        }
    }

    private fun periodRange(period: AnalyticsPeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        when (period) {
            AnalyticsPeriod.TODAY -> { /* already today */ }
            AnalyticsPeriod.WEEK -> cal.add(Calendar.DAY_OF_YEAR, -6)
            AnalyticsPeriod.MONTH -> cal.add(Calendar.DAY_OF_YEAR, -29)
            AnalyticsPeriod.YEAR -> cal.add(Calendar.YEAR, -1)
        }
        return cal.timeInMillis to end
    }
}
