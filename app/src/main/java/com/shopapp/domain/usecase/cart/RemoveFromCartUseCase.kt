package com.shopapp.domain.usecase.cart

import com.shopapp.data.repository.CartRepository
import javax.inject.Inject

class RemoveFromCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(userId: Long, productId: Long): Result<Boolean> {
        return try {
            val success = cartRepository.removeFromCart(userId, productId)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Товар не найден в корзине"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
