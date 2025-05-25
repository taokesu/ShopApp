package com.shopapp.domain.usecase.order

import com.shopapp.data.model.OrderStatus
import com.shopapp.data.repository.OrderRepository
import javax.inject.Inject

class UpdateOrderStatusUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: Long, newStatus: OrderStatus): Result<Boolean> {
        return try {
            val success = orderRepository.updateOrderStatus(orderId, newStatus)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Заказ не найден или не удалось обновить статус"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
