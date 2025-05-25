package com.shopapp.presentation.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.Order
import com.shopapp.data.model.OrderItem
import com.shopapp.data.model.OrderItemWithProduct
import com.shopapp.data.model.Product
import com.shopapp.domain.usecase.order.GetOrderItemsByOrderIdUseCase
import com.shopapp.domain.usecase.order.GetUserOrdersUseCase
import com.shopapp.domain.usecase.product.GetProductByIdUseCase
import com.shopapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderWithItems(
    val order: Order,
    val items: List<OrderItemWithProduct>
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val getUserOrdersUseCase: GetUserOrdersUseCase,
    private val getOrderItemsByOrderIdUseCase: GetOrderItemsByOrderIdUseCase,
    private val getProductByIdUseCase: GetProductByIdUseCase
) : ViewModel() {

    private val _ordersState = MutableStateFlow<UiState<List<Order>>>(UiState.Loading)
    val ordersState: StateFlow<UiState<List<Order>>> = _ordersState

    private val _orderDetailsState = MutableStateFlow<UiState<OrderWithItems>?>(null)
    val orderDetailsState: StateFlow<UiState<OrderWithItems>?> = _orderDetailsState

    fun loadUserOrders(userId: Long) {
        _ordersState.value = UiState.Loading
        
        getUserOrdersUseCase(userId)
            .onEach { orders ->
                _ordersState.value = UiState.Success(orders)
            }
            .catch { error ->
                _ordersState.value = UiState.Error(error.message ?: "Не удалось загрузить заказы")
            }
            .launchIn(viewModelScope)
    }

    fun loadOrderDetails(orderId: Long) {
        viewModelScope.launch {
            _orderDetailsState.value = UiState.Loading
            
            // Получаем заказ из списка заказов
            val ordersState = _ordersState.value
            if (ordersState !is UiState.Success) {
                _orderDetailsState.value = UiState.Error("Не удалось найти информацию о заказе")
                return@launch
            }
            
            val order = (ordersState as UiState.Success<List<Order>>).data.find { it.id == orderId }
            if (order == null) {
                _orderDetailsState.value = UiState.Error("Заказ не найден")
                return@launch
            }
            
            // Получаем элементы заказа
            try {
                getOrderItemsByOrderIdUseCase(orderId)
                    .collect { orderItems ->
                        val orderItemsWithProducts = mutableListOf<OrderItemWithProduct>()
                        
                        for (orderItem in orderItems) {
                            val productResult = getProductByIdUseCase(orderItem.productId)
                            productResult.onSuccess { product ->
                                orderItemsWithProducts.add(OrderItemWithProduct(orderItem, product))
                            }.onFailure {
                                // Если товар не найден, пропускаем его
                                // В реальном приложении можно было бы добавить дополнительную обработку
                            }
                        }
                        
                        _orderDetailsState.value = UiState.Success(
                            OrderWithItems(order, orderItemsWithProducts)
                        )
                    }
            } catch (e: Exception) {
                _orderDetailsState.value = UiState.Error(e.message ?: "Не удалось загрузить детали заказа")
            }
        }
    }

    fun resetOrderDetailsState() {
        _orderDetailsState.value = null
    }
}
