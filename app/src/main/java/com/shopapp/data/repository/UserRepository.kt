package com.shopapp.data.repository

import com.shopapp.data.model.User
import com.shopapp.data.model.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun registerUser(user: User): Long
    suspend fun loginUser(username: String, password: String): User?
    suspend fun getUserById(userId: Long): User?
    suspend fun updateUser(user: User)
    suspend fun isUsernameAvailable(username: String): Boolean
    fun getUsersByRole(role: UserRole): Flow<List<User>>
}
