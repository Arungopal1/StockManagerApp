package com.stockmanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["shopId", "name"]),
        Index(value = ["shopId", "phone"], unique = true),
        Index(value = ["shopId", "email"], unique = true),
        Index(value = ["shopId", "isActive"]),
        Index(value = ["remoteId"], unique = true),
        Index(value = ["shopId", "isSynced"]),
    ]
)
@Serializable
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    // ========================================================================
    // SYNC FIELDS
    // ========================================================================
    val shopId: Long = 0,
    val remoteId: String? = null,
    val isSynced: Boolean = false,
    
    // ========================================================================
    // CUSTOMER FIELDS
    // ========================================================================
    val name: String,
    val phone: String,
    val email: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val gstNumber: String? = null,
    val customerType: CustomerType = CustomerType.RETAIL,
    val creditLimit: Double = 0.0,
    val currentBalance: Double = 0.0,
    val loyaltyPoints: Int = 0,
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    val displayName: String
        get() = "$name${if (phone.isNotBlank()) " - $phone" else ""}"

    val fullAddress: String
        get() = listOf(address, city, state, postalCode, country)
            .filterNotNull()
            .filter { it.isNotBlank() }
            .joinToString(", ")
}

enum class CustomerType(val displayName: String) {
    RETAIL("Retail Customer"),
    WHOLESALE("Wholesale"),
    CORPORATE("Corporate"),
    ONLINE("Online"),
    WALK_IN("Walk-in"),
}