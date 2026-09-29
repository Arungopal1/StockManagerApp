package com.stockmanager.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stockmanager.app.data.dao.CustomerDao
import com.stockmanager.app.data.dao.ProductDao
import com.stockmanager.app.data.dao.StockTransactionDao
import com.stockmanager.app.data.dao.SupplierDao
import com.stockmanager.app.data.dao.UserDao
import com.stockmanager.app.data.entity.Customer
import com.stockmanager.app.data.entity.Product
import com.stockmanager.app.data.entity.StockTransaction
import com.stockmanager.app.data.entity.Supplier
import com.stockmanager.app.data.entity.User
import com.stockmanager.app.data.util.Converters

@Database(
    entities = [
        Product::class,
        StockTransaction::class,
        User::class,
        Supplier::class,
        Customer::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class StockDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun stockTransactionDao(): StockTransactionDao
    abstract fun userDao(): UserDao
    abstract fun supplierDao(): SupplierDao
    abstract fun customerDao(): CustomerDao

    companion object {
        @Volatile
        private var INSTANCE: StockDatabase? = null

        fun getDatabase(context: Context): StockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockDatabase::class.java,
                    "stock_manager.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}