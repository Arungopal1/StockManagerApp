package com.stockmanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["shopId", "sku"], unique = true),
        Index(value = ["shopId", "barcode"], unique = true),
        Index(value = ["shopId", "name"]),
        Index(value = ["shopId", "category"]),
        Index(value = ["shopId", "brand"]),
        Index(value = ["remoteId"], unique = true),
        Index(value = ["shopId", "isSynced"]),
    ]
)
@Serializable
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    // ========================================================================
    // SYNC FIELDS (Multi-tenant + Supabase)
    // ========================================================================
    val shopId: Long = 0,                      // Local shop ID (for multi-shop support)
    val remoteId: String? = null,              // Supabase UUID (e.g., "550e8400-e29b-41d4-a716-446655440000")
    val isSynced: Boolean = false,             // Has been pushed to server
    
    // ========================================================================
    // PRODUCT FIELDS
    // ========================================================================
    val sku: String,                           // Unique Stock Keeping Unit per shop
    val barcode: String? = null,               // IMEI/Barcode for scanning
    val name: String,                          // Product name (e.g., "iPhone 15 Pro")
    val brand: String,                         // Brand (Apple, Samsung, etc.)
    val model: String,                         // Model variant
    val category: ProductCategory,             // Phone, Accessory, Part, etc.
    val color: String,                         // Color variant
    val storage: String,                       // Storage capacity (128GB, 256GB, etc.)
    val condition: ProductCondition,           // New, Refurbished, Used
    val purchasePrice: Double,                 // Cost price
    val sellingPrice: Double,                  // Selling price
    val currentStock: Int = 0,                 // Current quantity in stock
    val minStockLevel: Int = 5,                // Minimum stock alert threshold
    val maxStockLevel: Int = 100,              // Maximum stock capacity
    val location: String? = null,              // Shelf/Location in store
    val supplierId: Long? = null,              // Supplier reference (local ID)
    val supplierRemoteId: String? = null,      // Supplier Supabase UUID
    val warrantyMonths: Int = 12,              // Warranty period
    val description: String? = null,           // Additional notes
    val imageUrl: String? = null,              // Product image URL/path
    val isActive: Boolean = true,              // Soft delete flag
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            sku: String,
            name: String,
            brand: String,
            model: String,
            category: ProductCategory,
            color: String,
            storage: String,
            condition: ProductCondition,
            purchasePrice: Double,
            sellingPrice: Double,
            shopId: Long = 0,
        ): Product {
            return Product(
                shopId = shopId,
                sku = sku,
                name = name,
                brand = brand,
                model = model,
                category = category,
                color = color,
                storage = storage,
                condition = condition,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
            )
        }
        
        // Create from Supabase remote data
        fun fromRemote(
            remoteId: String,
            shopId: Long,
            sku: String,
            name: String,
            brand: String,
            model: String,
            category: ProductCategory,
            color: String,
            storage: String,
            condition: ProductCondition,
            purchasePrice: Double,
            sellingPrice: Double,
            currentStock: Int,
            minStockLevel: Int,
            maxStockLevel: Int,
            location: String?,
            supplierRemoteId: String?,
            warrantyMonths: Int,
            description: String?,
            imageUrl: String?,
            isActive: Boolean,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
        ): Product {
            return Product(
                remoteId = remoteId,
                shopId = shopId,
                sku = sku,
                name = name,
                brand = brand,
                model = model,
                category = category,
                color = color,
                storage = storage,
                condition = condition,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                currentStock = currentStock,
                minStockLevel = minStockLevel,
                maxStockLevel = maxStockLevel,
                location = location,
                supplierRemoteId = supplierRemoteId,
                warrantyMonths = warrantyMonths,
                description = description,
                imageUrl = imageUrl,
                isActive = isActive,
                createdAt = createdAt,
                updatedAt = updatedAt,
                isSynced = true,
            )
        }
    }

    val profitMargin: Double
        get() = if (purchasePrice > 0) ((sellingPrice - purchasePrice) / purchasePrice * 100) else 0.0

    val isLowStock: Boolean
        get() = currentStock <= minStockLevel

    val isOutOfStock: Boolean
        get() = currentStock == 0

    val displayName: String
        get() = "$brand $model $storage $color".trim()
}

enum class ProductCategory(val displayName: String, val icon: String) {
    SMARTPHONE("Smartphone", "phone"),
    TABLET("Tablet", "tablet"),
    LAPTOP("Laptop", "laptop"),
    ACCESSORY("Accessory", "headphones"),
    PART("Spare Part", "build"),
    CASE("Case/Cover", "shield"),
    CHARGER("Charger", "battery"),
    AUDIO("Audio", "music_note"),
    WEARABLE("Wearable", "watch"),
    OTHER("Other", "category"),
}

enum class ProductCondition(val displayName: String) {
    NEW("New"),
    REFURBISHED("Refurbished"),
    USED_GOOD("Used - Good"),
    USED_FAIR("Used - Fair"),
    USED_POOR("Used - Poor"),
    FOR_PARTS("For Parts Only"),
}