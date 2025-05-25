package com.shopapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date

enum class OrderStatus(val displayName: String) {
    PENDING("Ожидает обработки"),
    PROCESSING("В обработке"),
    SHIPPED("Отправлен"),
    DELIVERED("Доставлен"),
    CANCELLED("Отменен")
}

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val orderDate: Date,
    val totalAmount: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val userName: String,
    val userPhone: String? = null,
    val userEmail: String? = null,
    val deliveryAddress: String? = null,
    val deliveryPrice: Double? = null,
    val paymentMethod: String? = null,
    val notes: String? = null
) {
    @Ignore
    var items: List<OrderItemWithProduct> = emptyList()
    
    fun copy(
        id: Long = this.id,
        userId: Long = this.userId,
        orderDate: Date = this.orderDate,
        totalAmount: Double = this.totalAmount,
        status: OrderStatus = this.status,
        userName: String = this.userName,
        userPhone: String? = this.userPhone,
        userEmail: String? = this.userEmail,
        deliveryAddress: String? = this.deliveryAddress,
        deliveryPrice: Double? = this.deliveryPrice,
        paymentMethod: String? = this.paymentMethod,
        notes: String? = this.notes,
        items: List<OrderItemWithProduct> = this.items
    ): Order {
        val order = Order(
            id, userId, orderDate, totalAmount, status, userName, userPhone, userEmail,
            deliveryAddress, deliveryPrice, paymentMethod, notes
        )
        order.items = items
        return order
    }
}
