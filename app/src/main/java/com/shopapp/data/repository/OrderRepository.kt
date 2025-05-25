package com.shopapp.data.repository

import com.shopapp.data.model.Order
import com.shopapp.data.model.OrderItem
import com.shopapp.data.model.OrderStatus
import com.shopapp.data.model.ProductCategory
import com.shopapp.data.model.ProductSalesInfo
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun createOrder(order: Order, orderItems: List<OrderItem>): Long
    suspend fun getOrderById(orderId: Long): Order?
    fun getOrdersByUserId(userId: Long): Flow<List<Order>>
    fun getAllOrders(): Flow<List<Order>>
    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>>
    suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): Boolean
    fun getOrderItemsByOrderId(orderId: Long): Flow<List<OrderItem>>
    suspend fun getTotalSoldQuantityForProduct(productId: Long): Int
    
    // Получение заказа вместе с позициями заказа
    suspend fun getOrderWithItems(orderId: Long): Order?
    
    // Методы для аналитики
    fun getTopSellingProducts(limit: Int = 5): Flow<List<ProductSalesInfo>>
    fun getSalesByCategory(): Flow<Map<ProductCategory, Double>>
    fun getOrdersInPeriod(startDate: Long, endDate: Long): Flow<List<Order>>
}
