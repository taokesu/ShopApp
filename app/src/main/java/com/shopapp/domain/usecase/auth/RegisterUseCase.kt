package com.shopapp.domain.usecase.auth

import com.shopapp.data.model.User
import com.shopapp.data.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<Long> {
        return try {
            // Проверяем, доступно ли имя пользователя
            if (userRepository.isUsernameAvailable(user.username)) {
                val userId = userRepository.registerUser(user)
                Result.success(userId)
            } else {
                Result.failure(Exception("Имя пользователя уже занято"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
