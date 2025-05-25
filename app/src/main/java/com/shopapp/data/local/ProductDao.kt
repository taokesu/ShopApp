package com.shopapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shopapp.data.model.Product
import com.shopapp.data.model.ProductCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    // Общие методы работы с товарами
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Long): Product?

    // Методы для покупателей (только доступные товары)
    @Query("SELECT * FROM products WHERE quantity > 0")
    fun getAvailableProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category AND quantity > 0")
    fun getAvailableProductsByCategory(category: ProductCategory): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE quantity > 0 ORDER BY name ASC")
    fun getProductsOrderedByName(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE quantity > 0 ORDER BY price ASC")
    fun getProductsOrderedByPriceAsc(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE quantity > 0 ORDER BY price DESC")
    fun getProductsOrderedByPriceDesc(): Flow<List<Product>>

    // Методы для менеджеров (все товары, включая отсутствующие в наличии)
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE category = :category")
    fun getProductsByCategory(category: ProductCategory): Flow<List<Product>>
    
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsOrderedByName(): Flow<List<Product>>
    
    @Query("SELECT * FROM products ORDER BY price ASC")
    fun getAllProductsOrderedByPriceAsc(): Flow<List<Product>>
    
    @Query("SELECT * FROM products ORDER BY price DESC")
    fun getAllProductsOrderedByPriceDesc(): Flow<List<Product>>

    // Методы сортировки по количеству (для менеджеров)
    @Query("SELECT * FROM products ORDER BY quantity ASC")
    fun getProductsOrderedByQuantityAsc(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY quantity DESC")
    fun getProductsOrderedByQuantityDesc(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE quantity <= 5")
    fun getLowStockProducts(): Flow<List<Product>>

    // Методы изменения количества товаров
    @Query("UPDATE products SET quantity = quantity - :quantityToSubtract WHERE id = :productId AND quantity >= :quantityToSubtract")
    suspend fun decreaseProductQuantity(productId: Long, quantityToSubtract: Int): Int

    @Query("UPDATE products SET quantity = quantity + :quantityToAdd WHERE id = :productId")
    suspend fun increaseProductQuantity(productId: Long, quantityToAdd: Int): Int
}
