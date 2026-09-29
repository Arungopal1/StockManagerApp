package com.stockmanager.app.presentation.transactions

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
import com.stockmanager.app.ui.components.CommonComponents.TransactionCard
import com.stockmanager.app.ui.theme.MaterialTheme
import com.stockmanager.app.ui.theme.StockManagerTheme
import com.stockmanager.app.ui.theme.TransactionColors
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.CalendarMonth
import androidx.compose.material3.icons.filled.FilterList
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@Composable
fun TransactionsScreen(
    navController: NavController,
    viewModel: StockTransactionViewModel = hiltViewModel()
) {
    val transactionsByDateRange by viewModel.transactionsByDateRange.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTypeFilter by remember { mutableStateOf(false) }

    StockManagerTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            TopAppBar(
                title = { Text(text = "Transactions") },
                actions = {
                    androidx.compose.material3.IconButton(onClick = { showDatePicker = true }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Date Range"
                        )
                    }
                    androidx.compose.material3.IconButton(onClick = { showTypeFilter = true }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter by Type"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            )

            // Date Range Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateRangeChip("Today", viewModel::setToday)
                DateRangeChip("This Week", viewModel::setThisWeek)
                DateRangeChip("This Month", viewModel::setThisMonth)
                DateRangeChip("Last Month", viewModel::setLastMonth)
            }

            // Type Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TypeFilterChip("All", null, viewModel.selectedType == null) {
                    viewModel.setType(null)
                }
                TransactionType.values().forEach { type ->
                    TypeFilterChip(type.displayName, type, viewModel.selectedType == type) {
                        viewModel.setType(type)
                    }
                }
            }

            // Summary Cards
            if (dailySummary.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dailySummary.groupBy { it.date }.keys.sortedDescending()) { date ->
                        val dayItems = dailySummary.filter { it.date == date }
                        val dayIn = dayItems.find { it.transactionType == TransactionType.STOCK_IN }?.totalQuantity ?: 0
                        val dayOut = dayItems.find { it.transactionType == TransactionType.STOCK_OUT }?.totalQuantity ?: 0
                        val dayValue = dayItems.sumOf { it.totalValue }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    SummaryItem("In", dayIn.toString(), TransactionColors.StockIn, Icons.Default.ArrowDownward)
                                    SummaryItem("Out", dayOut.toString(), TransactionColors.StockOut, Icons.Default.ArrowUpward)
                                    SummaryItem("Value", "\$${"%.2f".format(dayValue)}", MaterialTheme.colorScheme.primary, Icons.Default.AttachMoney)
                                }
                            }
                        }
                    }
                }
            }

            // Transaction List
            Box(modifier = Modifier.fillMaxSize()) {
                if (transactionsByDateRange.isEmpty()) {
                    com.stockmanager.app.ui.components.CommonComponents.EmptyState(
                        icon = Icons.Default.ReceiptLong,
                        title = "No Transactions",
                        message = "No transactions found for the selected period",
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactionsByDateRange) { transaction ->
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
    }
}

@Composable
fun DateRangeChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Chip(
        onClick = onClick,
        modifier = modifier,
        colors = androidx.compose.material3.ChipDefaults.chipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun TypeFilterChip(
    label: String,
    type: TransactionType?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = type?.let { getTypeColor(it) } ?: MaterialTheme.colorScheme.primary

    Chip(
        onClick = onClick,
        selected = selected,
        onCloseRequest = null,
        modifier = modifier,
        colors = androidx.compose.material3.ChipDefaults.chipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedContentColor = color,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        if (type != null) {
            Icon(
                imageVector = getTypeIcon(type),
                contentDescription = null,
                tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp).padding(end = 4.dp)
            )
        }
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color)
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
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

fun getTypeColor(type: TransactionType): Color = when (type) {
    TransactionType.STOCK_IN -> TransactionColors.StockIn
    TransactionType.STOCK_OUT -> TransactionColors.StockOut
    TransactionType.RETURN -> TransactionColors.Return
    TransactionType.ADJUSTMENT -> TransactionColors.Adjustment
    TransactionType.TRANSFER -> TransactionColors.Transfer
}

fun getTypeIcon(type: TransactionType): androidx.compose.ui.graphics.vector.ImageVector = when (type) {
    TransactionType.STOCK_IN -> Icons.Default.ArrowDownward
    TransactionType.STOCK_OUT -> Icons.Default.ArrowUpward
    TransactionType.RETURN -> Icons.Default.Undo
    TransactionType.ADJUSTMENT -> Icons.Default.Tune
    TransactionType.TRANSFER -> Icons.Default.SwapHoriz
}