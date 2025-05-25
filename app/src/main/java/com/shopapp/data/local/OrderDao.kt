package com.shopapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.shopapp.data.model.Order
import com.shopapp.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Long): Order?

    @Query("SELECT * FROM orders WHERE userId = :userId")
    fun getOrdersByUserId(userId: Long): Flow<List<Order>>

    @Query("SELECT * FROM orders")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE status = :status")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>>

    @Query("UPDATE orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): Int
    
    @Query("SELECT * FROM orders WHERE orderDate BETWEEN :startDate AND :endDate")
    fun getOrdersInPeriod(startDate: Long, endDate: Long): Flow<List<Order>>
}
