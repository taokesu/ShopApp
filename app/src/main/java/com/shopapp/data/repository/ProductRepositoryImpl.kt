package com.shopapp.data.repository

import com.shopapp.data.local.ProductDao
import com.shopapp.data.model.Product
import com.shopapp.data.model.ProductCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    // Общие методы работы с товарами
    override suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(product)
    }

    override suspend fun insertProducts(products: List<Product>) {
        productDao.insertProducts(products)
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    override suspend fun getProductById(productId: Long): Product? {
        return productDao.getProductById(productId)
    }

    // Методы для покупателей (только доступные товары)
    override fun getAvailableProducts(): Flow<List<Product>> {
        return productDao.getAvailableProducts()
    }

    override fun getAvailableProductsByCategory(category: ProductCategory): Flow<List<Product>> {
        return productDao.getAvailableProductsByCategory(category)
    }

    override fun getProductsOrderedByName(): Flow<List<Product>> {
        return productDao.getProductsOrderedByName()
    }

    override fun getProductsOrderedByPriceAsc(): Flow<List<Product>> {
        return productDao.getProductsOrderedByPriceAsc()
    }

    override fun getProductsOrderedByPriceDesc(): Flow<List<Product>> {
        return productDao.getProductsOrderedByPriceDesc()
    }
    
    // Методы для менеджеров (все товары, включая отсутствующие в наличии)
    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }
    
    override fun getProductsByCategory(category: ProductCategory): Flow<List<Product>> {
        return productDao.getProductsByCategory(category)
    }
    
    override fun getAllProductsOrderedByName(): Flow<List<Product>> {
        return productDao.getAllProductsOrderedByName()
    }
    
    override fun getAllProductsOrderedByPriceAsc(): Flow<List<Product>> {
        return productDao.getAllProductsOrderedByPriceAsc()
    }
    
    override fun getAllProductsOrderedByPriceDesc(): Flow<List<Product>> {
        return productDao.getAllProductsOrderedByPriceDesc()
    }

    // Общие методы сортировки по количеству (для менеджеров)
    override fun getProductsOrderedByQuantityAsc(): Flow<List<Product>> {
        return productDao.getProductsOrderedByQuantityAsc()
    }

    override fun getProductsOrderedByQuantityDesc(): Flow<List<Product>> {
        return productDao.getProductsOrderedByQuantityDesc()
    }

    override fun getLowStockProducts(): Flow<List<Product>> {
        return productDao.getLowStockProducts()
    }

    // Методы изменения количества товаров
    override suspend fun decreaseProductQuantity(productId: Long, quantityToSubtract: Int): Boolean {
        return productDao.decreaseProductQuantity(productId, quantityToSubtract) > 0
    }

    override suspend fun increaseProductQuantity(productId: Long, quantityToAdd: Int): Boolean {
        return productDao.increaseProductQuantity(productId, quantityToAdd) > 0
    }
}
