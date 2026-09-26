package com.inventra.app.presentation.inventory

import android.content.Intent
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventra.app.domain.model.InventoryItem
import com.inventra.app.domain.model.StockStatus
import com.inventra.app.ui.components.EmptyState
import com.inventra.app.ui.components.StatCard
import com.inventra.app.ui.components.StockDot
import com.inventra.app.ui.components.formatCurrency
import com.inventra.app.ui.components.formatQty
import com.inventra.app.ui.components.stockDotColor
import com.inventra.app.ui.theme.LossRedLight
import com.inventra.app.ui.theme.ProfitGreenLight
import com.inventra.app.ui.theme.WarningYellow

@Composable
fun InventoryScreen(viewModel: InventoryViewModel = hiltViewModel()) {
    val items by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Share Report") },
                icon = { Icon(Icons.Filled.Share, null) },
                onClick = {
                    val text = viewModel.buildShareText(items)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                        putExtra(Intent.EXTRA_SUBJECT, "Inventory Report")
                    }
                    context.startActivity(Intent.createChooser(intent, "Share via"))
                },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { innerPadding ->
        if (items.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(innerPadding)) {
                Text("Inventory", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp))
                EmptyState("🏪", "No inventory yet", "Add products and stock to see your inventory",
                    Modifier.weight(1f))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title
            item {
                Text("Inventory", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    modifier = Modifier.statusBarsPadding().padding(top = 8.dp, bottom = 4.dp))
            }
            // Summary strip
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Products", summary.totalProducts.toString(), Modifier.weight(1f))
                    StatCard("Units", formatQty(summary.totalUnits), Modifier.weight(1f))
                    StatCard("Value", formatCurrency(summary.totalValue), Modifier.weight(1f))
                }
            }

            // Legend
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    StockLegendItem(ProfitGreenLight, "In Stock")
                    StockLegendItem(WarningYellow, "Low Stock")
                    StockLegendItem(LossRedLight, "Out of Stock")
                }
            }

            item { HorizontalDivider() }

            // Stock items
            items(items.sortedWith(compareBy({ it.stockStatus.ordinal }, { it.product.name })), key = { it.product.id }) { item ->
                InventoryRow(item)
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StockLegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        StockDot(color, 9.dp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InventoryRow(item: InventoryItem) {
    val p = item.product
    val dotColor = when (item.stockStatus) {
        StockStatus.OK -> ProfitGreenLight
        StockStatus.LOW -> WarningYellow
        StockStatus.OUT -> LossRedLight
    }

    Card(
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (item.stockStatus) {
                StockStatus.OUT -> LossRedLight.copy(alpha = 0.06f)
                StockStatus.LOW -> WarningYellow.copy(alpha = 0.06f)
                StockStatus.OK -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StockDot(dotColor, 11.dp)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(p.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    if (p.productCode.isNotBlank()) {
                        Text("#${p.productCode}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (p.category.isNotBlank()) {
                    Text(p.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${formatQty(p.currentStockQty)} ${p.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = dotColor
                )
                Text(
                    "≈ ${formatCurrency(item.stockValue)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "@${formatCurrency(p.currentAvgCost)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
