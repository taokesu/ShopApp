package com.shopapp.data.repository

import com.shopapp.data.local.FavoriteDao
import com.shopapp.data.model.FavoriteItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override suspend fun addToFavorites(userId: Long, productId: Long): Boolean {
        favoriteDao.insertFavoriteItem(FavoriteItem(userId, productId))
        return true
    }

    override suspend fun removeFromFavorites(userId: Long, productId: Long): Boolean {
        return favoriteDao.removeFavoriteItem(userId, productId) > 0
    }

    override fun getFavoriteItems(userId: Long): Flow<List<FavoriteItem>> {
        return favoriteDao.getFavoriteItemsByUserId(userId)
    }

    override fun isProductFavorite(userId: Long, productId: Long): Flow<Boolean> {
        return favoriteDao.isProductFavorite(userId, productId)
    }

    override fun getFavoriteItemCount(userId: Long): Flow<Int> {
        return favoriteDao.getFavoriteItemCount(userId)
    }
}
