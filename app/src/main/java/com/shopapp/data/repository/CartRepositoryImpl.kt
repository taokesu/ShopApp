package com.shopapp.data.repository

import com.shopapp.data.local.CartDao
import com.shopapp.data.model.CartItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    override suspend fun addToCart(userId: Long, productId: Long, quantity: Int): Boolean {
        val existingItem = cartDao.getCartItemByUserAndProductId(userId, productId)
        
        return if (existingItem != null) {
            // Если товар уже в корзине, обновляем количество
            val newQuantity = existingItem.quantity + quantity
            updateCartItemQuantity(userId, productId, newQuantity)
        } else {
            // Иначе добавляем новый товар в корзину
            cartDao.insertCartItem(CartItem(userId, productId, quantity))
            true
        }
    }

    override suspend fun updateCartItemQuantity(userId: Long, productId: Long, quantity: Int): Boolean {
        val cartItem = cartDao.getCartItemByUserAndProductId(userId, productId)
        
        return if (cartItem != null) {
            cartDao.updateCartItem(cartItem.copy(quantity = quantity))
            true
        } else {
            false
        }
    }

    override suspend fun removeFromCart(userId: Long, productId: Long): Boolean {
        val cartItem = cartDao.getCartItemByUserAndProductId(userId, productId)
        
        return if (cartItem != null) {
            cartDao.deleteCartItem(cartItem)
            true
        } else {
            false
        }
    }

    override suspend fun clearCart(userId: Long) {
        cartDao.clearCart(userId)
    }

    override fun getCartItems(userId: Long): Flow<List<CartItem>> {
        return cartDao.getCartItemsByUserId(userId)
    }

    override fun getCartItemCount(userId: Long): Flow<Int> {
        return cartDao.getCartItemCount(userId)
    }

    override suspend fun incrementCartItemQuantity(userId: Long, productId: Long): Boolean {
        return cartDao.incrementCartItemQuantity(userId, productId) > 0
    }

    override suspend fun decrementCartItemQuantity(userId: Long, productId: Long): Boolean {
        return cartDao.decrementCartItemQuantity(userId, productId) > 0
    }
}
