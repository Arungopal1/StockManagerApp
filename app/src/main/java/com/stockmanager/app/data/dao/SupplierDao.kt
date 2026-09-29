package com.stockmanager.app.data.dao

import androidx.room.*
import com.stockmanager.app.data.entity.Supplier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface SupplierDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: Supplier): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suppliers: List<Supplier>): List<Long>

    @Update
    suspend fun update(supplier: Supplier): Int

    @Delete
    suspend fun delete(supplier: Supplier): Int

    @Query("SELECT * FROM suppliers WHERE id = :id")
    fun getById(id: Long): Flow<Supplier?>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getByIdSync(id: Long): Supplier?

    @Query("SELECT * FROM suppliers WHERE shopId = :shopId AND isActive = 1 ORDER BY name ASC")
    fun getAllActive(shopId: Long): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE shopId = :shopId ORDER BY createdAt DESC")
    fun getAll(shopId: Long): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE shopId = :shopId AND (name LIKE :query OR contactPerson LIKE :query OR email LIKE :query OR phone LIKE :query) ORDER BY name ASC")
    fun search(shopId: Long, query: String): Flow<List<Supplier>>

    @Query("UPDATE suppliers SET currentBalance = currentBalance + :amount WHERE id = :id")
    suspend fun adjustBalance(id: Long, amount: Double): Int

    @Query("SELECT SUM(currentBalance) FROM suppliers WHERE shopId = :shopId AND isActive = 1")
    suspend fun getTotalPayable(shopId: Long): Double?

    // ========================================================================
    // SYNC EXTENSIONS
    // ========================================================================

    @Query("SELECT * FROM suppliers WHERE remoteId IS NOT NULL AND (isSynced = 0 OR isSynced IS NULL) ORDER BY updatedAt ASC")
    suspend fun getUnsyncedSuppliers(): List<Supplier>

    @Query("SELECT * FROM suppliers WHERE remoteId = :remoteId")
    suspend fun getByRemoteIdSync(remoteId: String): Supplier?

    @Query("SELECT * FROM suppliers WHERE remoteId = :remoteId")
    fun getByRemoteId(remoteId: String): Flow<Supplier?>

    @Query("UPDATE suppliers SET isSynced = 1, updatedAt = :now WHERE id = :id")
    suspend fun markSynced(id: Long, now: LocalDateTime)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFromRemote(supplier: Supplier): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllFromRemote(suppliers: List<Supplier>): List<Long>

    @Query("SELECT * FROM suppliers WHERE shopId = :shopId ORDER BY name ASC")
    fun getByShopId(shopId: Long): Flow<List<Supplier>>

    @Query("DELETE FROM suppliers WHERE shopId = :shopId")
    suspend fun deleteByShopId(shopId: Long): Int

    @Query("SELECT COUNT(*) FROM suppliers WHERE remoteId IS NOT NULL AND (isSynced = 0 OR isSynced IS NULL)")
    suspend fun countUnsynced(): Int
}