package com.stockmanager.app.presentation.reports

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
import androidx.compose.material3.Chip
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
import com.stockmanager.app.presentation.viewmodel.StockTransactionViewModel
import com.stockmanager.app.ui.components.CommonComponents.LoadingState
import com.stockmanager.app.ui.theme.MaterialTheme
import com.stockmanager.app.ui.theme.StockManagerTheme
import com.stockmanager.app.ui.theme.TransactionColors
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.CalendarMonth
import androidx.compose.material3.icons.filled.Download
import androidx.compose.material3.icons.filled.TrendingUp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: StockTransactionViewModel = hiltViewModel()
) {
    val monthlySummary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val topSellingProducts by viewModel.topSellingProducts.collectAsStateWithLifecycle()
    val stockMovementReport by viewModel.stockMovementReport.collectAsStateWithLifecycle()
    val profitReport by viewModel.profitReport.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("Overview", "Top Selling", "Stock Movement", "Profit Report")

    StockManagerTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            TopAppBar(
                title = { Text(text = "Reports & Analytics") },
                actions = {
                    androidx.compose.material3.IconButton(onClick = { showDatePicker = true }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Date Range"
                        )
                    }
                    androidx.compose.material3.IconButton(onClick = { /* TODO: Export */ }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            )

            // Tab Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    androidx.compose.material3.Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        text = { Text(text = tab, maxLines = 1) },
                        indicator = { /* Custom indicator */ },
                        colors = androidx.compose.material3.TabDefaults.tabColors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        )
                    )
                }
            }

            // Tab Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> OverviewTab(monthlySummary, topSellingProducts)
                    1 -> TopSellingTab(topSellingProducts)
                    2 -> StockMovementTab(stockMovementReport)
                    3 -> ProfitTab(profitReport)
                }
            }
        }
    }
}

@Composable
fun OverviewTab(
    monthlySummary: List<com.stockmanager.app.data.dao.MonthlyTransactionSummary>,
    topSellingProducts: List<com.stockmanager.app.data.dao.TopSellingProduct>
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Monthly Summary Chart Placeholder
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
                    text = "Monthly Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (monthlySummary.isEmpty()) {
                    Text(
                        text = "No data available for selected period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                } else {
                    // Group by month
                    monthlySummary.groupBy { it.month }.forEach { (month, items) ->
                        val inItems = items.filter { it.transactionType == TransactionType.STOCK_IN }
                        val outItems = items.filter { it.transactionType == TransactionType.STOCK_OUT }
                        val totalIn = inItems.sumOf { it.totalQuantity }
                        val totalOut = outItems.sumOf { it.totalQuantity }
                        val totalValue = items.sumOf { it.totalValue }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = month,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStat("In", totalIn.toString(), TransactionColors.StockIn)
                            SummaryStat("Out", totalOut.toString(), TransactionColors.StockOut)
                            SummaryStat("Value", "\$${"%.2f".format(totalValue)}", MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Top 5 Products Quick View
        if (topSellingProducts.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top 5 Selling Products",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    topSellingProducts.take(5).forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 1}. ${item.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.totalSold} sold",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "\$${"%.2f".format(item.totalRevenue)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (index < 4) androidx.compose.foundation.layout.Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun TopSellingTab(topSellingProducts: List<com.stockmanager.app.data.dao.TopSellingProduct>) {
    if (topSellingProducts.isEmpty()) {
        com.stockmanager.app.ui.components.CommonComponents.EmptyState(
            icon = Icons.Default.TrendingUp,
            title = "No Sales Data",
            message = "No products sold in the selected period"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(topSellingProducts) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "SKU: ${item.sku}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${item.totalSold}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
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

@Composable
fun StockMovementTab(stockMovementReport: List<com.stockmanager.app.data.dao.StockMovementReport>) {
    if (stockMovementReport.isEmpty()) {
        com.stockmanager.app.ui.components.CommonComponents.EmptyState(
            icon = Icons.Default.SwapHoriz,
            title = "No Stock Movement",
            message = "No stock movements in the selected period"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stockMovementReport) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "SKU: ${item.sku} | Current: ${item.currentStock}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MovementStat("In", item.totalIn, TransactionColors.StockIn)
                            MovementStat("Out", item.totalOut, TransactionColors.StockOut)
                            MovementStat("Returns", item.totalReturns, TransactionColors.Return)
                            MovementStat("Adj.", item.totalAdjustments, TransactionColors.Adjustment)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfitTab(profitReport: List<com.stockmanager.app.data.dao.ProfitReportItem>) {
    if (profitReport.isEmpty()) {
        com.stockmanager.app.ui.components.CommonComponents.EmptyState(
            icon = Icons.Default.AttachMoney,
            title = "No Profit Data",
            message = "No profit data for the selected period"
        )
    } else {
        val totalProfit = profitReport.sumOf { it.totalProfit }
        val totalRevenue = profitReport.sumOf { it.totalRevenue }
        val totalCost = profitReport.sumOf { it.totalCost }
        val margin = if (totalRevenue > 0) (totalProfit / totalRevenue * 100) else 0.0

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfitSummaryCard("Revenue", "\$${"%.2f".format(totalRevenue)}", MaterialTheme.colorScheme.primary, Icons.Default.TrendingUp)
                ProfitSummaryCard("Cost", "\$${"%.2f".format(totalCost)}", MaterialTheme.colorScheme.secondary, Icons.Default.ShoppingCart)
                ProfitSummaryCard("Profit", "\$${"%.2f".format(totalProfit)}", TransactionColors.StockIn, Icons.Default.AttachMoney)
                ProfitSummaryCard("Margin", "${"%.1f".format(margin)}%", TransactionColors.StockOut, Icons.Default.Percent)
            }

            // Profit by Product
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(profitReport) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "SKU: ${item.sku}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "\$${"%.2f".format(item.totalProfit)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.totalProfit >= 0) TransactionColors.StockIn else TransactionColors.StockOut
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Rev: \$${"%.2f".format(item.totalRevenue)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Cost: \$${"%.2f".format(item.totalCost)}",
                                        style = MaterialTheme.typography.labelSmall,
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

@Composable
fun SummaryStat(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MovementStat(label: String, value: Int, color: Color) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfitSummaryCard(title: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f),
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}