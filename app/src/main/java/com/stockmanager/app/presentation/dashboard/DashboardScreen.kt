package com.stockmanager.app.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.stockmanager.app.data.entity.TransactionType
import com.stockmanager.app.presentation.viewmodel.DashboardViewModel
import com.stockmanager.app.ui.components.CommonComponents.LoadingState
import com.stockmanager.app.ui.components.CommonComponents.StatCard
import com.stockmanager.app.ui.components.CommonComponents.TransactionCard
import com.stockmanager.app.ui.theme.MaterialTheme
import com.stockmanager.app.ui.theme.StockManagerTheme
import com.stockmanager.app.ui.theme.TransactionColors
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Add
import androidx.compose.material3.icons.filled.ArrowDownward
import androidx.compose.material3.icons.filled.ArrowUpward
import androidx.compose.material3.icons.filled.CalendarMonth
import androidx.compose.material3.icons.filled.Inventory
import androidx.compose.material3.icons.filled.TrendingUp
import androidx.compose.material3.icons.filled.Warning
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavController
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val totalProducts by viewModel.totalProducts.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val outOfStockProducts by viewModel.outOfStockProducts.collectAsStateWithLifecycle()
    val totalStockValue by viewModel.totalStockValue.collectAsStateWithLifecycle()
    val totalRetailValue by viewModel.totalRetailValue.collectAsStateWithLifecycle()
    val potentialProfit by viewModel.potentialProfit.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val topSellingThisMonth by viewModel.topSellingThisMonth.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }

    StockManagerTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Top App Bar
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                actions = {
                    androidx.compose.material3.IconButton(onClick = { showDatePicker = true }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Select date range"
                        )
                    }
                    androidx.compose.material3.IconButton(onClick = { navController.navigate("settings") }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            )

            // Content
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
                    // Stats Grid
                    LazyVerticalGrid(
                        cells = GridCells.Fixed(2),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listOf(
                            StatData("Total Products", totalProducts.toString(), Icons.Default.Inventory, MaterialTheme.colorScheme.primary, null),
                            StatData("Low Stock", lowStockProducts.toString(), Icons.Default.Warning, MaterialTheme.colorScheme.tertiary, if (lowStockProducts > 0) "+${lowStockProducts} items" else null, lowStockProducts == 0),
                            StatData("Out of Stock", outOfStockProducts.toString(), Icons.Default.Error, MaterialTheme.colorScheme.error, if (outOfStockProducts > 0) "+${outOfStockProducts} items" else null, outOfStockProducts == 0),
                            StatData("Stock Value", "\$${"%.2f".format(totalStockValue)}", Icons.Default.AttachMoney, MaterialTheme.colorScheme.secondary, "\$${"%.2f".format(potentialProfit)} profit", potentialProfit > 0)
                        )) { stat ->
                            StatCard(
                                title = stat.title,
                                value = stat.value,
                                icon = stat.icon,
                                color = stat.color,
                                trend = stat.trend,
                                trendPositive = stat.trendPositive,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Quick Actions
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QuickActionButton(
                                    icon = Icons.Default.Add,
                                    label = "Stock In",
                                    color = TransactionColors.StockIn,
                                    onClick = { navController.navigate("stock/in") }
                                )
                                QuickActionButton(
                                    icon = Icons.Default.Remove,
                                    label = "Stock Out",
                                    color = TransactionColors.StockOut,
                                    onClick = { navController.navigate("stock/out") }
                                )
                                QuickActionButton(
                                    icon = Icons.Default.SwapHoriz,
                                    label = "Transfer",
                                    color = TransactionColors.Transfer,
                                    onClick = { navController.navigate("stock/transfer") }
                                )
                                QuickActionButton(
                                    icon = Icons.Default.Tune,
                                    label = "Adjustment",
                                    color = TransactionColors.Adjustment,
                                    onClick = { navController.navigate("stock/adjustment") }
                                )
                            }
                        }
                    }

                    // Low Stock Alert
                    if (lowStockItems.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(24.dp).padding(end = 8.dp)
                                        )
                                        Text(
                                            text = "${lowStockItems.size} Low Stock Items",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    Button(
                                        onClick = { navController.navigate("products?filter=low_stock") },
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        )
                                    ) {
                                        Text("View All")
                                    }
                                }
                                androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(lowStockItems.take(5)) { product ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = product.displayName,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Stock: ${product.currentStock}/${product.minStockLevel}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Recent Transactions
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Transactions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                ).let { text ->
                                    androidx.compose.foundation.gestures.clickable(
                                        onClick = { navController.navigate("transactions") },
                                        role = androidx.compose.ui.semantics.Role.Button
                                    ) {
                                        text
                                    }
                                }
                            }
                            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                            if (recentTransactions.isEmpty()) {
                                Text(
                                    text = "No recent transactions",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(recentTransactions.take(5)) { transaction ->
                                        TransactionCard(
                                            transaction = transaction,
                                            productName = "Product #${transaction.productId}", // TODO: Get product name
                                            onClick = { /* navController.navigate("transactions/${transaction.id}") */ }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Top Selling Products
                    if (topSellingThisMonth.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp).padding(end = 8.dp)
                                        )
                                        Text(
                                            text = "Top Selling This Month",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(topSellingThisMonth) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = item.name,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = "SKU: ${item.sku}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "Sold: ${item.totalSold}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "\$${"%.2f".format(item.totalRevenue)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier
            .weight(1f)
            .fillMaxWidth()
            .height(80.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class StatData(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val trend: String?,
    val trendPositive: Boolean
)