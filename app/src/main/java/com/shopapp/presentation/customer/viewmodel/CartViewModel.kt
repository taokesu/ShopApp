package com.shopapp.presentation.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.CartItem
import com.shopapp.data.model.Product
import com.shopapp.domain.usecase.cart.ClearCartUseCase
import com.shopapp.domain.usecase.cart.GetCartItemsUseCase
import com.shopapp.domain.usecase.cart.RemoveFromCartUseCase
import com.shopapp.domain.usecase.cart.UpdateCartItemQuantityUseCase
import com.shopapp.domain.usecase.product.GetProductByIdUseCase
import com.shopapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItemWithProduct(
    val cartItem: CartItem,
    val product: Product
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val updateCartItemQuantityUseCase: UpdateCartItemQuantityUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val clearCartUseCase: ClearCartUseCase
) : ViewModel() {

    private val _cartItemsState = MutableStateFlow<UiState<List<CartItemWithProduct>>>(UiState.Loading)
    val cartItemsState: StateFlow<UiState<List<CartItemWithProduct>>> = _cartItemsState

    private val _totalPriceState = MutableStateFlow(0.0)
    val totalPriceState: StateFlow<Double> = _totalPriceState

    private val _actionState = MutableStateFlow<UiState<String>?>(null)
    val actionState: StateFlow<UiState<String>?> = _actionState

    fun loadCartItems(userId: Long) {
        _cartItemsState.value = UiState.Loading
        
        getCartItemsUseCase(userId)
            .onEach { cartItems ->
                if (cartItems.isEmpty()) {
                    _cartItemsState.value = UiState.Success(emptyList())
                    _totalPriceState.value = 0.0
                    return@onEach
                }
                
                val cartItemsWithProducts = mutableListOf<CartItemWithProduct>()
                var totalPrice = 0.0
                
                for (cartItem in cartItems) {
                    val productResult = getProductByIdUseCase(cartItem.productId)
                    productResult.onSuccess { product ->
                        cartItemsWithProducts.add(CartItemWithProduct(cartItem, product))
                        totalPrice += product.price * cartItem.quantity
                    }
                }
                
                _cartItemsState.value = UiState.Success(cartItemsWithProducts)
                _totalPriceState.value = totalPrice
            }
            .catch { error ->
                _cartItemsState.value = UiState.Error(error.message ?: "Не удалось загрузить корзину")
            }
            .launchIn(viewModelScope)
    }

    fun updateQuantity(userId: Long, productId: Long, newQuantity: Int) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            
            updateCartItemQuantityUseCase(userId, productId, newQuantity)
                .onSuccess {
                    loadCartItems(userId)
                    _actionState.value = UiState.Success("Количество обновлено")
                }
                .onFailure { error ->
                    _actionState.value = UiState.Error(error.message ?: "Не удалось обновить количество")
                }
        }
    }

    fun removeItem(userId: Long, productId: Long) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            
            removeFromCartUseCase(userId, productId)
                .onSuccess {
                    loadCartItems(userId)
                    _actionState.value = UiState.Success("Товар удален из корзины")
                }
                .onFailure { error ->
                    _actionState.value = UiState.Error(error.message ?: "Не удалось удалить товар из корзины")
                }
        }
    }

    fun clearCart(userId: Long) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            
            clearCartUseCase(userId)
                .onSuccess {
                    loadCartItems(userId)
                    _actionState.value = UiState.Success("Корзина очищена")
                }
                .onFailure { error ->
                    _actionState.value = UiState.Error(error.message ?: "Не удалось очистить корзину")
                }
        }
    }

    fun resetActionState() {
        _actionState.value = null
    }
}
