package com.stockmanager.app.data.dao

import androidx.room.*
import com.stockmanager.app.data.entity.User
import com.stockmanager.app.data.entity.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>): List<Long>

    @Update
    suspend fun update(user: User): Int

    @Delete
    suspend fun delete(user: User): Int

    @Query("SELECT * FROM users WHERE id = :id")
    fun getById(id: Long): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getByIdSync(id: Long): User?

    @Query("SELECT * FROM users WHERE username = :username")
    fun getByUsername(username: String): Flow<User?>

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getByUsernameSync(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getByEmailSync(email: String): User?

    @Query("SELECT * FROM users WHERE shopId = :shopId AND role = :role AND isActive = 1 ORDER BY fullName ASC")
    fun getByRole(shopId: Long, role: UserRole): Flow<List<User>>

    @Query("SELECT * FROM users WHERE shopId = :shopId AND isActive = 1 ORDER BY fullName ASC")
    fun getAllActive(shopId: Long): Flow<List<User>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAll(): Flow<List<User>>

    @Query("UPDATE users SET lastLoginAt = :lastLoginAt WHERE id = :id")
    suspend fun updateLastLogin(id: Long, lastLoginAt: LocalDateTime): Int

    @Query("UPDATE users SET passwordHash = :newHash WHERE id = :id")
    suspend fun updatePassword(id: Long, newHash: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE shopId = :shopId AND isActive = 1")
    suspend fun getActiveCount(shopId: Long): Int

    // ========================================================================
    // SYNC EXTENSIONS
    // ========================================================================

    @Query("SELECT * FROM users WHERE remoteId IS NOT NULL AND (isSynced = 0 OR isSynced IS NULL) ORDER BY updatedAt ASC")
    suspend fun getUnsyncedUsers(): List<User>

    @Query("SELECT * FROM users WHERE remoteId = :remoteId")
    suspend fun getByRemoteIdSync(remoteId: String): User?

    @Query("SELECT * FROM users WHERE remoteId = :remoteId")
    fun getByRemoteId(remoteId: String): Flow<User?>

    @Query("UPDATE users SET isSynced = 1, updatedAt = :now WHERE id = :id")
    suspend fun markSynced(id: Long, now: LocalDateTime)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFromRemote(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllFromRemote(users: List<User>): List<Long>

    @Query("SELECT * FROM users WHERE shopId = :shopId ORDER BY fullName ASC")
    fun getByShopId(shopId: Long): Flow<List<User>>

    @Query("DELETE FROM users WHERE shopId = :shopId")
    suspend fun deleteByShopId(shopId: Long): Int

    @Query("SELECT COUNT(*) FROM users WHERE remoteId IS NOT NULL AND (isSynced = 0 OR isSynced IS NULL)")
    suspend fun countUnsynced(): Int
}