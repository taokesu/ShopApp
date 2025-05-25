package com.shopapp.domain.usecase.order

import com.shopapp.data.model.Order
import com.shopapp.data.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(userId: Long): Flow<List<Order>> {
        return orderRepository.getOrdersByUserId(userId)
    }
}
