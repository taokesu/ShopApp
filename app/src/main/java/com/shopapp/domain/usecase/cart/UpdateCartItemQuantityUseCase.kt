package com.shopapp.domain.usecase.cart

import com.shopapp.data.repository.CartRepository
import com.shopapp.data.repository.ProductRepository
import javax.inject.Inject

class UpdateCartItemQuantityUseCase @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(userId: Long, productId: Long, quantity: Int): Result<Boolean> {
        return try {
            if (quantity <= 0) {
                return Result.failure(Exception("Количество должно быть больше нуля"))
            }
            
            // Проверяем, доступно ли такое количество товара
            val product = productRepository.getProductById(productId)
            if (product == null) {
                return Result.failure(Exception("Товар не найден"))
            }
            
            if (product.quantity < quantity) {
                return Result.failure(Exception("Недостаточное количество товара на складе"))
            }
            
            val success = cartRepository.updateCartItemQuantity(userId, productId, quantity)
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
