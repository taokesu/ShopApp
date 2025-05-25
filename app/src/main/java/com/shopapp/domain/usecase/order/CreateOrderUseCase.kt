package com.shopapp.domain.usecase.order

import com.shopapp.data.model.CartItem
import com.shopapp.data.model.Order
import com.shopapp.data.model.OrderItem
import com.shopapp.data.repository.CartRepository
import com.shopapp.data.repository.OrderRepository
import com.shopapp.data.repository.ProductRepository
import javax.inject.Inject

class CreateOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        order: Order,
        cartItems: List<CartItem>
    ): Result<Long> {
        return try {
            // Конвертируем элементы корзины в элементы заказа
            val orderItems = mutableListOf<OrderItem>()
            
            for (cartItem in cartItems) {
                val product = productRepository.getProductById(cartItem.productId)
                if (product == null) {
                    return Result.failure(Exception("Товар с ID ${cartItem.productId} не найден"))
                }
                
                if (product.quantity < cartItem.quantity) {
                    return Result.failure(Exception("Недостаточное количество товара ${product.name} на складе"))
                }
                
                orderItems.add(
                    OrderItem(
                        orderId = 0, // будет заменено после создания заказа
                        productId = product.id,
                        quantity = cartItem.quantity,
                        pricePerItem = product.price
                    )
                )
                
                // Уменьшаем количество товара на складе
                val decreased = productRepository.decreaseProductQuantity(product.id, cartItem.quantity)
                if (!decreased) {
                    return Result.failure(Exception("Не удалось уменьшить количество товара ${product.name} на складе"))
                }
            }
            
            // Создаем заказ и его элементы
            val orderId = orderRepository.createOrder(order, orderItems)
            
            // Очищаем корзину пользователя
            cartRepository.clearCart(order.userId)
            
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
