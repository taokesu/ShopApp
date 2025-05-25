package com.shopapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shopapp.data.model.OrderItem
import com.shopapp.data.model.OrderItemWithProduct
import com.shopapp.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(orderItem: OrderItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(orderItems: List<OrderItem>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItemsByOrderId(orderId: Long): Flow<List<OrderItem>>

    @Query("SELECT * FROM order_items WHERE productId = :productId")
    fun getOrderItemsByProductId(productId: Long): Flow<List<OrderItem>>

    @Query("SELECT SUM(quantity) FROM order_items WHERE productId = :productId")
    suspend fun getTotalSoldQuantityForProduct(productId: Long): Int?

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteOrderItemsByOrderId(orderId: Long)
    
    // Методы для аналитики
    
    @Transaction
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsWithProductsByOrderId(orderId: Long): List<OrderItemWithProduct>
    
    @Query("""
        SELECT productId FROM order_items
        GROUP BY productId
        ORDER BY SUM(quantity) DESC
        LIMIT :limit
    """)
    suspend fun getTopSellingProductIds(limit: Int): List<Long>
    
    @Query("""
        SELECT SUM(quantity * pricePerItem) FROM order_items
        WHERE productId = :productId
    """)
    suspend fun getTotalRevenueForProduct(productId: Long): Double?
    
    @Transaction
    @Query("""
        SELECT oi.* FROM order_items oi
        JOIN orders o ON oi.orderId = o.id
        WHERE o.status = 'DELIVERED'
    """)
    suspend fun getCompletedOrderItemsWithProducts(): List<OrderItemWithProduct>
}
