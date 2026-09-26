package com.inventra.app.presentation.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventra.app.domain.model.AnalyticsPeriod
import com.inventra.app.domain.model.AnalyticsSummary
import com.inventra.app.domain.model.ProductSalesSummary
import com.inventra.app.ui.components.EmptyState
import com.inventra.app.ui.components.HorizontalBar
import com.inventra.app.ui.components.PeriodChip
import com.inventra.app.ui.components.StatCard
import com.inventra.app.ui.components.TrendBarChart
import com.inventra.app.ui.components.formatCurrency
import com.inventra.app.ui.components.formatQty
import com.inventra.app.ui.theme.LossRedLight
import com.inventra.app.ui.theme.ProfitGreenLight

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    Scaffold(contentWindowInsets = WindowInsets(0)) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                "Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            // Period selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnalyticsPeriod.entries.forEach { period ->
                    PeriodChip(
                        label = period.label,
                        selected = state.period == period,
                        onClick = { viewModel.onPeriodChanged(period) }
                    )
                }
            }

            // Tab row
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                listOf("Overview", "Products", "Trends").forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title) })
                }
            }

            if (state.isLoading) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> OverviewTab(state.summary)
                    1 -> ProductsTab(state.productSummaries)
                    2 -> TrendsTab(state)
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(summary: AnalyticsSummary) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Revenue", formatCurrency(summary.totalRevenue), Modifier.weight(1f))
                StatCard(
                    "Profit", formatCurrency(summary.totalProfit), Modifier.weight(1f),
                    valueColor = if (summary.totalProfit >= 0) ProfitGreenLight else LossRedLight
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Units Sold", formatQty(summary.totalUnitsSold), Modifier.weight(1f))
                StatCard(
                    "Margin", String.format("%.1f%%", summary.profitMarginPct), Modifier.weight(1f),
                    valueColor = if (summary.profitMarginPct >= 0) ProfitGreenLight else LossRedLight
                )
            }
        }
        item {
            StatCard(
                label = "Total COGS",
                value = formatCurrency(summary.totalCost),
                modifier = Modifier.fillMaxWidth(),
                subtitle = "Cost of Goods Sold (weighted avg cost at time of each sale)"
            )
        }
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (summary.totalProfit >= 0)
                        ProfitGreenLight.copy(alpha = 0.12f)
                    else LossRedLight.copy(alpha = 0.12f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        if (summary.totalProfit >= 0) "✅ Profitable Period" else "⚠️ Loss-Making Period",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        color = if (summary.totalProfit >= 0) ProfitGreenLight else LossRedLight
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (summary.totalRevenue > 0)
                            "You made ${formatCurrency(summary.totalProfit)} on ${formatCurrency(summary.totalRevenue)} in revenue (${String.format("%.1f%%", summary.profitMarginPct)} margin)"
                        else "No sales recorded in this period.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductsTab(products: List<ProductSalesSummary>) {
    if (products.isEmpty()) {
        EmptyState("📊", "No product data", "Add sales to see product performance",
            Modifier.fillMaxSize())
        return
    }
    val maxRevenue = products.maxOf { it.totalRevenue }.coerceAtLeast(1.0)
    val maxProfit = products.maxOf { it.totalProfit }.coerceAtLeast(1.0)

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("By Revenue", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
        }
        items(products.sortedByDescending { it.totalRevenue }) { p ->
            ProductRankCard(
                name = p.productName,
                primary = formatCurrency(p.totalRevenue),
                secondary = "${formatQty(p.totalQty)} sold",
                fraction = (p.totalRevenue / maxRevenue).toFloat(),
                barColor = MaterialTheme.colorScheme.primary
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text("By Profit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
        }
        items(products.sortedByDescending { it.totalProfit }) { p ->
            ProductRankCard(
                name = p.productName,
                primary = formatCurrency(p.totalProfit),
                secondary = "${String.format("%.1f%%", p.profitMarginPct)} margin",
                fraction = (p.totalProfit.coerceAtLeast(0.0) / maxProfit).toFloat(),
                barColor = ProfitGreenLight
            )
        }
    }
}

@Composable
private fun ProductRankCard(name: String, primary: String, secondary: String, fraction: Float, barColor: androidx.compose.ui.graphics.Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            HorizontalBar(fraction = fraction, color = barColor)
            Text(secondary, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TrendsTab(state: AnalyticsUiState) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Revenue vs Profit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    TrendBarChart(
                        trends = state.trends,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        if (state.trends.isNotEmpty()) {
            item {
                Text("Daily Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            items(state.trends.reversed()) { trend ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(trend.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(formatCurrency(trend.totalRevenue), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Text(
                        formatCurrency(trend.totalProfit),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        color = if (trend.totalProfit >= 0) ProfitGreenLight else LossRedLight
                    )
                }
            }
        }
    }
}

private val AnalyticsPeriod.label: String get() = when (this) {
    AnalyticsPeriod.TODAY -> "Today"
    AnalyticsPeriod.WEEK -> "7 Days"
    AnalyticsPeriod.MONTH -> "30 Days"
    AnalyticsPeriod.YEAR -> "1 Year"
}
