package com.stockmanager.app.di

import android.content.Context
import com.stockmanager.app.data.database.StockDatabase
import com.stockmanager.app.data.dao.CustomerDao
import com.stockmanager.app.data.dao.ProductDao
import com.stockmanager.app.data.dao.StockTransactionDao
import com.stockmanager.app.data.dao.SupplierDao
import com.stockmanager.app.data.dao.UserDao
import com.stockmanager.app.data.repository.CustomerRepository
import com.stockmanager.app.data.repository.ProductRepository
import com.stockmanager.app.data.repository.StockTransactionRepository
import com.stockmanager.app.data.repository.SupplierRepository
import com.stockmanager.app.data.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StockDatabase {
        return StockDatabase.getDatabase(context)
    }

    @Provides
    fun provideProductDao(database: StockDatabase): ProductDao = database.productDao()

    @Provides
    fun provideStockTransactionDao(database: StockDatabase): StockTransactionDao = database.stockTransactionDao()

    @Provides
    fun provideUserDao(database: StockDatabase): UserDao = database.userDao()

    @Provides
    fun provideSupplierDao(database: StockDatabase): SupplierDao = database.supplierDao()

    @Provides
    fun provideCustomerDao(database: StockDatabase): CustomerDao = database.customerDao()

    @Provides
    @Singleton
    fun provideProductRepository(dao: ProductDao): ProductRepository = ProductRepository(dao)

    @Provides
    @Singleton
    fun provideStockTransactionRepository(dao: StockTransactionDao): StockTransactionRepository = StockTransactionRepository(dao)

    @Provides
    @Singleton
    fun provideUserRepository(dao: UserDao): UserRepository = UserRepository(dao)

    @Provides
    @Singleton
    fun provideSupplierRepository(dao: SupplierDao): SupplierRepository = SupplierRepository(dao)

    @Provides
    @Singleton
    fun provideCustomerRepository(dao: CustomerDao): CustomerRepository = CustomerRepository(dao)
}