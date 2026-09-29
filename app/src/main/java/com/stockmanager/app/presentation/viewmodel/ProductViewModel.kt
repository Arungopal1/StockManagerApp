package com.stockmanager.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockmanager.app.data.entity.Product
import com.stockmanager.app.data.entity.ProductCategory
import com.stockmanager.app.data.entity.ProductCondition
import com.stockmanager.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    // UI State
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<ProductCategory?>(null)
    private val _selectedBrand = MutableStateFlow<String?>(null)
    private val _selectedCondition = MutableStateFlow<ProductCondition?>(null)
    private val _sortBy = MutableStateFlow("name")
    private val _sortOrder = MutableStateFlow("ASC")
    private val _currentPage = MutableStateFlow(0)
    private val _pageSize = MutableStateFlow(50)

    val searchQuery: String
        get() = _searchQuery.value
        set(value) { _searchQuery.value = value }

    val selectedCategory: ProductCategory?
        get() = _selectedCategory.value
        set(value) { _selectedCategory.value = value }

    val selectedBrand: String?
        get() = _selectedBrand.value
        set(value) { _selectedBrand.value = value }

    val selectedCondition: ProductCondition?
        get() = _selectedCondition.value
        set(value) { _selectedCondition.value = value }

    // Filtered products
    val products = productRepository.searchWithFilters(
        category = _selectedCategory,
        brand = _selectedBrand,
        condition = _selectedCondition,
        searchQuery = _searchQuery.map { if (it.isBlank()) null else it },
        orderBy = _sortBy,
        orderDir = _sortOrder,
        limit = _pageSize,
        offset = combine(_currentPage, _pageSize) { page, size -> page * size }
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Statistics
    val activeCount = productRepository.getAllActive()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lowStockCount = productRepository.getAllActive()
        .map { it.count { it.isLowStock } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val outOfStockCount = productRepository.getAllActive()
        .map { it.count { it.isOutOfStock } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStockValue = productRepository.getAllActive()
        .map { it.sumOf { it.currentStock * it.purchasePrice } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalRetailValue = productRepository.getAllActive()
        .map { it.sumOf { it.currentStock * it.sellingPrice } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val allBrands = productRepository.getAllActive()
        .map { it.map { it.brand }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _currentPage.value = 0
    }

    fun setCategory(category: ProductCategory?) {
        _selectedCategory.value = category
        _currentPage.value = 0
    }

    fun setBrand(brand: String?) {
        _selectedBrand.value = brand
        _currentPage.value = 0
    }

    fun setCondition(condition: ProductCondition?) {
        _selectedCondition.value = condition
        _currentPage.value = 0
    }

    fun setSortBy(sortBy: String) {
        _sortBy.value = sortBy
    }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == "ASC") "DESC" else "ASC"
    }

    fun nextPage() {
        _currentPage.value++
    }

    fun previousPage() {
        if (_currentPage.value > 0) _currentPage.value--
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _selectedBrand.value = null
        _selectedCondition.value = null
        _currentPage.value = 0
    }

    // Product CRUD
    suspend fun createProduct(product: Product): Long = productRepository.insert(product)

    suspend fun updateProduct(product: Product) = productRepository.update(product)

    suspend fun deleteProduct(product: Product) = productRepository.delete(product)

    suspend fun adjustStock(productId: Long, quantity: Int) = productRepository.adjustStock(productId, quantity)

    suspend fun recordStockIn(productId: Long, quantity: Int) = productRepository.recordStockIn(productId, quantity)

    suspend fun recordStockOut(productId: Long, quantity: Int) = productRepository.recordStockOut(productId, quantity)
}