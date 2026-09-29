package com.stockmanager.app.data.repository

import com.stockmanager.app.data.dao.StockTransactionDao
import com.stockmanager.app.data.entity.StockTransaction
import com.stockmanager.app.data.entity.TransactionType
import com.stockmanager.app.data.entity.TransactionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockTransactionRepository @Inject constructor(
    private val transactionDao: StockTransactionDao
) {

    // Basic CRUD
    suspend fun insert(transaction: StockTransaction): Long = transactionDao.insert(transaction)

    suspend fun insertAll(transactions: List<StockTransaction>): List<Long> = transactionDao.insertAll(transactions)

    suspend fun update(transaction: StockTransaction): Int = transactionDao.update(transaction)

    suspend fun delete(transaction: StockTransaction): Int = transactionDao.delete(transaction)

    // Queries
    fun getById(id: Long): Flow<StockTransaction?> = transactionDao.getById(id)

    fun getByReferenceNumber(refNumber: String): Flow<StockTransaction?> = transactionDao.getByReferenceNumber(refNumber)

    fun getByProductId(productId: Long): Flow<List<StockTransaction>> = transactionDao.getByProductId(productId)

    suspend fun getRecentByProductId(productId: Long, limit: Int): List<StockTransaction> =
        transactionDao.getRecentByProductId(productId, limit)

    fun getByType(type: TransactionType): Flow<List<StockTransaction>> = transactionDao.getByType(type)

    fun getByUserId(userId: Long): Flow<List<StockTransaction>> = transactionDao.getByUserId(userId)

    fun getBySupplierId(supplierId: Long): Flow<List<StockTransaction>> = transactionDao.getBySupplierId(supplierId)

    fun getByCustomerId(customerId: Long): Flow<List<StockTransaction>> = transactionDao.getByCustomerId(customerId)

    fun getByStatus(status: TransactionStatus): Flow<List<StockTransaction>> = transactionDao.getByStatus(status)

    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<StockTransaction>> =
        transactionDao.getByDateRange(startDate, endDate)

    fun getByDateRangeAndType(startDate: LocalDate, endDate: LocalDate, type: TransactionType): Flow<List<StockTransaction>> =
        transactionDao.getByDateRangeAndType(startDate, endDate, type)

    fun getRecent(limit: Int): Flow<List<StockTransaction>> = transactionDao.getRecent(limit)

    fun getAll(): Flow<List<StockTransaction>> = transactionDao.getAll()

    // Statistics
    suspend fun getTotalStockIn(productId: Long): Int = transactionDao.getTotalStockIn(productId) ?: 0

    suspend fun getTotalStockOut(productId: Long): Int = transactionDao.getTotalStockOut(productId) ?: 0

    suspend fun getTransactionCount(productId: Long): Int = transactionDao.getTransactionCount(productId)

    // Reports
    suspend fun getDailySummary(startDate: LocalDate, endDate: LocalDate): List<DailyTransactionSummary> =
        transactionDao.getDailySummary(startDate, endDate)

    suspend fun getMonthlySummary(startDate: LocalDate, endDate: LocalDate): List<MonthlyTransactionSummary> =
        transactionDao.getMonthlySummary(startDate, endDate)

    suspend fun getTopSellingProducts(startDate: LocalDate, endDate: LocalDate, limit: Int): List<TopSellingProduct> =
        transactionDao.getTopSellingProducts(startDate, endDate, limit)

    suspend fun getStockMovementReport(startDate: LocalDate, endDate: LocalDate): List<StockMovementReport> =
        transactionDao.getStockMovementReport(startDate, endDate)

    suspend fun getProfitReport(startDate: LocalDate, endDate: LocalDate): List<ProfitReportItem> =
        transactionDao.getProfitReport(startDate, endDate)

    // Convenience methods for creating transactions
    suspend fun recordStockIn(
        productId: Long,
        quantity: Int,
        unitCost: Double,
        unitPrice: Double,
        supplierId: Long?,
        userId: Long,
        notes: String? = null
    ): Long {
        val transaction = StockTransaction(
            productId = productId,
            transactionType = TransactionType.STOCK_IN,
            quantity = quantity,
            unitCost = unitCost,
            unitPrice = unitPrice,
            referenceNumber = StockTransaction.generateReferenceNumber(TransactionType.STOCK_IN),
            supplierId = supplierId,
            userId = userId,
            notes = notes,
        )
        return insert(transaction)
    }

    suspend fun recordStockOut(
        productId: Long,
        quantity: Int,
        unitCost: Double,
        unitPrice: Double,
        customerId: Long?,
        userId: Long,
        notes: String? = null
    ): Long {
        val transaction = StockTransaction(
            productId = productId,
            transactionType = TransactionType.STOCK_OUT,
            quantity = -quantity,
            unitCost = unitCost,
            unitPrice = unitPrice,
            referenceNumber = StockTransaction.generateReferenceNumber(TransactionType.STOCK_OUT),
            customerId = customerId,
            userId = userId,
            notes = notes,
        )
        return insert(transaction)
    }

    suspend fun recordReturn(
        productId: Long,
        quantity: Int,
        unitCost: Double,
        unitPrice: Double,
        customerId: Long?,
        userId: Long,
        relatedTransactionId: Long?,
        notes: String? = null
    ): Long {
        val transaction = StockTransaction(
            productId = productId,
            transactionType = TransactionType.RETURN,
            quantity = quantity,
            unitCost = unitCost,
            unitPrice = unitPrice,
            referenceNumber = StockTransaction.generateReferenceNumber(TransactionType.RETURN),
            customerId = customerId,
            userId = userId,
            relatedTransactionId = relatedTransactionId,
            notes = notes,
        )
        return insert(transaction)
    }

    suspend fun recordAdjustment(
        productId: Long,
        quantity: Int, // Can be positive or negative
        unitCost: Double,
        unitPrice: Double,
        userId: Long,
        notes: String? = null
    ): Long {
        val transaction = StockTransaction(
            productId = productId,
            transactionType = TransactionType.ADJUSTMENT,
            quantity = quantity,
            unitCost = unitCost,
            unitPrice = unitPrice,
            referenceNumber = StockTransaction.generateReferenceNumber(TransactionType.ADJUSTMENT),
            userId = userId,
            notes = notes,
        )
        return insert(transaction)
    }

    suspend fun recordTransfer(
        productId: Long,
        quantity: Int,
        unitCost: Double,
        unitPrice: Double,
        locationFrom: String?,
        locationTo: String?,
        userId: Long,
        notes: String? = null
    ): Long {
        val transaction = StockTransaction(
            productId = productId,
            transactionType = TransactionType.TRANSFER,
            quantity = -quantity,
            unitCost = unitCost,
            unitPrice = unitPrice,
            referenceNumber = StockTransaction.generateReferenceNumber(TransactionType.TRANSFER),
            userId = userId,
            locationFrom = locationFrom,
            locationTo = locationTo,
            notes = notes,
        )
        return insert(transaction)
    }
}

// Re-export data classes for convenience
typealias DailyTransactionSummary = com.stockmanager.app.data.dao.DailyTransactionSummary
typealias MonthlyTransactionSummary = com.stockmanager.app.data.dao.MonthlyTransactionSummary
typealias TopSellingProduct = com.stockmanager.app.data.dao.TopSellingProduct
typealias StockMovementReport = com.stockmanager.app.data.dao.StockMovementReport
typealias ProfitReportItem = com.stockmanager.app.data.dao.ProfitReportItem