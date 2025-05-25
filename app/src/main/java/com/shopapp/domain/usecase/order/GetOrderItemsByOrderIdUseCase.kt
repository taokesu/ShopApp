package com.shopapp.domain.usecase.order

import com.shopapp.data.model.OrderItem
import com.shopapp.data.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOrderItemsByOrderIdUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(orderId: Long): Flow<List<OrderItem>> {
        return orderRepository.getOrderItemsByOrderId(orderId)
    }
}
