package com.stockmanager.app.data.util

import androidx.room.TypeConverter
import com.stockmanager.app.data.entity.ProductCategory
import com.stockmanager.app.data.entity.ProductCondition
import com.stockmanager.app.data.entity.TransactionStatus
import com.stockmanager.app.data.entity.TransactionType
import com.stockmanager.app.data.entity.UserRole
import com.stockmanager.app.data.entity.CustomerType
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeFormatter
import kotlinx.datetime.format.DateTimeFormatterBuilder
import kotlinx.datetime.format.DateTimeFormatterCombinators.*

class Converters {

    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_DATE

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(dateTimeFormatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun fromProductCategory(value: ProductCategory?): String? {
        return value?.name
    }

    @TypeConverter
    fun toProductCategory(value: String?): ProductCategory? {
        return value?.let { ProductCategory.valueOf(it) }
    }

    @TypeConverter
    fun fromProductCondition(value: ProductCondition?): String? {
        return value?.name
    }

    @TypeConverter
    fun toProductCondition(value: String?): ProductCondition? {
        return value?.let { ProductCondition.valueOf(it) }
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? {
        return value?.let { TransactionType.valueOf(it) }
    }

    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toTransactionStatus(value: String?): TransactionStatus? {
        return value?.let { TransactionStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromUserRole(value: UserRole?): String? {
        return value?.name
    }

    @TypeConverter
    fun toUserRole(value: String?): UserRole? {
        return value?.let { UserRole.valueOf(it) }
    }

    @TypeConverter
    fun fromCustomerType(value: CustomerType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toCustomerType(value: String?): CustomerType? {
        return value?.let { CustomerType.valueOf(it) }
    }
}