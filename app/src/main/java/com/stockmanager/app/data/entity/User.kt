package com.stockmanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["shopId", "username"], unique = true),
        Index(value = ["shopId", "email"], unique = true),
        Index(value = ["shopId", "role"]),
        Index(value = ["remoteId"], unique = true),
        Index(value = ["shopId", "isSynced"]),
    ]
)
@Serializable
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    // ========================================================================
    // SYNC FIELDS
    // ========================================================================
    val shopId: Long = 0,
    val remoteId: String? = null,
    val isSynced: Boolean = false,
    
    // ========================================================================
    // USER FIELDS
    // ========================================================================
    val username: String,
    val email: String,
    val fullName: String,
    val passwordHash: String,       // Hashed password (local auth)
    val role: UserRole = UserRole.STAFF,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val isActive: Boolean = true,
    val lastLoginAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    val displayName: String
        get() = if (fullName.isNotBlank()) fullName else username

    val initials: String
        get() = fullName.split(" ")
            .filter { it.isNotBlank() }
            .joinToString("") { it.first().uppercase() }
            .take(2)
}

enum class UserRole(val displayName: String, val permissions: List<UserPermission>) {
    ADMIN("Administrator", UserPermission.all()),
    MANAGER("Manager", UserPermission.managerPermissions()),
    STAFF("Staff", UserPermission.staffPermissions()),
    VIEWER("Viewer", UserPermission.viewerPermissions()),
}

enum class UserPermission(val key: String, val displayName: String) {
    // Product permissions
    PRODUCT_CREATE("product.create", "Create Products"),
    PRODUCT_READ("product.read", "View Products"),
    PRODUCT_UPDATE("product.update", "Edit Products"),
    PRODUCT_DELETE("product.delete", "Delete Products"),

    // Stock permissions
    STOCK_IN_CREATE("stock.in.create", "Record Stock In"),
    STOCK_OUT_CREATE("stock.out.create", "Record Stock Out"),
    STOCK_ADJUST("stock.adjust", "Adjust Stock"),
    STOCK_TRANSFER("stock.transfer", "Transfer Stock"),
    STOCK_VIEW_HISTORY("stock.history", "View Stock History"),

    // Reports
    REPORT_VIEW("report.view", "View Reports"),
    REPORT_EXPORT("report.export", "Export Reports"),

    // User management
    USER_CREATE("user.create", "Create Users"),
    USER_READ("user.read", "View Users"),
    USER_UPDATE("user.update", "Edit Users"),
    USER_DELETE("user.delete", "Delete Users"),

    // Settings
    SETTINGS_MANAGE("settings.manage", "Manage Settings"),
    BACKUP_RESTORE("backup.restore", "Backup & Restore"),

    // Barcode
    BARCODE_SCAN("barcode.scan", "Scan Barcodes"),
    BARCODE_PRINT("barcode.print", "Print Barcodes"),

    companion object {
        fun all(): List<UserPermission> = values().toList()

        fun managerPermissions(): List<UserPermission> = listOf(
            PRODUCT_CREATE, PRODUCT_READ, PRODUCT_UPDATE,
            STOCK_IN_CREATE, STOCK_OUT_CREATE, STOCK_ADJUST, STOCK_TRANSFER, STOCK_VIEW_HISTORY,
            REPORT_VIEW, REPORT_EXPORT,
            USER_READ,
            BARCODE_SCAN, BARCODE_PRINT,
        )

        fun staffPermissions(): List<UserPermission> = listOf(
            PRODUCT_READ,
            STOCK_IN_CREATE, STOCK_OUT_CREATE, STOCK_VIEW_HISTORY,
            REPORT_VIEW,
            BARCODE_SCAN,
        )

        fun viewerPermissions(): List<UserPermission> = listOf(
            PRODUCT_READ,
            STOCK_VIEW_HISTORY,
            REPORT_VIEW,
        )
    }
}