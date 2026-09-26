package com.inventra.app.presentation.products

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

private val UNITS = listOf("piece", "kg", "g", "litre", "ml", "box", "pack", "pair", "dozen", "metre")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: String?,
    onBack: () -> Unit,
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val isEdit = productId != null
    val scope = rememberCoroutineScope()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var productCode by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("piece") }
    var minStockLevel by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var catDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (productId != null) {
            val p = viewModel.getProduct(productId)
            if (p != null) {
                name = p.name; category = p.category; productCode = p.productCode
                unit = p.unit; minStockLevel = if (p.minStockLevel > 0) p.minStockLevel.toString() else ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Product" else "New Product", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it; nameError = null },
                label = { Text("Product Name *") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category with dropdown
            ExposedDropdownMenuBox(expanded = catDropdownExpanded, onExpandedChange = { catDropdownExpanded = it }) {
                OutlinedTextField(
                    value = category, onValueChange = { category = it },
                    label = { Text("Category (optional)") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catDropdownExpanded) }
                )
                if (categories.isNotEmpty()) {
                    ExposedDropdownMenu(expanded = catDropdownExpanded, onDismissRequest = { catDropdownExpanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; catDropdownExpanded = false })
                        }
                    }
                }
            }

            OutlinedTextField(
                value = productCode, onValueChange = { productCode = it },
                label = { Text("Product Code / SKU (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Unit dropdown
            ExposedDropdownMenuBox(expanded = unitDropdownExpanded, onExpandedChange = { unitDropdownExpanded = it }) {
                OutlinedTextField(
                    value = unit, onValueChange = {},
                    label = { Text("Unit") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitDropdownExpanded) }
                )
                ExposedDropdownMenu(expanded = unitDropdownExpanded, onDismissRequest = { unitDropdownExpanded = false }) {
                    UNITS.forEach { u ->
                        DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitDropdownExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = minStockLevel, onValueChange = { minStockLevel = it },
                label = { Text("Low Stock Alert Threshold (optional)") },
                placeholder = { Text("e.g. 5") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("Alert when stock drops to this level") }
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) { nameError = "Name is required"; return@Button }
                    scope.launch {
                        viewModel.saveProduct(
                            id = productId,
                            name = name.trim(),
                            category = category.trim(),
                            productCode = productCode.trim(),
                            unit = unit,
                            minStockLevel = minStockLevel.toDoubleOrNull() ?: 0.0
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(if (isEdit) "Save Changes" else "Add Product", style = MaterialTheme.typography.titleSmall)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
