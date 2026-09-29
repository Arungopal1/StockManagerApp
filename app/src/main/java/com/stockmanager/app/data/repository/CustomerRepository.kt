package com.stockmanager.app.data.repository

import com.stockmanager.app.data.dao.CustomerDao
import com.stockmanager.app.data.entity.Customer
import com.stockmanager.app.data.entity.CustomerType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {

    suspend fun insert(customer: Customer): Long = customerDao.insert(customer)

    suspend fun insertAll(customers: List<Customer>): List<Long> = customerDao.insertAll(customers)

    suspend fun update(customer: Customer): Int = customerDao.update(customer.copy(updatedAt = LocalDateTime.now()))

    suspend fun delete(customer: Customer): Int = customerDao.delete(customer)

    fun getById(id: Long): Flow<Customer?> = customerDao.getById(id)

    suspend fun getByIdSync(id: Long): Customer? = customerDao.getByIdSync(id)

    suspend fun getByPhoneSync(phone: String): Customer? = customerDao.getByPhoneSync(phone)

    suspend fun getByEmailSync(email: String): Customer? = customerDao.getByEmailSync(email)

    fun getAllActive(): Flow<List<Customer>> = customerDao.getAllActive()

    fun getAll(): Flow<List<Customer>> = customerDao.getAll()

    fun getByType(type: CustomerType): Flow<List<Customer>> = customerDao.getByType(type)

    fun search(query: String): Flow<List<Customer>> = customerDao.search("%$query%")

    suspend fun adjustBalance(id: Long, amount: Double): Int = customerDao.adjustBalance(id, amount)

    suspend fun addLoyaltyPoints(id: Long, points: Int): Int = customerDao.addLoyaltyPoints(id, points)

    suspend fun getTotalReceivable(): Double = customerDao.getTotalReceivable() ?: 0.0
}