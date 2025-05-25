package com.shopapp.domain.usecase.product

import com.shopapp.data.model.Product
import com.shopapp.data.model.ProductCategory
import com.shopapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    // Для покупателей - только доступные товары
    operator fun invoke(): Flow<List<Product>> {
        return productRepository.getAvailableProducts()
    }

    fun byCategory(category: ProductCategory): Flow<List<Product>> {
        return productRepository.getAvailableProductsByCategory(category)
    }

    fun byNameOrder(): Flow<List<Product>> {
        return productRepository.getProductsOrderedByName()
    }

    fun byPriceAscOrder(): Flow<List<Product>> {
        return productRepository.getProductsOrderedByPriceAsc()
    }

    fun byPriceDescOrder(): Flow<List<Product>> {
        return productRepository.getProductsOrderedByPriceDesc()
    }
    
    // Для менеджеров - все товары, включая те, которых нет в наличии
    fun getAllProducts(): Flow<List<Product>> {
        return productRepository.getAllProducts()
    }
    
    fun getProductsByCategory(category: ProductCategory): Flow<List<Product>> {
        return productRepository.getProductsByCategory(category)
    }
    
    fun getAllProductsOrderedByName(): Flow<List<Product>> {
        return productRepository.getAllProductsOrderedByName()
    }
    
    fun getAllProductsOrderedByPriceAsc(): Flow<List<Product>> {
        return productRepository.getAllProductsOrderedByPriceAsc()
    }
    
    fun getAllProductsOrderedByPriceDesc(): Flow<List<Product>> {
        return productRepository.getAllProductsOrderedByPriceDesc()
    }
    
    fun getProductsOrderedByQuantityAsc(): Flow<List<Product>> {
        return productRepository.getProductsOrderedByQuantityAsc()
    }
    
    fun getProductsOrderedByQuantityDesc(): Flow<List<Product>> {
        return productRepository.getProductsOrderedByQuantityDesc()
    }
    
    fun getLowStockProducts(): Flow<List<Product>> {
        return productRepository.getLowStockProducts()
    }
}
