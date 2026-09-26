package com.inventra.app.presentation.stock

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockInScreen(
    preselectedProductId: String?,
    onBack: () -> Unit,
    viewModel: StockViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("") }
    var costPerUnit by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val date = remember { System.currentTimeMillis() }

    LaunchedEffect(products, preselectedProductId) {
        if (selectedProduct == null && preselectedProductId != null) {
            selectedProduct = products.find { it.id == preselectedProductId }
        }
    }

    LaunchedEffect(result) {
        when (result) {
            is StockInResult.Success -> { onBack(); viewModel.resetResult() }
            is StockInResult.Error -> { snackbarHostState.showSnackbar((result as StockInResult.Error).msg); viewModel.resetResult() }
            else -> {}
        }
    }

    val qty = quantity.toDoubleOrNull() ?: 0.0
    val cost = costPerUnit.toDoubleOrNull() ?: 0.0
    val newAvgCost = selectedProduct?.let { viewModel.previewNewAvgCost(it, qty, cost) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock In", fontWeight = FontWeight.Bold) },
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

            // Product selector
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
                            text = { Text(p.name) },
                            onClick = { selectedProduct = p; productDropdownExpanded = false }
                        )
                    }
                }
            }

            // Current stock info
            selectedProduct?.let { p ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MiniStat("Current Stock", "${formatQty(p.currentStockQty)} ${p.unit}")
                        MiniStat("Avg Cost", formatCurrency(p.currentAvgCost))
                        MiniStat("Stock Value", formatCurrency(p.currentStockValue))
                    }
                }
            }

            OutlinedTextField(
                value = quantity, onValueChange = { quantity = it },
                label = { Text("Quantity *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = costPerUnit, onValueChange = { costPerUnit = it },
                label = { Text("Cost per Unit *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("$", style = MaterialTheme.typography.bodyLarge) }
            )

            OutlinedTextField(
                value = supplier, onValueChange = { supplier = it },
                label = { Text("Supplier (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            // WAC Preview card
            if (selectedProduct != null && qty > 0 && cost > 0 && newAvgCost != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("After This Purchase", style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        HorizontalDivider()
                        val p = selectedProduct!!
                        PreviewRow("New Stock", "${formatQty(p.currentStockQty + qty)} ${p.unit}")
                        PreviewRow("New Avg Cost", formatCurrency(newAvgCost))
                        PreviewRow("Total Cost of This Purchase", formatCurrency(qty * cost))
                        Text(
                            "Avg cost uses Weighted Average — blends existing stock cost with new purchase cost",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val p = selectedProduct ?: return@Button
                    viewModel.addStock(p.id, qty, cost, date, supplier, notes)
                },
                enabled = selectedProduct != null && qty > 0 && cost >= 0,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Confirm Stock In", style = MaterialTheme.typography.titleSmall)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
