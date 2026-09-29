package com.stockmanager.app.presentation.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stockmanager.app.R
import com.stockmanager.app.data.entity.Product
import com.stockmanager.app.data.entity.ProductCategory
import com.stockmanager.app.data.entity.ProductCondition
import com.stockmanager.app.presentation.viewmodel.ProductViewModel
import com.stockmanager.app.ui.components.CommonComponents.EmptyState
import com.stockmanager.app.ui.components.CommonComponents.LoadingState
import com.stockmanager.app.ui.components.CommonComponents.ProductCard
import com.stockmanager.app.ui.components.CommonComponents.SearchBar
import com.stockmanager.app.ui.theme.MaterialTheme
import com.stockmanager.app.ui.theme.StockManagerTheme
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Add
import androidx.compose.material3.icons.filled.FilterList
import androidx.compose.material3.icons.filled.Sort
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@Composable
fun ProductsScreen(
    navController: NavController,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val allBrands by viewModel.allBrands.collectAsStateWithLifecycle()
    val activeCount by viewModel.activeCount.collectAsStateWithLifecycle()
    val lowStockCount by viewModel.lowStockCount.collectAsStateWithLifecycle()
    val outOfStockCount by viewModel.outOfStockCount.collectAsStateWithLifecycle()

    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    StockManagerTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Top App Bar
            TopAppBar(
                title = { Text(text = "Products ($activeCount)") },
                actions = {
                    androidx.compose.material3.IconButton(onClick = { showFilterMenu = true }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    androidx.compose.material3.IconButton(onClick = { showSortMenu = true }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            )

            // Search Bar
            SearchBar(
                query = viewModel.searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                placeholder = "Search products..."
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.stockmanager.app.ui.components.FilterChip(
                    label = "All",
                    selected = viewModel.selectedCategory == null,
                    onClick = { viewModel.setCategory(null) }
                )
                ProductCategory.values().forEach { category ->
                    com.stockmanager.app.ui.components.FilterChip(
                        label = category.displayName,
                        selected = viewModel.selectedCategory == category,
                        onClick = { viewModel.setCategory(category) },
                        icon = getCategoryIcon(category)
                    )
                }
            }

            // Product List
            Box(modifier = Modifier.fillMaxSize()) {
                if (products.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Inventory,
                        title = "No Products Found",
                        message = "Add your first product to get started",
                        actionText = "Add Product",
                        onActionClick = { navController.navigate("products/add") }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products) { product ->
                            ProductCard(
                                product = product,
                                onClick = { navController.navigate("products/${product.id}") },
                                onEditClick = { navController.navigate("products/edit/${product.id}") },
                                onDeleteClick = { /* TODO: Show delete confirmation */ }
                            )
                        }
                    }
                }
            }

            // FAB
            FloatingActionButton(
                onClick = { navController.navigate("products/add") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Product"
                )
            }
        }
    }
}

@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: Long,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val product by viewModel.getById(productId).collectAsStateWithLifecycle()

    product?.let { product ->
        StockManagerTheme {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                TopAppBar(
                    title = { Text(text = product.displayName) },
                    navigationIcon = {
                        androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        androidx.compose.material3.IconButton(onClick = { navController.navigate("products/edit/${product.id}") }) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }
                        androidx.compose.material3.IconButton(onClick = { navController.navigate("stock/in?productId=${product.id}") }) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Stock In"
                            )
                        }
                        androidx.compose.material3.IconButton(onClick = { navController.navigate("stock/out?productId=${product.id}") }) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Stock Out"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                )

                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopStart
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Product Image Placeholder
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = getCategoryIcon(product.category),
                                    contentDescription = "Product Image",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        // Details
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
                                DetailRow("SKU", product.sku)
                                DetailRow("Category", product.category.displayName)
                                DetailRow("Brand", product.brand)
                                DetailRow("Model", product.model)
                                DetailRow("Color", product.color)
                                DetailRow("Storage", product.storage)
                                DetailRow("Condition", product.condition.displayName)
                                DetailRow("Location", product.location ?: "Not set")
                                DetailRow("Warranty", "${product.warrantyMonths} months")

                                androidx.compose.foundation.layout.Divider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        DetailRow("Purchase Price", "\$${"%.2f".format(product.purchasePrice)}")
                                        DetailRow("Selling Price", "\$${"%.2f".format(product.sellingPrice)}")
                                        DetailRow("Profit Margin", "${"%.1f".format(product.profitMargin)}%")
                                    }
                                    Column {
                                        DetailRow("Current Stock", product.currentStock.toString())
                                        DetailRow("Min Level", product.minStockLevel.toString())
                                        DetailRow("Max Level", product.maxStockLevel.toString())
                                    }
                                }
                            }
                        }

                        // Stock Actions
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Stock Actions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { navController.navigate("stock/in?productId=${product.id}") },
                                        modifier = Modifier.weight(1f),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = com.stockmanager.app.ui.theme.TransactionColors.StockIn.copy(alpha = 0.15f),
                                            contentColor = com.stockmanager.app.ui.theme.TransactionColors.StockIn
                                        )
                                    ) {
                                        Text("Stock In")
                                    }
                                    Button(
                                        onClick = { navController.navigate("stock/out?productId=${product.id}") },
                                        modifier = Modifier.weight(1f),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = com.stockmanager.app.ui.theme.TransactionColors.StockOut.copy(alpha = 0.15f),
                                            contentColor = com.stockmanager.app.ui.theme.TransactionColors.StockOut
                                        )
                                    ) {
                                        Text("Stock Out")
                                    }
                                    Button(
                                        onClick = { navController.navigate("stock/adjustment?productId=${product.id}") },
                                        modifier = Modifier.weight(1f),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = com.stockmanager.app.ui.theme.TransactionColors.Adjustment.copy(alpha = 0.15f),
                                            contentColor = com.stockmanager.app.ui.theme.TransactionColors.Adjustment
                                        )
                                    ) {
                                        Text("Adjust")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } ?: LoadingState()
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProductFormScreen(
    navController: NavController,
    productId: Long? = null,
    viewModel: ProductViewModel = hiltViewModel()
) {
    // TODO: Implement product form with all fields
    StockManagerTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            TopAppBar(
                title = { Text(text = if (productId != null) "Edit Product" else "Add Product") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            )
            // TODO: Form fields
            Text(text = "Product Form - TODO", modifier = Modifier.padding(16.dp))
        }
    }
}

fun getCategoryIcon(category: ProductCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        ProductCategory.SMARTPHONE -> Icons.Default.PhoneAndroid
        ProductCategory.TABLET -> Icons.Default.TabletAndroid
        ProductCategory.LAPTOP -> Icons.Default.Laptop
        ProductCategory.ACCESSORY -> Icons.Default.Headphones
        ProductCategory.PART -> Icons.Default.Build
        ProductCategory.CASE -> Icons.Default.Shield
        ProductCategory.CHARGER -> Icons.Default.BatteryChargingFull
        ProductCategory.AUDIO -> Icons.Default.MusicNote
        ProductCategory.WEARABLE -> Icons.Default.Watch
        ProductCategory.OTHER -> Icons.Default.Category
    }
}