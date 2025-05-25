package com.shopapp.domain.usecase.favorite

import com.shopapp.data.repository.FavoriteRepository
import javax.inject.Inject

class RemoveFromFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(userId: Long, productId: Long): Result<Boolean> {
        return try {
            val success = favoriteRepository.removeFromFavorites(userId, productId)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Товар не найден в избранном"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
