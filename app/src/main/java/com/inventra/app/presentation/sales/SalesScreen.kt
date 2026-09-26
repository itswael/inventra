package com.inventra.app.presentation.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventra.app.domain.model.Sale
import com.inventra.app.ui.components.EmptyState
import com.inventra.app.ui.components.formatCurrency
import com.inventra.app.ui.components.formatQty
import com.inventra.app.ui.theme.LossRedLight
import com.inventra.app.ui.theme.ProfitGreenLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// This screen is accessible from Analytics or deep-links; bottom nav uses Dashboard for recent sales
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onAddSale: () -> Unit,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val sales by viewModel.recentSales.collectAsStateWithLifecycle()
    val grouped = sales.groupBy {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it.date))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSale, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Add, "Add Sale", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { innerPadding ->
        if (sales.isEmpty()) {
            EmptyState("🛒", "No sales recorded yet", "Tap + to record your first sale",
                Modifier.fillMaxSize().padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                grouped.forEach { (dateLabel, daySales) ->
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            dateLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        val dayRevenue = daySales.sumOf { it.totalRevenue }
                        val dayProfit = daySales.sumOf { it.profit }
                        Text(
                            "Revenue: ${formatCurrency(dayRevenue)}  ·  Profit: ${formatCurrency(dayProfit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (dayProfit >= 0) ProfitGreenLight else LossRedLight
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    items(daySales) { sale -> SaleRow(sale) }
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun SaleRow(sale: Sale) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(sale.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    "${formatQty(sale.quantity)} ${sale.productUnit} × ${formatCurrency(sale.sellingPricePerUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sale.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(formatCurrency(sale.totalRevenue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${if (sale.profit >= 0) "+" else ""}${formatCurrency(sale.profit)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (sale.profit >= 0) ProfitGreenLight else LossRedLight
                )
            }
        }
    }
}
