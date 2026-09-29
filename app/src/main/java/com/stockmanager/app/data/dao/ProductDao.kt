package com.stockmanager.app.data.dao

import androidx.room.*
import com.stockmanager.app.data.entity.Product
import com.stockmanager.app.data.entity.ProductCategory
import com.stockmanager.app.data.entity.ProductCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface ProductDao {

    // ========================================================================
    // EXISTING METHODS (keep all existing)
    // ========================================================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>): List<Long>

    @Update
    suspend fun update(product: Product): Int

    @Delete
    suspend fun delete(product: Product): Int

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM products WHERE id = :id")
    fun getById(id: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getByIdSync(id: Long): Product?

    @Query("SELECT * FROM products WHERE sku = :sku")
    fun getBySku(sku: String): Flow<Product?>

    @Query("SELECT * FROM products WHERE barcode = :barcode")
    fun getByBarcode(barcode: String): Flow<Product?>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActive(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND category = :category ORDER BY name ASC")
    fun getByCategory(category: ProductCategory): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND brand = :brand ORDER BY name ASC")
    fun getByBrand(brand: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND currentStock <= minStockLevel ORDER BY currentStock ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND currentStock = 0 ORDER BY name ASC")
    fun getOutOfStockProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND (name LIKE :query OR sku LIKE :query OR barcode LIKE :query OR brand LIKE :query OR model LIKE :query) ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND supplierId = :supplierId ORDER BY name ASC")
    fun getBySupplier(supplierId: Long): Flow<List<Product>>

    // Stock updates
    @Query("UPDATE products SET currentStock = currentStock + :quantity, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun adjustStock(productId: Long, quantity: Int, updatedAt: LocalDateTime): Int

    @Query("UPDATE products SET currentStock = :newStock, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun setStock(productId: Long, newStock: Int, updatedAt: LocalDateTime): Int

    // Statistics
    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1 AND currentStock <= minStockLevel")
    suspend fun getLowStockCount(): Int

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1 AND currentStock = 0")
    suspend fun getOutOfStockCount(): Int

    @Query("SELECT SUM(currentStock * purchasePrice) FROM products WHERE isActive = 1")
    suspend fun getTotalStockValue(): Double?

    @Query("SELECT SUM(currentStock * sellingPrice) FROM products WHERE isActive = 1")
    suspend fun getTotalStockRetailValue(): Double?

    @Query("SELECT DISTINCT brand FROM products WHERE isActive = 1 ORDER BY brand ASC")
    suspend fun getAllBrands(): List<String>

    @Query("SELECT DISTINCT category FROM products WHERE isActive = 1")
    suspend fun getAllCategories(): List<ProductCategory>

    // Advanced search with filters
    @Query("""
        SELECT * FROM products
        WHERE isActive = 1
        AND (:category IS NULL OR category = :category)
        AND (:brand IS NULL OR brand = :brand)
        AND (:condition IS NULL OR condition = :condition)
        AND (:searchQuery IS NULL OR name LIKE :searchQuery OR sku LIKE :searchQuery OR barcode LIKE :searchQuery)
        ORDER BY :orderBy :orderDir
        LIMIT :limit OFFSET :offset
    """)
    fun searchWithFilters(
        category: ProductCategory?,
        brand: String?,
        condition: ProductCondition?,
        searchQuery: String?,
        orderBy: String,
        orderDir: String,
        limit: Int,
        offset: Int
    ): Flow<List<Product>>

    @Query("""
        SELECT COUNT(*) FROM products
        WHERE isActive = 1
        AND (:category IS NULL OR category = :category)
        AND (:brand IS NULL OR brand = :brand)
        AND (:condition IS NULL OR condition = :condition)
        AND (:searchQuery IS NULL OR name LIKE :searchQuery OR sku LIKE :searchQuery OR barcode LIKE :searchQuery)
    """)
    suspend fun countWithFilters(
        category: ProductCategory?,
        brand: String?,
        condition: ProductCondition?,
        searchQuery: String?
    ): Int

    // ========================================================================
    // SYNC EXTENSIONS (Add these new methods)
    // ========================================================================

    // Get products that need to be pushed to server
    @Query("SELECT * FROM products WHERE remoteId IS NOT NULL AND (isSynced = 0 OR isSynced IS NULL) ORDER BY updatedAt ASC")
    suspend fun getUnsyncedProducts(): List<Product>

    // Get product by Supabase remote ID
    @Query("SELECT * FROM products WHERE remoteId = :remoteId")
    suspend fun getByRemoteIdSync(remoteId: String): Product?

    @Query("SELECT * FROM products WHERE remoteId = :remoteId")
    fun getByRemoteId(remoteId: String): Flow<Product?>

    // Mark product as synced
    @Query("UPDATE products SET isSynced = 1, updatedAt = :now WHERE id = :id")
    suspend fun markSynced(id: Long, now: LocalDateTime)

    // Upsert from remote (server wins on conflict)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFromRemote(product: Product): Long

    // Bulk upsert from remote
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllFromRemote(products: List<Product>): List<Long>

    // Get products by shop_id (for multi-shop queries)
    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY name ASC")
    fun getByShopId(shopId: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY name ASC")
    suspend fun getByShopIdSync(shopId: Long): List<Product>

    // Delete all products for a shop (on logout/switch shop)
    @Query("DELETE FROM products WHERE shopId = :shopId")
    suspend fun deleteByShopId(shopId: Long): Int

    // Count unsynced
    @Query("SELECT COUNT(*) FROM products WHERE remoteId IS NOT NULL AND (isSynced = 0 OR isSynced IS NULL)")
    suspend fun countUnsynced(): Int
}