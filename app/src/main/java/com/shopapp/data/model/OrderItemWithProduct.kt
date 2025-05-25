package com.shopapp.data.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Класс для связи элемента заказа с информацией о продукте
 */
data class OrderItemWithProduct(
    @Embedded val orderItem: OrderItem,
    
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: Product
)
