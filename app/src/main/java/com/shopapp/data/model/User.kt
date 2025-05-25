package com.shopapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CUSTOMER,
    MANAGER
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String,
    val email: String,
    val fullName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val role: UserRole
)
