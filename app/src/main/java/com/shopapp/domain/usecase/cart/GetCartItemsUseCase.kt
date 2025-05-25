package com.shopapp.domain.usecase.cart

import com.shopapp.data.model.CartItem
import com.shopapp.data.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartItemsUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    operator fun invoke(userId: Long): Flow<List<CartItem>> {
        return cartRepository.getCartItems(userId)
    }
}
