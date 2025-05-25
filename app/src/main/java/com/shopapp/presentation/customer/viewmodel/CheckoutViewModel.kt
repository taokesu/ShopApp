package com.shopapp.presentation.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.CartItem
import com.shopapp.data.model.Order
import com.shopapp.data.model.OrderStatus
import com.shopapp.data.model.Product
import com.shopapp.domain.usecase.cart.GetCartItemsUseCase
import com.shopapp.domain.usecase.order.CreateOrderUseCase
import com.shopapp.domain.usecase.product.GetProductByIdUseCase
import com.shopapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val createOrderUseCase: CreateOrderUseCase
) : ViewModel() {

    private val _cartItemsState = MutableStateFlow<UiState<List<CartItemWithProduct>>>(UiState.Loading)
    val cartItemsState: StateFlow<UiState<List<CartItemWithProduct>>> = _cartItemsState

    private val _totalPriceState = MutableStateFlow(0.0)
    val totalPriceState: StateFlow<Double> = _totalPriceState

    private val _orderState = MutableStateFlow<UiState<Long>?>(null)
    val orderState: StateFlow<UiState<Long>?> = _orderState

    data class CheckoutFormState(
        val fullName: String = "",
        val phone: String = "",
        val email: String = "",
        val address: String = ""
    )

    private val _checkoutFormState = MutableStateFlow(CheckoutFormState())
    val checkoutFormState: StateFlow<CheckoutFormState> = _checkoutFormState

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

    fun updateCheckoutForm(fullName: String = _checkoutFormState.value.fullName,
                           phone: String = _checkoutFormState.value.phone,
                           email: String = _checkoutFormState.value.email,
                           address: String = _checkoutFormState.value.address) {
        _checkoutFormState.value = CheckoutFormState(
            fullName = fullName,
            phone = phone,
            email = email,
            address = address
        )
    }

    fun createOrder(userId: Long) {
        viewModelScope.launch {
            _orderState.value = UiState.Loading
            
            val cartItemsState = _cartItemsState.value
            if (cartItemsState !is UiState.Success || cartItemsState.data.isEmpty()) {
                _orderState.value = UiState.Error("Корзина пуста")
                return@launch
            }
            
            val cartItems = (cartItemsState as UiState.Success).data.map { it.cartItem }
            val totalPrice = _totalPriceState.value
            
            val order = Order(
                userId = userId,
                orderDate = Date(),
                totalAmount = totalPrice,
                status = OrderStatus.PENDING,
                userName = _checkoutFormState.value.fullName,
                userPhone = _checkoutFormState.value.phone,
                userEmail = _checkoutFormState.value.email,
                deliveryAddress = _checkoutFormState.value.address
            )
            
            createOrderUseCase(order, cartItems)
                .onSuccess { orderId ->
                    _orderState.value = UiState.Success(orderId)
                }
                .onFailure { error ->
                    _orderState.value = UiState.Error(error.message ?: "Не удалось создать заказ")
                }
        }
    }

    fun resetOrderState() {
        _orderState.value = null
    }
}
