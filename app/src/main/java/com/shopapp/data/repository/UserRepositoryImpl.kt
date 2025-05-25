package com.shopapp.data.repository

import com.shopapp.data.local.UserDao
import com.shopapp.data.model.User
import com.shopapp.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun registerUser(user: User): Long {
        return userDao.insertUser(user)
    }

    override suspend fun loginUser(username: String, password: String): User? {
        return userDao.login(username, password)
    }

    override suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    override suspend fun isUsernameAvailable(username: String): Boolean {
        return userDao.checkUsernameExists(username) == 0
    }

    override fun getUsersByRole(role: UserRole): Flow<List<User>> {
        return userDao.getUsersByRole(role)
    }
}
