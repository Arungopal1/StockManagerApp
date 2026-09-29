package com.stockmanager.app.presentation.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stockmanager.app.R
import com.stockmanager.app.data.entity.Product
import com.stockmanager.app.data.entity.TransactionType
import com.stockmanager.app.presentation.viewmodel.ProductViewModel
import com.stockmanager.app.presentation.viewmodel.StockTransactionViewModel
import com.stockmanager.app.ui.components.CommonComponents.LoadingState
import com.stockmanager.app.ui.theme.MaterialTheme
import com.stockmanager.app.ui.theme.StockManagerTheme
import com.stockmanager.app.ui.theme.TransactionColors
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowDownward
import androidx.compose.material3.icons.filled.ArrowUpward
import androidx.compose.material3.icons.filled.Check
import androidx.compose.material3.icons.filled.Close
import androidx.compose.material3.icons.filled.QrCodeScanner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavController
import androidx.navigation.compose.getString
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@Composable
fun StockTransactionScreen(
    navController: NavController,
    transactionType: TransactionType,
    viewModel: StockTransactionViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    var selectedProductId by remember { mutableStateOf<Long?>(null) }
    var quantity by remember { mutableStateOf("") }
    var unitCost by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var supplierId by remember { mutableStateOf<Long?>(null) }
    var customerId by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showScanner by remember { mutableStateOf(false) }

    val preSelectedProductId = navController.getString("productId")?.toLongOrNull()
    if (preSelectedProductId != null && selectedProductId == null) {
        selectedProductId = preSelectedProductId
    }

    val selectedProduct = selectedProductId?.let { id ->
        productViewModel.getByIdSync(id)
    }

    val typeColor = when (transactionType) {
        TransactionType.STOCK_IN -> TransactionColors.StockIn
        TransactionType.STOCK_OUT -> TransactionColors.StockOut
        TransactionType.RETURN -> TransactionColors.Return
        TransactionType.ADJUSTMENT -> TransactionColors.Adjustment
        TransactionType.TRANSFER -> TransactionColors.Transfer
    }

    val typeIcon = when (transactionType) {
        TransactionType.STOCK_IN -> Icons.Default.ArrowDownward
        TransactionType.STOCK_OUT -> Icons.Default.ArrowUpward
        TransactionType.RETURN -> Icons.Default.Undo
        TransactionType.ADJUSTMENT -> Icons.Default.Tune
        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
    }

    val typeLabel = when (transactionType) {
        TransactionType.STOCK_IN -> "Stock In (Receive)"
        TransactionType.STOCK_OUT -> "Stock Out (Sell)"
        TransactionType.RETURN -> "Return"
        TransactionType.ADJUSTMENT -> "Adjustment"
        TransactionType.TRANSFER -> "Transfer"
    }

    val isIncoming = transactionType == TransactionType.STOCK_IN || transactionType == TransactionType.RETURN

    StockManagerTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            TopAppBar(
                title = { Text(text = typeLabel) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = typeColor.copy(alpha = 0.15f),
                    titleContentColor = typeColor,
                )
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Product Selection Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Select Product",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (selectedProduct != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = selectedProduct.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "SKU: ${selectedProduct.sku} | Stock: ${selectedProduct.currentStock}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Cost: \$${"%.2f".format(selectedProduct.purchasePrice)} | Price: \$${"%.2f".format(selectedProduct.sellingPrice)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    androidx.compose.material3.IconButton(onClick = { selectedProductId = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Change product")
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = "",
                                        onValueChange = {},
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text("Search product...") },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                        trailingIcon = {
                                            IconButton(onClick = { showScanner = true }) {
                                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan barcode")
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                        singleLine = true
                                    )
                                }
                                Text(
                                    text = "Or scan barcode/IMEI to find product",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Quantity & Pricing Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Quantity & Pricing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = quantity,
                                    onValueChange = { quantity = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Quantity") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = unitCost,
                                    onValueChange = { unitCost = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Unit Cost") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    placeholder = { Text(selectedProduct?.purchasePrice?.toString() ?: "") }
                                )
                            }

                            OutlinedTextField(
                                value = unitPrice,
                                onValueChange = { unitPrice = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Unit Price") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                placeholder = { Text(selectedProduct?.sellingPrice?.toString() ?: "") }
                            )

                            // Total calculation
                            val qty = quantity.toIntOrNull() ?: 0
                            val cost = unitCost.toDoubleOrNull() ?: (selectedProduct?.purchasePrice ?: 0.0)
                            val price = unitPrice.toDoubleOrNull() ?: (selectedProduct?.sellingPrice ?: 0.0)
                            if (qty > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Total Cost: \$${"%.2f".format(qty * cost)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Total Value: \$${"%.2f".format(qty * price)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = typeColor
                                    )
                                }
                            }
                        }
                    }

                    // Supplier/Customer Card
                    if (transactionType == TransactionType.STOCK_IN || transactionType == TransactionType.STOCK_OUT || transactionType == TransactionType.RETURN) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = when (transactionType) {
                                        TransactionType.STOCK_IN -> "Supplier"
                                        TransactionType.STOCK_OUT -> "Customer"
                                        TransactionType.RETURN -> "Customer (Return)"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                    value = "",
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(when (transactionType) {
                                        TransactionType.STOCK_IN -> "Search Supplier"
                                        else -> "Search Customer"
                                    }) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                    singleLine = true
                                )
                                Text(
                                    text = "Select from existing or create new",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Notes Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Notes (Optional)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Notes") },
                                singleLine = false,
                                minLines = 3,
                                maxLines = 5
                            )
                        }
                    }

                    // Error Message
                    errorMessage?.let { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                IconButton(onClick = { errorMessage = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            if (validateForm()) {
                                submitTransaction()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        enabled = !isLoading && validateForm(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = typeColor,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp).padding(end = 8.dp)
                            )
                        }
                        Icon(typeIcon, contentDescription = null, modifier = Modifier.size(20.dp).padding(end = 8.dp))
                        Text(
                            text = if (isLoading) "Processing..." else "Record ${typeLabel}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    fun validateForm(): Boolean {
        return selectedProductId != null &&
               quantity.toIntOrNull() != null &&
               quantity.toIntOrNull()! > 0 &&
               unitCost.toDoubleOrNull() != null &&
               unitPrice.toDoubleOrNull() != null
    }

    fun submitTransaction() {
        isLoading = true
        errorMessage = null

        val productId = selectedProductId!!
        val qty = quantity.toInt()!!
        val cost = unitCost.toDouble()!!
        val price = unitPrice.toDouble()!!
        val userId = 1L // TODO: Get from auth

        androidx.lifecycle.viewmodel.compose.viewModelScope.launch {
            try {
                when (transactionType) {
                    TransactionType.STOCK_IN -> {
                        viewModel.recordStockIn(productId, qty, cost, price, supplierId, userId, notes)
                    }
                    TransactionType.STOCK_OUT -> {
                        viewModel.recordStockOut(productId, qty, cost, price, customerId, userId, notes)
                    }
                    TransactionType.RETURN -> {
                        viewModel.recordReturn(productId, qty, cost, price, customerId, userId, null, notes)
                    }
                    TransactionType.ADJUSTMENT -> {
                        viewModel.recordAdjustment(productId, if (isIncoming) qty else -qty, cost, price, userId, notes)
                    }
                    TransactionType.TRANSFER -> {
                        viewModel.recordTransfer(productId, qty, cost, price, null, null, userId, notes)
                    }
                }
                navController.popBackStack()
            } catch (e: Exception) {
                errorMessage = "Failed to record transaction: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun StockInScreen(
    navController: NavController,
    viewModel: StockTransactionViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    StockTransactionScreen(
        navController = navController,
        transactionType = TransactionType.STOCK_IN,
        viewModel = viewModel,
        productViewModel = productViewModel
    )
}

@Composable
fun StockOutScreen(
    navController: NavController,
    viewModel: StockTransactionViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    StockTransactionScreen(
        navController = navController,
        transactionType = TransactionType.STOCK_OUT,
        viewModel = viewModel,
        productViewModel = productViewModel
    )
}

@Composable
fun StockAdjustmentScreen(
    navController: NavController,
    viewModel: StockTransactionViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    StockTransactionScreen(
        navController = navController,
        transactionType = TransactionType.ADJUSTMENT,
        viewModel = viewModel,
        productViewModel = productViewModel
    )
}

@Composable
fun StockTransferScreen(
    navController: NavController,
    viewModel: StockTransactionViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    StockTransactionScreen(
        navController = navController,
        transactionType = TransactionType.TRANSFER,
        viewModel = viewModel,
        productViewModel = productViewModel
    )
}

@Composable
fun StockReturnScreen(
    navController: NavController,
    viewModel: StockTransactionViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    StockTransactionScreen(
        navController = navController,
        transactionType = TransactionType.RETURN,
        viewModel = viewModel,
        productViewModel = productViewModel
    )
}