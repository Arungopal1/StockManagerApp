package com.stockmanager.app.data.repository

import com.stockmanager.app.data.dao.UserDao
import com.stockmanager.app.data.entity.User
import com.stockmanager.app.data.entity.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    suspend fun insert(user: User): Long = userDao.insert(user)

    suspend fun insertAll(users: List<User>): List<Long> = userDao.insertAll(users)

    suspend fun update(user: User): Int = userDao.update(user.copy(updatedAt = LocalDateTime.now()))

    suspend fun delete(user: User): Int = userDao.delete(user)

    fun getById(id: Long): Flow<User?> = userDao.getById(id)

    suspend fun getByIdSync(id: Long): User? = userDao.getByIdSync(id)

    fun getByUsername(username: String): Flow<User?> = userDao.getByUsername(username)

    suspend fun getByUsernameSync(username: String): User? = userDao.getByUsernameSync(username)

    suspend fun getByEmailSync(email: String): User? = userDao.getByEmailSync(email)

    fun getByRole(role: UserRole): Flow<List<User>> = userDao.getByRole(role)

    fun getAllActive(): Flow<List<User>> = userDao.getAllActive()

    fun getAll(): Flow<List<User>> = userDao.getAll()

    suspend fun updateLastLogin(id: Long): Int = userDao.updateLastLogin(id, LocalDateTime.now())

    suspend fun updatePassword(id: Long, newHash: String): Int = userDao.updatePassword(id, newHash)

    suspend fun getActiveCount(): Int = userDao.getActiveCount()

    // Authentication helper
    suspend fun authenticate(username: String, passwordHash: String): User? {
        val user = getByUsernameSync(username)
        return if (user != null && user.passwordHash == passwordHash && user.isActive) user else null
    }

    // Permission checking
    fun hasPermission(user: User, permission: com.stockmanager.app.data.entity.UserPermission): Boolean {
        return user.role.permissions.contains(permission)
    }
}