package com.shopapp.data.repository

import com.shopapp.data.model.Product
import com.shopapp.data.model.ProductCategory
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    // Общие методы работы с товарами
    suspend fun insertProduct(product: Product): Long
    suspend fun insertProducts(products: List<Product>)
    suspend fun updateProduct(product: Product)
    suspend fun getProductById(productId: Long): Product?
    
    // Методы для покупателей (только доступные товары)
    fun getAvailableProducts(): Flow<List<Product>>
    fun getAvailableProductsByCategory(category: ProductCategory): Flow<List<Product>>
    fun getProductsOrderedByName(): Flow<List<Product>>
    fun getProductsOrderedByPriceAsc(): Flow<List<Product>>
    fun getProductsOrderedByPriceDesc(): Flow<List<Product>>
    
    // Методы для менеджеров (все товары, включая отсутствующие в наличии)
    fun getAllProducts(): Flow<List<Product>>
    fun getProductsByCategory(category: ProductCategory): Flow<List<Product>>
    fun getAllProductsOrderedByName(): Flow<List<Product>>
    fun getAllProductsOrderedByPriceAsc(): Flow<List<Product>>
    fun getAllProductsOrderedByPriceDesc(): Flow<List<Product>>
    
    // Общие методы сортировки по количеству (для менеджеров)
    fun getProductsOrderedByQuantityAsc(): Flow<List<Product>>
    fun getProductsOrderedByQuantityDesc(): Flow<List<Product>>
    fun getLowStockProducts(): Flow<List<Product>>
    
    // Методы изменения количества товаров
    suspend fun decreaseProductQuantity(productId: Long, quantityToSubtract: Int): Boolean
    suspend fun increaseProductQuantity(productId: Long, quantityToAdd: Int): Boolean
}
