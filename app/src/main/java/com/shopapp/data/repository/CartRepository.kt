package com.shopapp.data.repository

import com.shopapp.data.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    suspend fun addToCart(userId: Long, productId: Long, quantity: Int = 1): Boolean
    suspend fun updateCartItemQuantity(userId: Long, productId: Long, quantity: Int): Boolean
    suspend fun removeFromCart(userId: Long, productId: Long): Boolean
    suspend fun clearCart(userId: Long)
    fun getCartItems(userId: Long): Flow<List<CartItem>>
    fun getCartItemCount(userId: Long): Flow<Int>
    suspend fun incrementCartItemQuantity(userId: Long, productId: Long): Boolean
    suspend fun decrementCartItemQuantity(userId: Long, productId: Long): Boolean
}
