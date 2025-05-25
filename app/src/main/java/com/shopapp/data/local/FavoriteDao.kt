package com.shopapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shopapp.data.model.FavoriteItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteItem(favoriteItem: FavoriteItem)

    @Delete
    suspend fun deleteFavoriteItem(favoriteItem: FavoriteItem)

    @Query("SELECT * FROM favorite_items WHERE userId = :userId")
    fun getFavoriteItemsByUserId(userId: Long): Flow<List<FavoriteItem>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_items WHERE userId = :userId AND productId = :productId)")
    fun isProductFavorite(userId: Long, productId: Long): Flow<Boolean>

    @Query("DELETE FROM favorite_items WHERE userId = :userId AND productId = :productId")
    suspend fun removeFavoriteItem(userId: Long, productId: Long): Int

    @Query("SELECT COUNT(*) FROM favorite_items WHERE userId = :userId")
    fun getFavoriteItemCount(userId: Long): Flow<Int>
}
