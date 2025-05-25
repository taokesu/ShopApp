package com.shopapp.data.repository

import com.shopapp.data.model.FavoriteItem
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    suspend fun addToFavorites(userId: Long, productId: Long): Boolean
    suspend fun removeFromFavorites(userId: Long, productId: Long): Boolean
    fun getFavoriteItems(userId: Long): Flow<List<FavoriteItem>>
    fun isProductFavorite(userId: Long, productId: Long): Flow<Boolean>
    fun getFavoriteItemCount(userId: Long): Flow<Int>
}
