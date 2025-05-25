package com.shopapp.domain.usecase.cart

import com.shopapp.data.repository.CartRepository
import com.shopapp.data.repository.ProductRepository
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(userId: Long, productId: Long, quantity: Int = 1): Result<Boolean> {
        return try {
            // Проверяем, существует ли продукт и доступен ли он
            val product = productRepository.getProductById(productId)
            
            if (product == null) {
                return Result.failure(Exception("Товар не найден"))
            }
            
            if (product.quantity < quantity) {
                return Result.failure(Exception("Недостаточное количество товара на складе"))
            }
            
            val success = cartRepository.addToCart(userId, productId, quantity)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Не удалось добавить товар в корзину"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
