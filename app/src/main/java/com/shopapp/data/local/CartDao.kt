package com.shopapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.shopapp.data.model.CartItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItemsByUserId(userId: Long): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId")
    suspend fun getCartItemByUserAndProductId(userId: Long, productId: Long): CartItem?

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Long)

    @Query("SELECT COUNT(*) FROM cart_items WHERE userId = :userId")
    fun getCartItemCount(userId: Long): Flow<Int>

    @Query("UPDATE cart_items SET quantity = quantity + 1 WHERE userId = :userId AND productId = :productId")
    suspend fun incrementCartItemQuantity(userId: Long, productId: Long): Int

    @Query("UPDATE cart_items SET quantity = quantity - 1 WHERE userId = :userId AND productId = :productId AND quantity > 1")
    suspend fun decrementCartItemQuantity(userId: Long, productId: Long): Int
}
