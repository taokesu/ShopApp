package com.shopapp.data.local

import androidx.room.TypeConverter
import com.shopapp.data.model.OrderStatus
import com.shopapp.data.model.ProductCategory
import com.shopapp.data.model.UserRole
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromProductCategory(value: ProductCategory): String {
        return value.name
    }

    @TypeConverter
    fun toProductCategory(value: String): ProductCategory {
        return ProductCategory.valueOf(value)
    }

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String {
        return value.name
    }

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus {
        return OrderStatus.valueOf(value)
    }

    @TypeConverter
    fun fromUserRole(value: UserRole): String {
        return value.name
    }

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return UserRole.valueOf(value)
    }
}
