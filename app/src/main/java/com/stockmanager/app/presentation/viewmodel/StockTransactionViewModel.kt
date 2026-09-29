package com.stockmanager.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockmanager.app.data.entity.StockTransaction
import com.stockmanager.app.data.entity.TransactionType
import com.stockmanager.app.data.repository.StockTransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class StockTransactionViewModel @Inject constructor(
    private val transactionRepository: StockTransactionRepository
) : ViewModel() {

    // UI State
    private val _selectedType = MutableStateFlow<TransactionType?>(null)
    private val _startDate = MutableStateFlow(LocalDate.now().minusMonths(1))
    private val _endDate = MutableStateFlow(LocalDate.now())
    private val _productId = MutableStateFlow<Long?>(null)
    private val _userId = MutableStateFlow<Long?>(null)

    val selectedType: TransactionType?
        get() = _selectedType.value
        set(value) { _selectedType.value = value }

    val startDate: LocalDate
        get() = _startDate.value
        set(value) { _startDate.value = value }

    val endDate: LocalDate
        get() = _endDate.value
        set(value) { _endDate.value = value }

    // Transaction lists
    val recentTransactions = transactionRepository.getRecent(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionsByType = combine(
        transactionRepository.getAll(),
        _selectedType
    ) { transactions, type ->
        if (type == null) transactions else transactions.filter { it.transactionType == type }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionsByDateRange = combine(
        transactionRepository.getAll(),
        _startDate,
        _endDate
    ) { transactions, start, end ->
        transactions.filter { tx ->
            val txDate = tx.transactionDate.toLocalDate()
            txDate >= start && txDate <= end
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionsByProduct = _productId.map { productId ->
        if (productId == null) emptyList<StockTransaction>() else transactionRepository.getByProductId(productId)
    }.flattenMerge().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reports
    val dailySummary = combine(
        _startDate,
        _endDate
    ) { start, end ->
        transactionRepository.getDailySummary(start, end)
    }.flattenMerge().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySummary = combine(
        _startDate,
        _endDate
    ) { start, end ->
        transactionRepository.getMonthlySummary(start, end)
    }.flattenMerge().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topSellingProducts = combine(
        _startDate,
        _endDate
    ) { start, end ->
        transactionRepository.getTopSellingProducts(start, end, 10)
    }.flattenMerge().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockMovementReport = combine(
        _startDate,
        _endDate
    ) { start, end ->
        transactionRepository.getStockMovementReport(start, end)
    }.flattenMerge().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profitReport = combine(
        _startDate,
        _endDate
    ) { start, end ->
        transactionRepository.getProfitReport(start, end)
    }.flattenMerge().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setType(type: TransactionType?) {
        _selectedType.value = type
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        _startDate.value = start
        _endDate.value = end
    }

    fun setProductId(productId: Long?) {
        _productId.value = productId
    }

    fun setUserId(userId: Long?) {
        _userId.value = userId
    }

    // Quick date range presets
    fun setToday() {
        val today = LocalDate.now()
        setDateRange(today, today)
    }

    fun setThisWeek() {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.ordinal)
        setDateRange(startOfWeek, today)
    }

    fun setThisMonth() {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        setDateRange(startOfMonth, today)
    }

    fun setLastMonth() {
        val today = LocalDate.now()
        val lastMonth = today.minusMonths(1)
        val startOfMonth = lastMonth.withDayOfMonth(1)
        val endOfMonth = today.withDayOfMonth(1).minusDays(1)
        setDateRange(startOfMonth, endOfMonth)
    }

    // Record transactions
    suspend fun recordStockIn(
        productId: Long,
        quantity: Int,
        unitCost: Double,
        unitPrice: Double,
        supplierId: Long?,
        userId: Long,
        notes: String? = null
    ): Long = transactionRepository.recordStockIn(
        productId, quantity, unitCost, unitPrice, supplierId, userId, notes
    )

    suspend fun recordStockOut(
        productId: Long,
        quantity: Int,
        unitCost: Double,
        unitPrice: Double,
        customerId: Long?,
        userId: Long,
        notes: String? = null
    ): Long = transactionRepository.recordStockOut(
        productId, quantity, unitCost, unitPrice, customerId, userId, notes
    )

    suspend fun recordReturn(
        productId: Long,
        quantity: Int,
        unitCost: Double,
        unitPrice: Double,
        customerId: Long?,
        userId: Long,
        relatedTransactionId: Long?,
        notes: String? = null
    ): Long = transactionRepository.recordReturn(
        productId, quantity, unitCost, unitPrice, customerId, userId, relatedTransactionId, notes
    )

    suspend fun recordAdjustment(
        productId: Long,
        quantity: Int,
        unitCost: Double,
        unitPrice: Double,
        userId: Long,
        notes: String? = null
    ): Long = transactionRepository.recordAdjustment(
        productId, quantity, unitCost, unitPrice, userId, notes
    )

    suspend fun recordTransfer(
        productId: Long,
        quantity: Int,
        unitCost: Double,
        unitPrice: Double,
        locationFrom: String?,
        locationTo: String?,
        userId: Long,
        notes: String? = null
    ): Long = transactionRepository.recordTransfer(
        productId, quantity, unitCost, unitPrice, locationFrom, locationTo, userId, notes
    )
}