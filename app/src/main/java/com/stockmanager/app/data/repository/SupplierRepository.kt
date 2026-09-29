package com.stockmanager.app.data.repository

import com.stockmanager.app.data.dao.SupplierDao
import com.stockmanager.app.data.entity.Supplier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplierRepository @Inject constructor(
    private val supplierDao: SupplierDao
) {

    suspend fun insert(supplier: Supplier): Long = supplierDao.insert(supplier)

    suspend fun insertAll(suppliers: List<Supplier>): List<Long> = supplierDao.insertAll(suppliers)

    suspend fun update(supplier: Supplier): Int = supplierDao.update(supplier.copy(updatedAt = LocalDateTime.now()))

    suspend fun delete(supplier: Supplier): Int = supplierDao.delete(supplier)

    fun getById(id: Long): Flow<Supplier?> = supplierDao.getById(id)

    suspend fun getByIdSync(id: Long): Supplier? = supplierDao.getByIdSync(id)

    fun getAllActive(): Flow<List<Supplier>> = supplierDao.getAllActive()

    fun getAll(): Flow<List<Supplier>> = supplierDao.getAll()

    fun search(query: String): Flow<List<Supplier>> = supplierDao.search("%$query%")

    suspend fun adjustBalance(id: Long, amount: Double): Int = supplierDao.adjustBalance(id, amount)

    suspend fun getTotalPayable(): Double = supplierDao.getTotalPayable() ?: 0.0
}