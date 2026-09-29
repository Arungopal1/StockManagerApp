package com.stockmanager.app.data.dao

import androidx.room.*
import com.stockmanager.app.data.entity.StockTransaction
import com.stockmanager.app.data.entity.TransactionType
import com.stockmanager.app.data.entity.TransactionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

@Dao
interface StockTransactionDao {

    // ========================================================================
    // EXISTING METHODS (keep all existing)
    // ========================================================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: StockTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<StockTransaction>): List<Long>

    @Update
    suspend fun update(transaction: StockTransaction): Int

    @Delete
    suspend fun delete(transaction: StockTransaction): Int

    @Query("SELECT * FROM stock_transactions WHERE id = :id")
    fun getById(id: Long): Flow<StockTransaction?>

    @Query("SELECT * FROM stock_transactions WHERE referenceNumber = :refNumber")
    fun getByReferenceNumber(refNumber: String): Flow<StockTransaction?>

    @Query("SELECT * FROM stock_transactions WHERE productId = :productId ORDER BY transactionDate DESC")
    fun getByProductId(productId: Long): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE productId = :productId ORDER BY transactionDate DESC LIMIT :limit")
    suspend fun getRecentByProductId(productId: Long, limit: Int): List<StockTransaction>

    @Query("SELECT * FROM stock_transactions WHERE transactionType = :type ORDER BY transactionDate DESC")
    fun getByType(type: TransactionType): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE userId = :userId ORDER BY transactionDate DESC")
    fun getByUserId(userId: Long): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE supplierId = :supplierId ORDER BY transactionDate DESC")
    fun getBySupplierId(supplierId: Long): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE customerId = :customerId ORDER BY transactionDate DESC")
    fun getByCustomerId(customerId: Long): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE status = :status ORDER BY transactionDate DESC")
    fun getByStatus(status: TransactionStatus): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE date(transactionDate) BETWEEN date(:startDate) AND date(:endDate) ORDER BY transactionDate DESC")
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE date(transactionDate) BETWEEN date(:startDate) AND date(:endDate) AND transactionType = :type ORDER BY transactionDate DESC")
    fun getByDateRangeAndType(startDate: LocalDate, endDate: LocalDate, type: TransactionType): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions ORDER BY transactionDate DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions ORDER BY transactionDate DESC")
    fun getAll(): Flow<List<StockTransaction>>

    // Statistics
    @Query("SELECT SUM(quantity) FROM stock_transactions WHERE productId = :productId AND transactionType = 'STOCK_IN'")
    suspend fun getTotalStockIn(productId: Long): Int?

    @Query("SELECT SUM(ABS(quantity)) FROM stock_transactions WHERE productId = :productId AND transactionType = 'STOCK_OUT'")
    suspend fun getTotalStockOut(productId: Long): Int?

    @Query("SELECT COUNT(*) FROM stock_transactions WHERE productId = :productId")
    suspend fun getTransactionCount(productId: Long): Int

    // Daily summary
    @Query("""
        SELECT
            date(transactionDate) as date,
            transactionType,
            COUNT(*) as count,
            SUM(quantity) as totalQuantity,
            SUM(unitCost * quantity) as totalCost,
            SUM(unitPrice * quantity) as totalValue
        FROM stock_transactions
        WHERE date(transactionDate) BETWEEN date(:startDate) AND date(:endDate)
        GROUP BY date(transactionDate), transactionType
        ORDER BY date(transactionDate) DESC, transactionType
    """)
    suspend fun getDailySummary(startDate: LocalDate, endDate: LocalDate): List<DailyTransactionSummary>

    // Monthly summary
    @Query("""
        SELECT
            strftime('%Y-%m', transactionDate) as month,
            transactionType,
            COUNT(*) as count,
            SUM(quantity) as totalQuantity,
            SUM(unitCost * quantity) as totalCost,
            SUM(unitPrice * quantity) as totalValue
        FROM stock_transactions
        WHERE date(transactionDate) BETWEEN date(:startDate) AND date(:endDate)
        GROUP BY strftime('%Y-%m', transactionDate), transactionType
        ORDER BY month DESC, transactionType
    """)
    suspend fun getMonthlySummary(startDate: LocalDate, endDate: LocalDate): List<MonthlyTransactionSummary>

    // Top selling products
    @Query("""
        SELECT
            st.productId,
            p.name,
            p.sku,
            SUM(ABS(st.quantity)) as totalSold,
            SUM(ABS(st.quantity) * st.unitPrice) as totalRevenue
        FROM stock_transactions st
        JOIN products p ON st.productId = p.id
        WHERE st.transactionType = 'STOCK_OUT'
        AND date(st.transactionDate) BETWEEN date(:startDate) AND date(:endDate)
        GROUP BY st.productId, p.name, p.sku
        ORDER BY totalSold DESC
        LIMIT :limit
    """)
    suspend fun getTopSellingProducts(startDate: LocalDate, endDate: LocalDate, limit: Int): List<TopSellingProduct>

