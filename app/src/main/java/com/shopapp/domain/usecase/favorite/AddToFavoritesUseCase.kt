package com.shopapp.domain.usecase.favorite

import com.shopapp.data.repository.FavoriteRepository
import com.shopapp.data.repository.ProductRepository
import javax.inject.Inject

class AddToFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(userId: Long, productId: Long): Result<Boolean> {
        return try {
            // Проверяем, существует ли продукт
            val product = productRepository.getProductById(productId)
            if (product == null) {
                return Result.failure(Exception("Товар не найден"))
            }
            
            val success = favoriteRepository.addToFavorites(userId, productId)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Не удалось добавить товар в избранное"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
