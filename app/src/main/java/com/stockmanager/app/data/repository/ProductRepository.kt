package com.stockmanager.app.data.repository

import com.stockmanager.app.data.dao.ProductDao
import com.stockmanager.app.data.entity.Product
import com.stockmanager.app.data.entity.ProductCategory
import com.stockmanager.app.data.entity.ProductCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {

    // Basic CRUD
    suspend fun insert(product: Product): Long = productDao.insert(product)

    suspend fun insertAll(products: List<Product>): List<Long> = productDao.insertAll(products)

    suspend fun update(product: Product): Int = productDao.update(product.copy(updatedAt = LocalDateTime.now()))

    suspend fun delete(product: Product): Int = productDao.delete(product)

    suspend fun deleteById(id: Long): Int = productDao.deleteById(id)

    // Queries
    fun getById(id: Long): Flow<Product?> = productDao.getById(id)

    suspend fun getByIdSync(id: Long): Product? = productDao.getByIdSync(id)

    fun getBySku(sku: String): Flow<Product?> = productDao.getBySku(sku)

    fun getByBarcode(barcode: String): Flow<Product?> = productDao.getByBarcode(barcode)

    fun getAllActive(): Flow<List<Product>> = productDao.getAllActive()

    fun getAll(): Flow<List<Product>> = productDao.getAll()

    fun getByCategory(category: ProductCategory): Flow<List<Product>> = productDao.getByCategory(category)

    fun getByBrand(brand: String): Flow<List<Product>> = productDao.getByBrand(brand)

    fun getLowStockProducts(): Flow<List<Product>> = productDao.getLowStockProducts()

    fun getOutOfStockProducts(): Flow<List<Product>> = productDao.getOutOfStockProducts()

    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts("%$query%")

    fun getBySupplier(supplierId: Long): Flow<List<Product>> = productDao.getBySupplier(supplierId)

    // Stock management
    suspend fun adjustStock(productId: Long, quantity: Int): Int {
        return productDao.adjustStock(productId, quantity, LocalDateTime.now())
    }

    suspend fun setStock(productId: Long, newStock: Int): Int {
        return productDao.setStock(productId, newStock, LocalDateTime.now())
    }

    suspend fun recordStockIn(productId: Long, quantity: Int): Int {
        return adjustStock(productId, quantity)
    }

    suspend fun recordStockOut(productId: Long, quantity: Int): Int {
        return adjustStock(productId, -quantity)
    }

    // Statistics
    suspend fun getActiveCount(): Int = productDao.getActiveCount()

    suspend fun getLowStockCount(): Int = productDao.getLowStockCount()

    suspend fun getOutOfStockCount(): Int = productDao.getOutOfStockCount()

    suspend fun getTotalStockValue(): Double = productDao.getTotalStockValue() ?: 0.0

    suspend fun getTotalStockRetailValue(): Double = productDao.getTotalStockRetailValue() ?: 0.0

    suspend fun getAllBrands(): List<String> = productDao.getAllBrands()

    suspend fun getAllCategories(): List<ProductCategory> = productDao.getAllCategories()

    // Advanced search
    fun searchWithFilters(
        category: ProductCategory? = null,
        brand: String? = null,
        condition: ProductCondition? = null,
        searchQuery: String? = null,
        orderBy: String = "name",
        orderDir: String = "ASC",
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<Product>> = productDao.searchWithFilters(
        category = category,
        brand = brand,
        condition = condition,
        searchQuery = searchQuery?.let { "%$it%" },
        orderBy = orderBy,
        orderDir = orderDir,
        limit = limit,
        offset = offset
    )

    suspend fun countWithFilters(
        category: ProductCategory? = null,
        brand: String? = null,
        condition: ProductCondition? = null,
        searchQuery: String? = null
    ): Int = productDao.countWithFilters(
        category = category,
        brand = brand,
        condition = condition,
        searchQuery = searchQuery?.let { "%$it%" }
    )
}