    // Stock movement per product
    @Query("""
        SELECT
            st.productId,
            p.name,
            p.sku,
            p.currentStock,
            SUM(CASE WHEN st.transactionType = 'STOCK_IN' THEN st.quantity ELSE 0 END) as totalIn,
            SUM(CASE WHEN st.transactionType = 'STOCK_OUT' THEN ABS(st.quantity) ELSE 0 END) as totalOut,
            SUM(CASE WHEN st.transactionType = 'RETURN' THEN st.quantity ELSE 0 END) as totalReturns,
            SUM(CASE WHEN st.transactionType = 'ADJUSTMENT' THEN st.quantity ELSE 0 END) as totalAdjustments
        FROM stock_transactions st
        JOIN products p ON st.productId = p.id
        WHERE date(st.transactionDate) BETWEEN date(:startDate) AND date(:endDate)
        GROUP BY st.productId, p.name, p.sku, p.currentStock
        ORDER BY totalOut DESC
    """)
    suspend fun getStockMovementReport(startDate: LocalDate, endDate: LocalDate): List<StockMovementReport>

    // Profit report
    @Query("""
        SELECT
            st.productId,
            p.name,
            p.sku,
            SUM(CASE WHEN st.transactionType = 'STOCK_IN' THEN st.quantity * st.unitCost ELSE 0 END) as totalCost,
            SUM(CASE WHEN st.transactionType = 'STOCK_OUT' THEN ABS(st.quantity) * st.unitPrice ELSE 0 END) as totalRevenue,
            SUM(CASE WHEN st.transactionType = 'STOCK_OUT' THEN ABS(st.quantity) * (st.unitPrice - st.unitCost) ELSE 0 END) as totalProfit
        FROM stock_transactions st
        JOIN products p ON st.productId = p.id
        WHERE date(st.transactionDate) BETWEEN date(:startDate) AND date(:endDate)
        AND (st.transactionType = 'STOCK_IN' OR st.transactionType = 'STOCK_OUT')
        GROUP BY st.productId, p.name, p.sku
        ORDER BY totalProfit DESC
    """)
    suspend fun getProfitReport(startDate: LocalDate, endDate: LocalDate): List<ProfitReportItem>

    // ========================================================================
    // SYNC EXTENSIONS (Add these new methods)
    // ========================================================================

    // Get transactions that need to be pushed to server
    @Query("SELECT * FROM stock_transactions WHERE remoteId IS NOT NULL AND (isSynced = 0 OR isSynced IS NULL) ORDER BY transactionDate ASC")
    suspend fun getUnsyncedTransactions(): List<StockTransaction>

    // Get transaction by Supabase remote ID
    @Query("SELECT * FROM stock_transactions WHERE remoteId = :remoteId")
    suspend fun getByRemoteIdSync(remoteId: String): StockTransaction?

    @Query("SELECT * FROM stock_transactions WHERE remoteId = :remoteId")
    fun getByRemoteId(remoteId: String): Flow<StockTransaction?>

    // Mark transaction as synced
    @Query("UPDATE stock_transactions SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    // Upsert from remote (server wins on conflict)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFromRemote(transaction: StockTransaction): Long

    // Bulk upsert from remote
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllFromRemote(transactions: List<StockTransaction>): List<Long>

    // Get transactions by shop_id
    @Query("SELECT * FROM stock_transactions WHERE shopId = :shopId ORDER BY transactionDate DESC")
    fun getByShopId(shopId: Long): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE shopId = :shopId ORDER BY transactionDate DESC")
    suspend fun getByShopIdSync(shopId: Long): List<StockTransaction>

    // Delete all transactions for a shop
    @Query("DELETE FROM stock_transactions WHERE shopId = :shopId")
    suspend fun deleteByShopId(shopId: Long): Int

    // Count unsynced
    @Query("SELECT COUNT(*) FROM stock_transactions WHERE remoteId IS NOT NULL AND (isSynced = 0 OR isSynced IS NULL)")
    suspend fun countUnsynced(): Int
}

data class DailyTransactionSummary(
    val date: String,
    val transactionType: TransactionType,
    val count: Int,
    val totalQuantity: Int,
    val totalCost: Double,
    val totalValue: Double
)

data class MonthlyTransactionSummary(
    val month: String,
    val transactionType: TransactionType,
    val count: Int,
    val totalQuantity: Int,
    val totalCost: Double,
    val totalValue: Double
)

data class TopSellingProduct(
    val productId: Long,
    val name: String,
    val sku: String,
    val totalSold: Int,
    val totalRevenue: Double
)

data class StockMovementReport(
    val productId: Long,
    val name: String,
    val sku: String,
    val currentStock: Int,
    val totalIn: Int,
    val totalOut: Int,
    val totalReturns: Int,
    val totalAdjustments: Int
)

data class ProfitReportItem(
    val productId: Long,
    val name: String,
    val sku: String,
    val totalCost: Double,
    val totalRevenue: Double,
    val totalProfit: Double
)