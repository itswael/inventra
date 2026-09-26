package com.inventra.app.presentation.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inventra.app.domain.model.Product
import com.inventra.app.ui.components.formatCurrency
import com.inventra.app.ui.components.formatQty
import com.inventra.app.ui.theme.LossRedLight
import com.inventra.app.ui.theme.ProfitGreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleScreen(
    preselectedProductId: String?,
    onBack: () -> Unit,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val date = remember { System.currentTimeMillis() }

    LaunchedEffect(products, preselectedProductId) {
        if (selectedProduct == null && preselectedProductId != null) {
            selectedProduct = products.find { it.id == preselectedProductId }
        }
    }

    LaunchedEffect(result) {
        when (result) {
            is AddSaleResult.Success -> { onBack(); viewModel.resetResult() }
            is AddSaleResult.Error -> { snackbarHostState.showSnackbar((result as AddSaleResult.Error).msg); viewModel.resetResult() }
            else -> {}
        }
    }

    val qty = quantity.toDoubleOrNull() ?: 0.0
    val price = sellingPrice.toDoubleOrNull() ?: 0.0
    val profitPreview = selectedProduct?.let { viewModel.computeProfitPreview(it, qty, price) }
    val totalRevenue = qty * price

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Sale", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()).imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Product picker
            ExposedDropdownMenuBox(expanded = productDropdownExpanded, onExpandedChange = { productDropdownExpanded = it }) {
                OutlinedTextField(
                    value = selectedProduct?.name ?: "",
                    onValueChange = {},
                    label = { Text("Product *") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(productDropdownExpanded) }
                )
                ExposedDropdownMenu(expanded = productDropdownExpanded, onDismissRequest = { productDropdownExpanded = false }) {
                    products.forEach { p ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(p.name)
                                    Text(
                                        "Stock: ${formatQty(p.currentStockQty)} ${p.unit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = { selectedProduct = p; productDropdownExpanded = false }
                        )
                    }
                }
            }

            // Current stock chip
            selectedProduct?.let { p ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (p.isOutOfStock) LossRedLight.copy(0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("Available", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${formatQty(p.currentStockQty)} ${p.unit}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("Avg Cost", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatCurrency(p.currentAvgCost), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = quantity, onValueChange = { quantity = it },
                label = { Text("Quantity *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = selectedProduct?.let {
                    if (qty > it.currentStockQty && qty > 0) {
                        { Text("Exceeds available stock (${formatQty(it.currentStockQty)})", color = LossRedLight) }
                    } else null
                }
            )

            OutlinedTextField(
                value = sellingPrice, onValueChange = { sellingPrice = it },
                label = { Text("Selling Price per Unit *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("$", style = MaterialTheme.typography.bodyLarge) }
            )

            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            // Profit preview
            if (selectedProduct != null && qty > 0 && price > 0 && profitPreview != null) {
                val isProfit = profitPreview >= 0
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isProfit) ProfitGreenLight.copy(0.1f) else LossRedLight.copy(0.1f)
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Sale Preview",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        HorizontalDivider()
                        PreviewRow("Total Revenue", formatCurrency(totalRevenue))
                        PreviewRow("Total Cost (WAC)", formatCurrency(selectedProduct!!.currentAvgCost * qty))
                        PreviewRow("Profit", formatCurrency(profitPreview), if (isProfit) ProfitGreenLight else LossRedLight)
                        if (totalRevenue > 0) {
                            val margin = (profitPreview / totalRevenue) * 100
                            PreviewRow("Margin", String.format("%.1f%%", margin))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val p = selectedProduct ?: return@Button
                    viewModel.addSale(p.id, qty, price, date, notes)
                },
                enabled = selectedProduct != null && qty > 0 && price >= 0
                        && qty <= (selectedProduct?.currentStockQty ?: 0.0),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Record Sale", style = MaterialTheme.typography.titleSmall)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
