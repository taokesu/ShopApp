package com.shopapp.data.repository

import com.shopapp.data.local.OrderDao
import com.shopapp.data.local.OrderItemDao
import com.shopapp.data.local.ProductDao
import com.shopapp.data.model.Order
import com.shopapp.data.model.OrderItem
import com.shopapp.data.model.OrderStatus
import com.shopapp.data.model.ProductCategory
import com.shopapp.data.model.ProductSalesInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val productDao: ProductDao
) : OrderRepository {

    override suspend fun createOrder(order: Order, orderItems: List<OrderItem>): Long {
        val orderId = orderDao.insertOrder(order)
        
        // Устанавливаем orderId для всех элементов заказа
        val orderItemsWithId = orderItems.map { item ->
            item.copy(orderId = orderId)
        }
        
        orderItemDao.insertOrderItems(orderItemsWithId)
        return orderId
    }

    override suspend fun getOrderById(orderId: Long): Order? {
        return orderDao.getOrderById(orderId)
    }

    override fun getOrdersByUserId(userId: Long): Flow<List<Order>> {
        return orderDao.getOrdersByUserId(userId)
    }

    override fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders()
    }

    override fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>> {
        return orderDao.getOrdersByStatus(status)
    }

    override suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): Boolean {
        return orderDao.updateOrderStatus(orderId, newStatus) > 0
    }

    override fun getOrderItemsByOrderId(orderId: Long): Flow<List<OrderItem>> {
        return orderItemDao.getOrderItemsByOrderId(orderId)
    }

    override suspend fun getTotalSoldQuantityForProduct(productId: Long): Int {
        return orderItemDao.getTotalSoldQuantityForProduct(productId) ?: 0
    }
    
    override suspend fun getOrderWithItems(orderId: Long): Order? {
        val order = orderDao.getOrderById(orderId) ?: return null
        val orderItems = orderItemDao.getOrderItemsWithProductsByOrderId(orderId)
        return order.copy(items = orderItems)
    }
    
    override fun getTopSellingProducts(limit: Int): Flow<List<ProductSalesInfo>> = flow {
        try {
            val productsWithSales = mutableListOf<ProductSalesInfo>()
            val productsIds = orderItemDao.getTopSellingProductIds(limit)
            
            for (productId in productsIds) {
                val product = productDao.getProductById(productId) ?: continue
                val quantitySold = orderItemDao.getTotalSoldQuantityForProduct(productId) ?: 0
                val totalRevenue = orderItemDao.getTotalRevenueForProduct(productId) ?: 0.0
                
                // Для демо-данных просто используем случайный тренд от -10 до +20
                val trend = (-10..20).random()
                
                productsWithSales.add(
                    ProductSalesInfo(
                        product = product,
                        quantitySold = quantitySold,
                        totalRevenue = totalRevenue,
                        trend = trend
                    )
                )
            }
            
            emit(productsWithSales)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    override fun getSalesByCategory(): Flow<Map<ProductCategory, Double>> = flow {
        try {
            val categorySales = mutableMapOf<ProductCategory, Double>()
            
            // Инициализируем все категории нулевыми продажами
            ProductCategory.values().forEach { category ->
                categorySales[category] = 0.0
            }
            
            // Получаем данные о продажах по категориям
            val completedOrderItemsWithProducts = orderItemDao.getCompletedOrderItemsWithProducts()
            
            completedOrderItemsWithProducts.forEach { orderItem ->
                val category = orderItem.product.category
                val revenue = orderItem.orderItem.quantity * orderItem.product.price
                categorySales[category] = (categorySales[category] ?: 0.0) + revenue.toDouble()
            }
            
            emit(categorySales)
        } catch (e: Exception) {
            emit(emptyMap())
        }
    }
    
    override fun getOrdersInPeriod(startDate: Long, endDate: Long): Flow<List<Order>> {
        return orderDao.getOrdersInPeriod(startDate, endDate)
    }
}
