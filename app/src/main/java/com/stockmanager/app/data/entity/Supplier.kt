package com.stockmanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Entity(
    tableName = "suppliers",
    indices = [
        Index(value = ["shopId", "name"]),
        Index(value = ["shopId", "email"], unique = true),
        Index(value = ["shopId", "phone"]),
        Index(value = ["shopId", "isActive"]),
        Index(value = ["remoteId"], unique = true),
        Index(value = ["shopId", "isSynced"]),
    ]
)
@Serializable
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    // ========================================================================
    // SYNC FIELDS
    // ========================================================================
    val shopId: Long = 0,
    val remoteId: String? = null,
    val isSynced: Boolean = false,
    
    // ========================================================================
    // SUPPLIER FIELDS
    // ========================================================================
    val name: String,
    val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
    val taxId: String? = null,           // GST/VAT number
    val paymentTerms: Int = 30,          // Days
    val creditLimit: Double = 0.0,
    val currentBalance: Double = 0.0,
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    val displayName: String
        get() = contactPerson?.let { "$name ($it)" } ?: name

    val fullAddress: String
        get() = listOf(address, city, state, postalCode, country)
            .filterNotNull()
            .filter { it.isNotBlank() }
            .joinToString(", ")
}