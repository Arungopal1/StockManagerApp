package com.stockmanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Entity(
    tableName = "stock_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        ),
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["shopId", "transactionType"]),
        Index(value = ["shopId", "transactionDate"]),
        Index(value = ["referenceNumber"], unique = true),
        Index(value = ["userId"]),
        Index(value = ["remoteId"], unique = true),
        Index(value = ["shopId", "isSynced"]),
    ]
)
@Serializable
data class StockTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    // ========================================================================
    // SYNC FIELDS (Multi-tenant + Supabase)
    // ========================================================================
    val shopId: Long = 0,                           // Local shop ID
    val remoteId: String? = null,                   // Supabase UUID
    val isSynced: Boolean = false,                  // Has been pushed to server
    
    // Remote references (Supabase UUIDs)
    val productRemoteId: String? = null,            // Product Supabase UUID
    val supplierRemoteId: String? = null,           // Supplier Supabase UUID
    val customerRemoteId: String? = null,           // Customer Supabase UUID
    val relatedTransactionRemoteId: String? = null, // Related transaction Supabase UUID
    
    // ========================================================================
    // TRANSACTION FIELDS
    // ========================================================================
    val productId: Long,                            // Reference to local product
    val transactionType: TransactionType,           // IN, OUT, ADJUSTMENT, RETURN, TRANSFER
    val quantity: Int,                              // Quantity (positive for IN, negative for OUT)
    val unitCost: Double,                           // Cost per unit at time of transaction
    val unitPrice: Double,                          // Selling price per unit at time of transaction
    val transactionDate: LocalDateTime = LocalDateTime.now(),
    val referenceNumber: String,                    // Unique transaction reference (e.g., PO-2024-001)
    val supplierId: Long? = null,                   // Supplier for IN transactions (local ID)
    val customerId: Long? = null,                   // Customer for OUT transactions (local ID)
    val userId: Long,                               // User who performed transaction
    val notes: String? = null,                      // Additional notes
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val relatedTransactionId: Long? = null,         // For returns/adjustments linking to original (local ID)
    val locationFrom: String? = null,               // Source location (for transfers)
    val locationTo: String? = null,                 // Destination location (for transfers)
) {
    companion object {
        fun generateReferenceNumber(type: TransactionType): String {
            val prefix = when (type) {
                TransactionType.STOCK_IN -> "GRN"   // Goods Received Note
                TransactionType.STOCK_OUT -> "DN"   // Delivery Note
                TransactionType.RETURN -> "RTN"
                TransactionType.ADJUSTMENT -> "ADJ"
                TransactionType.TRANSFER -> "TRF"
            }
            val timestamp = System.currentTimeMillis().toString().takeLast(6)
            return "$prefix-${LocalDateTime.now().year}-$timestamp"
        }
    }

    val totalCost: Double
        get() = quantity * unitCost

    val totalValue: Double
        get() = quantity * unitPrice

    val isIncoming: Boolean
        get() = transactionType == TransactionType.STOCK_IN || transactionType == TransactionType.RETURN

    val isOutgoing: Boolean
        get() = transactionType == TransactionType.STOCK_OUT || transactionType == TransactionType.TRANSFER
}

enum class TransactionType(val displayName: String, val icon: String, val color: Int) {
    STOCK_IN("Stock In", "inventory", 0xFF4CAF50),        // Green
    STOCK_OUT("Stock Out", "shopping_cart", 0xFFF44336),  // Red
    RETURN("Return", "undo", 0xFFFF9800),                 // Orange
    ADJUSTMENT("Adjustment", "tune", 0xFF2196F3),         // Blue
    TRANSFER("Transfer", "swap_horiz", 0xFF9C27B0),       // Purple
}

enum class TransactionStatus(val displayName: String) {
    PENDING("Pending"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    PARTIAL("Partial"),
}