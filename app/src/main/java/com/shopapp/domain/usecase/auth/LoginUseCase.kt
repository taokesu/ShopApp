package com.shopapp.domain.usecase.auth

import com.shopapp.data.model.User
import com.shopapp.data.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return try {
            val user = userRepository.loginUser(username, password)
            
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Неправильное имя пользователя или пароль"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
