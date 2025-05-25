package com.shopapp.presentation.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.Order
import com.shopapp.data.model.OrderStatus
import com.shopapp.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagerOrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerOrdersUiState())
    val uiState: StateFlow<ManagerOrdersUiState> = _uiState.asStateFlow()

    init {
        loadAllOrders()
    }

    fun loadAllOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                orderRepository.getAllOrders().collect { orders ->
                    _uiState.update {
                        it.copy(
                            orders = orders,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка загрузки заказов: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun loadOrderDetails(orderId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val order = orderRepository.getOrderWithItems(orderId)
                _uiState.update {
                    it.copy(
                        currentOrder = order,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка загрузки деталей заказа: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                orderRepository.updateOrderStatus(orderId, newStatus)
                
                // Обновляем текущий заказ, если он отображается
                val currentOrder = _uiState.value.currentOrder
                if (currentOrder != null && currentOrder.id == orderId) {
                    loadOrderDetails(orderId)
                } else {
                    // Если мы на экране списка, обновляем список заказов
                    loadAllOrders()
                }
                
                _uiState.update {
                    it.copy(
                        successMessage = "Статус заказа успешно обновлен",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка обновления статуса заказа: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun filterOrdersByStatus(status: OrderStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                orderRepository.getOrdersByStatus(status).collect { orders ->
                    _uiState.update {
                        it.copy(
                            orders = orders,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка фильтрации заказов: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun sortOrdersByDateDesc() {
        _uiState.update {
            it.copy(orders = it.orders.sortedByDescending { order -> order.orderDate })
        }
    }
    
    fun sortOrdersByDateAsc() {
        _uiState.update {
            it.copy(orders = it.orders.sortedBy { order -> order.orderDate })
        }
    }
    
    fun sortOrdersByTotalDesc() {
        _uiState.update {
            it.copy(orders = it.orders.sortedByDescending { order -> order.totalAmount })
        }
    }
    
    fun sortOrdersByTotalAsc() {
        _uiState.update {
            it.copy(orders = it.orders.sortedBy { order -> order.totalAmount })
        }
    }
    
    fun clearMessage() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }
}

data class ManagerOrdersUiState(
    val orders: List<Order> = emptyList(),
    val currentOrder: Order? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
