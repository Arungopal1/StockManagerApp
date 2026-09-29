package com.stockmanager.app.data.dao

import androidx.room.*
import com.stockmanager.app.data.entity.Customer
import com.stockmanager.app.data.entity.CustomerType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<Customer>): List<Long>

    @Update
    suspend fun update(customer: Customer): Int

    @Delete
    suspend fun delete(customer: Customer): Int

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getById(id: Long): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getByIdSync(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE phone = :phone")
    suspend fun getByPhoneSync(phone: String): Customer?

    @Query("SELECT * FROM customers WHERE email = :email")
    suspend fun getByEmailSync(email: String): Customer?

    @Query("SELECT * FROM customers WHERE shopId = :shopId AND isActive = 1 ORDER BY name ASC")
    fun getAllActive(shopId: Long): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE shopId = :shopId ORDER BY createdAt DESC")
    fun getAll(shopId: Long): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE shopId = :shopId AND customerType = :type AND isActive = 1 ORDER BY name ASC")
    fun getByType(shopId: Long, type: CustomerType): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE shopId = :shopId AND (name LIKE :query OR phone LIKE :query OR email LIKE :query) ORDER BY name ASC")
    fun search(shopId: Long, query: String): Flow<List<Customer>>

    @Query("UPDATE customers SET currentBalance = currentBalance + :amount WHERE id = :id")
    suspend fun adjustBalance(id: Long, amount: Double): Int

    @Query("UPDATE customers SET loyaltyPoints = loyaltyPoints + :points WHERE id = :id")
    suspend fun addLoyaltyPoints(id: Long, points: Int): Int

    @Query("SELECT SUM(currentBalance) FROM customers WHERE shopId = :shopId AND isActive = 1 AND currentBalance > 0")
    suspend fun getTotalReceivable(shopId: Long): Double?

    // ========================================================================
    // SYNC EXTENSIONS
    // ========================================================================

    @Query("SELECT * FROM customers WHERE remoteId IS NOT NULL AND (isSynced = 0 OR isSynced IS NULL) ORDER BY updatedAt ASC")
    suspend fun getUnsyncedCustomers(): List<Customer>

    @Query("SELECT * FROM customers WHERE remoteId = :remoteId")
    suspend fun getByRemoteIdSync(remoteId: String): Customer?

    @Query("SELECT * FROM customers WHERE remoteId = :remoteId")
    fun getByRemoteId(remoteId: String): Flow<Customer?>

    @Query("UPDATE customers SET isSynced = 1, updatedAt = :now WHERE id = :id")
    suspend fun markSynced(id: Long, now: LocalDateTime)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFromRemote(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllFromRemote(customers: List<Customer>): List<Long>

    @Query("SELECT * FROM customers WHERE shopId = :shopId ORDER BY name ASC")
    fun getByShopId(shopId: Long): Flow<List<Customer>>

    @Query("DELETE FROM customers WHERE shopId = :shopId")
    suspend fun deleteByShopId(shopId: Long): Int

    @Query("SELECT COUNT(*) FROM customers WHERE remoteId IS NOT NULL AND (isSynced = 0 OR isSynced IS NULL)")
    suspend fun countUnsynced(): Int
}