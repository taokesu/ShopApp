package com.shopapp.data.model

/**
 * Класс, содержащий информацию о продажах продукта для аналитики
 */
data class ProductSalesInfo(
    val product: Product,
    val quantitySold: Int,
    val totalRevenue: Double,
    val trend: Int = 0 // Процентное изменение по сравнению с предыдущим периодом (положительное или отрицательное)
) {
    // Дополнительные свойства для экспорта в Excel
    val productId: Long get() = product.id
    val productName: String get() = product.name
    val category: ProductCategory get() = product.category
    val revenue: Double get() = totalRevenue
}
