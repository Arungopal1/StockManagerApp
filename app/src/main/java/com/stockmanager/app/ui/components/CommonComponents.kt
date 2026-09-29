package com.stockmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Chip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowBack
import androidx.compose.material3.icons.filled.ArrowForward
import androidx.compose.material3.icons.filled.Check
import androidx.compose.material3.icons.filled.Close
import androidx.compose.material3.icons.filled.Delete
import androidx.compose.material3.icons.filled.Edit
import androidx.compose.material3.icons.filled.Error
import androidx.compose.material3.icons.filled.FilterList
import androidx.compose.material3.icons.filled.Info
import androidx.compose.material3.icons.filled.Menu
import androidx.compose.material3.icons.filled.MoreVert
import androidx.compose.material3.icons.filled.Search
import androidx.compose.material3.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.valueOfDp
import com.stockmanager.app.R
import com.stockmanager.app.ui.theme.StockManagerTheme

@Composable
fun StockAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackPress: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable (() -> Unit)? = null
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = title, maxLines = 1) },
        navigationIcon = {
            if (onBackPress != null) {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            } else {
                navigationIcon?.invoke()
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: (() -> Unit)? = null,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var text by remember { mutableStateOf(query) }
    androidx.compose.material3.TextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onQueryChange(newText)
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        singleLine = true,
        placeholder = { Text(text = placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            if (text.isNotBlank()) {
                IconButton(onClick = { text = ""; onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
    )
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Chip(
        onClick = onClick,
        selected = selected,
        onCloseRequest = null,
        modifier = modifier,
        colors = androidx.compose.material3.ChipDefaults.chipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.width(4.dp))
        }
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun StatusChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Chip(
        onClick = {},
        modifier = modifier,
        enabled = false,
        colors = androidx.compose.material3.ChipDefaults.chipColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color,
        )
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ProductCard(
    product: com.stockmanager.app.data.entity.Product,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val stockColor = when {
        product.isOutOfStock -> com.stockmanager.app.ui.theme.StockColors.OutOfStock
        product.isLowStock -> com.stockmanager.app.ui.theme.StockColors.LowStock
        else -> com.stockmanager.app.ui.theme.StockColors.InStock
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        onClick = onClick,
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
                Column {
                    Text(
                        text = product.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Text(
                        text = "SKU: ${product.sku}${product.barcode?.let { " | Barcode: $it" } ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(
                        label = when {
                            product.isOutOfStock -> "Out of Stock"
                            product.isLowStock -> "Low Stock"
                            else -> "In Stock"
                        },
                        color = stockColor
                    )
                    if (onEditClick != null || onDeleteClick != null) {
                        androidx.compose.material3.Menu(
                            onClick = {},
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            if (onEditClick != null) {
                                androidx.compose.material3.MenuItem(
                                    onClick = { onEditClick() },
                                    icon = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                                    text = { Text("Edit") }
                                )
                            }
                            if (onDeleteClick != null) {
                                androidx.compose.material3.MenuItem(
                                    onClick = { onDeleteClick() },
                                    icon = { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) },
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) }
                                )
                            }
                        }
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Stock: ${product.currentStock}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Min: ${product.minStockLevel} | Max: ${product.maxStockLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Cost: $${"%.2f".format(product.purchasePrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Price: $${"%.2f".format(product.sellingPrice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Margin: ${"%.1f".format(product.profitMargin)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (product.profitMargin >= 20) com.stockmanager.app.ui.theme.StockColors.InStock else com.stockmanager.app.ui.theme.StockColors.LowStock
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: com.stockmanager.app.data.entity.StockTransaction,
    productName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeColor = when (transaction.transactionType) {
        com.stockmanager.app.data.entity.TransactionType.STOCK_IN -> com.stockmanager.app.ui.theme.TransactionColors.StockIn
        com.stockmanager.app.data.entity.TransactionType.STOCK_OUT -> com.stockmanager.app.ui.theme.TransactionColors.StockOut
        com.stockmanager.app.data.entity.TransactionType.RETURN -> com.stockmanager.app.ui.theme.TransactionColors.Return
        com.stockmanager.app.data.entity.TransactionType.ADJUSTMENT -> com.stockmanager.app.ui.theme.TransactionColors.Adjustment
        com.stockmanager.app.data.entity.TransactionType.TRANSFER -> com.stockmanager.app.ui.theme.TransactionColors.Transfer
    }

    val typeIcon = when (transaction.transactionType) {
        com.stockmanager.app.data.entity.TransactionType.STOCK_IN -> Icons.Default.ArrowDownward
        com.stockmanager.app.data.entity.TransactionType.STOCK_OUT -> Icons.Default.ArrowUpward
        com.stockmanager.app.data.entity.TransactionType.RETURN -> Icons.Default.Undo
        com.stockmanager.app.data.entity.TransactionType.ADJUSTMENT -> Icons.Default.Tune
        com.stockmanager.app.data.entity.TransactionType.TRANSFER -> Icons.Default.SwapHoriz
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = typeColor
                    )
                }
                androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis
                    )
                    Text(
                        text = transaction.referenceNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.isIncoming) "+" else ""}${transaction.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
                Text(
                    text = "$\"%.2f\".format(transaction.totalValue)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = transaction.transactionDate.formatDateTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    trend: String? = null,
    trendPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color
                    )
                }
            }
            if (trend != null) {
                androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (trendPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (trendPositive) com.stockmanager.app.ui.theme.StockColors.InStock else com.stockmanager.app.ui.theme.StockColors.OutOfStock,
                        modifier = Modifier.size(16.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.width(4.dp))
                    Text(
                        text = trend,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (trendPositive) com.stockmanager.app.ui.theme.StockColors.InStock else com.stockmanager.app.ui.theme.StockColors.OutOfStock
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionText != null && onActionClick != null) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
            Button(onClick = onActionClick) {
                Text(text = actionText)
            }
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
        Text(
            text = "Loading...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
        Text(
            text = "Error",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (onRetry != null) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}

private fun kotlinx.datetime.LocalDateTime.formatDateTime(): String {
    return "${this.month.numberValue}/${this.dayOfMonth}/${this.year} ${this.hour}:${"%02d".format(this.minute)}"
}