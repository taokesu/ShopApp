package com.shopapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProductCategory {
    SHIRTS,
    PANTS,
    DRESSES,
    OUTERWEAR,
    SHOES,
    ACCESSORIES,
    UNDERWEAR,
    SPORTSWEAR,
    OTHER
}

// Функция расширения для получения отображаемого имени категории
val ProductCategory.displayName: String
    get() = when (this) {
        ProductCategory.SHIRTS -> "Рубашки"
        ProductCategory.PANTS -> "Брюки"
        ProductCategory.DRESSES -> "Платья"
        ProductCategory.OUTERWEAR -> "Верхняя одежда"
        ProductCategory.SHOES -> "Обувь"
        ProductCategory.ACCESSORIES -> "Аксессуары"
        ProductCategory.UNDERWEAR -> "Нижнее белье"
        ProductCategory.SPORTSWEAR -> "Спортивная одежда"
        ProductCategory.OTHER -> "Другое"
    }

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: ProductCategory,
    val imageUrl: String = "",
    val quantity: Int,
    val size: String? = null,
    val color: String? = null,
    val isAvailable: Boolean = quantity > 0
)
