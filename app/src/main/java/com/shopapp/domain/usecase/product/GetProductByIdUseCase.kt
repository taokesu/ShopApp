package com.shopapp.domain.usecase.product

import com.shopapp.data.model.Product
import com.shopapp.data.repository.ProductRepository
import javax.inject.Inject

class GetProductByIdUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: Long): Result<Product> {
        return try {
            val product = productRepository.getProductById(productId)
            if (product != null) {
                Result.success(product)
            } else {
                Result.failure(Exception("Товар не найден"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
