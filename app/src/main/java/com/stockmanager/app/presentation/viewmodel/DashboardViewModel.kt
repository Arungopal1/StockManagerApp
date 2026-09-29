package com.stockmanager.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockmanager.app.data.entity.Product
import com.stockmanager.app.data.entity.TransactionType
import com.stockmanager.app.data.repository.ProductRepository
import com.stockmanager.app.data.repository.StockTransactionRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val transactionRepository: StockTransactionRepository
) : ViewModel() {

    private val _startDate = MutableStateFlow(LocalDate.now().minusMonths(1))
    private val _endDate = MutableStateFlow(LocalDate.now())

    val startDate: LocalDate
        get() = _startDate.value
        set(value) { _startDate.value = value }

    val endDate: LocalDate
        get() = _endDate.value
        set(value) { _endDate.value = value }

    // Dashboard stats
    val totalProducts = productRepository.getAllActive()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lowStockProducts = productRepository.getAllActive()
        .map { it.filter { it.isLowStock }.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val outOfStockProducts = productRepository.getAllActive()
        .map { it.filter { it.isOutOfStock }.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStockValue = productRepository.getAllActive()
        .map { it.sumOf { it.currentStock * it.purchasePrice } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalRetailValue = productRepository.getAllActive()
        .map { it.sumOf { it.currentStock * it.sellingPrice } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val potentialProfit = combine(
        totalStockValue,
        totalRetailValue
    ) { cost, retail -> retail - cost }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Recent transactions
    val recentTransactions = transactionRepository.getRecent(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Low stock items
    val lowStockItems = productRepository.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Top selling products this month
    val topSellingThisMonth = combine(
        _startDate,
        _endDate
    ) { start, end ->
        transactionRepository.getTopSellingProducts(start, end, 5)
    }.flattenMerge().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily summary for chart
    val dailySummary = combine(
        _startDate,
        _endDate
    ) { start, end ->
        transactionRepository.getDailySummary(start, end)
    }.flattenMerge().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly comparison
    val monthlyComparison = combine(
        _startDate,
        _endDate
    ) { start, end ->
        transactionRepository.getMonthlySummary(start, end)
    }.flattenMerge().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDateRange(start: LocalDate, end: LocalDate) {
        _startDate.value = start
        _endDate.value = end
    }

    fun setThisMonth() {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        setDateRange(startOfMonth, today)
    }

    fun setLast30Days() {
        val today = LocalDate.now()
        setDateRange(today.minusDays(30), today)
    }

    fun setLast90Days() {
        val today = LocalDate.now()
        setDateRange(today.minusDays(90), today)
    }
}