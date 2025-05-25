package com.shopapp.data.model

/**
 * Класс, содержащий информацию о продажах продукта для аналитики
 */
data class ProductSalesInfo(
    val product: Product,
    val quantitySold: Int,
    val totalRevenue: Double,
    val trend: Int // Процентное изменение по сравнению с предыдущим периодом (положительное или отрицательное)
)
