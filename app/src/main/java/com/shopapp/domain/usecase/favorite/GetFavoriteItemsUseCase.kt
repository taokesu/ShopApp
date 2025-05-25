package com.shopapp.domain.usecase.favorite

import com.shopapp.data.model.FavoriteItem
import com.shopapp.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteItemsUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(userId: Long): Flow<List<FavoriteItem>> {
        return favoriteRepository.getFavoriteItems(userId)
    }
}
