package com.inventra.app.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventra.app.domain.model.Product
import com.inventra.app.domain.model.Sale
import com.inventra.app.ui.components.EmptyState
import com.inventra.app.ui.components.SectionHeader
import com.inventra.app.ui.components.StatCard
import com.inventra.app.ui.components.StockDot
import com.inventra.app.ui.components.formatCurrency
import com.inventra.app.ui.components.formatQty
import com.inventra.app.ui.components.stockDotColor
import com.inventra.app.ui.theme.LossRedLight
import com.inventra.app.ui.theme.ProfitGreenLight
import com.inventra.app.ui.theme.WarningYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    onAddSale: () -> Unit,
    onStockIn: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentSales by viewModel.recentSales.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    var fabExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadTodaySummary() }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AnimatedVisibility(visible = fabExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Stock In", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                            SmallFloatingActionButton(
                                onClick = { fabExpanded = false; onStockIn() },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ) { Icon(Icons.Filled.Inventory2, "Stock In") }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Add Sale", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                            SmallFloatingActionButton(
                                onClick = { fabExpanded = false; onAddSale() },
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ) { Icon(Icons.Filled.AddShoppingCart, "Add Sale") }
                        }
                    }
                }
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, "Actions", tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Inline header — sits tight below status bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 4.dp)
                ) {
                    Text(
                        "Inventra",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(Date()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Today's Stats
            item {
                SectionHeader("Today")
                Spacer(Modifier.height(8.dp))
                val summary = uiState.todaySummary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        label = "Revenue",
                        value = formatCurrency(summary.totalRevenue),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Profit",
                        value = formatCurrency(summary.totalProfit),
                        modifier = Modifier.weight(1f),
                        valueColor = if (summary.totalProfit >= 0) ProfitGreenLight else LossRedLight
                    )
                    StatCard(
                        label = "Units Sold",
                        value = formatQty(summary.totalUnitsSold),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Low Stock Alerts
            if (lowStockProducts.isNotEmpty()) {
                item {
                    SectionHeader("Low Stock Alerts")
                    Spacer(Modifier.height(8.dp))
                }
                items(lowStockProducts) { product ->
                    LowStockAlert(product)
                }
            }

            // Recent Sales
            item {
                SectionHeader("Recent Sales")
                Spacer(Modifier.height(8.dp))
            }
            if (recentSales.isEmpty()) {
                item {
                    EmptyState(
                        icon = "🛒",
                        title = "No sales yet",
                        subtitle = "Tap + to record your first sale"
                    )
                }
            } else {
                items(recentSales) { sale -> RecentSaleRow(sale) }
            }

            item { Spacer(Modifier.height(72.dp)) } // FAB clearance
        }
    }
}

@Composable
private fun LowStockAlert(product: Product) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WarningYellow.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.Warning, null, tint = WarningYellow, modifier = Modifier.size(18.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Only ${formatQty(product.currentStockQty)} ${product.unit}(s) left",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentSaleRow(sale: Sale) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(sale.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    "${formatQty(sale.quantity)} × ${formatCurrency(sale.sellingPricePerUnit)} · ${
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sale.date))
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatCurrency(sale.totalRevenue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Profit: ${formatCurrency(sale.profit)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (sale.profit >= 0) ProfitGreenLight else LossRedLight
                )
            }
        }
    }
}
