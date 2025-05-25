package com.shopapp.presentation.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.Product
import com.shopapp.data.model.ProductCategory
import com.shopapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductEditViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductEditUiState())
    val uiState: StateFlow<ProductEditUiState> = _uiState.asStateFlow()

    private var originalProduct: Product? = null

    fun loadProduct(productId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val product = productRepository.getProductById(productId)
                if (product != null) {
                    originalProduct = product
                    _uiState.update {
                        it.copy(
                            productId = product.id,
                            name = product.name,
                            description = product.description,
                            category = product.category,
                            imageUrl = product.imageUrl ?: "",
                            price = product.price.toString(),
                            quantity = product.quantity.toString(),
                            size = product.size ?: "",
                            color = product.color ?: "",
                            isLoading = false,
                            isValid = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Товар не найден",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка загрузки товара: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    suspend fun saveProduct(): Boolean {
        if (!validateInputs()) {
            return false
        }

        _uiState.update { it.copy(isLoading = true) }
        
        return try {
            val currentState = _uiState.value
            val product = Product(
                id = currentState.productId ?: 0,
                name = currentState.name,
                description = currentState.description,
                category = currentState.category ?: ProductCategory.OTHER,
                price = currentState.price.toDoubleOrNull() ?: 0.0,
                quantity = currentState.quantity.toIntOrNull() ?: 0,
                imageUrl = currentState.imageUrl,
                size = currentState.size.takeIf { it.isNotBlank() },
                color = currentState.color.takeIf { it.isNotBlank() }
            )

            if (product.id == 0L) {
                val newId = productRepository.insertProduct(product)
                _uiState.update { it.copy(productId = newId) }
            } else {
                productRepository.updateProduct(product)
            }
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    errorMessage = null
                )
            }
            true
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = "Ошибка сохранения товара: ${e.localizedMessage}",
                    isLoading = false
                )
            }
            false
        }
    }

    suspend fun deleteProduct(): Boolean {
        val productId = _uiState.value.productId ?: return false
        
        _uiState.update { it.copy(isLoading = true) }
        
        return try {
            // Предполагаем, что у ProductRepository есть метод для удаления товара
            // Если его нет, нужно добавить в репозиторий
            val product = productRepository.getProductById(productId)
            if (product != null) {
                // Создаем новый объект с количеством 0
                val updatedProduct = product.copy(quantity = 0)
                productRepository.updateProduct(updatedProduct)
                _uiState.update { it.copy(isLoading = false) }
                true
            } else {
                _uiState.update { 
                    it.copy(
                        errorMessage = "Товар не найден",
                        isLoading = false
                    )
                }
                false
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = "Ошибка удаления товара: ${e.localizedMessage}",
                    isLoading = false
                )
            }
            false
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        var nameError: String? = null
        var descriptionError: String? = null
        var priceError: String? = null
        var quantityError: String? = null

        // Проверка названия
        if (_uiState.value.name.isBlank()) {
            nameError = "Название товара не может быть пустым"
            isValid = false
        }

        // Проверка описания
        if (_uiState.value.description.isBlank()) {
            descriptionError = "Описание товара не может быть пустым"
            isValid = false
        }

        // Проверка цены
        val price = _uiState.value.price.toDoubleOrNull()
        if (price == null || price <= 0) {
            priceError = "Введите корректную цену"
            isValid = false
        }

        // Проверка количества
        val quantity = _uiState.value.quantity.toIntOrNull()
        if (quantity == null || quantity < 0) {
            quantityError = "Введите корректное количество"
            isValid = false
        }

        // Обновление состояния с ошибками
        _uiState.update {
            it.copy(
                nameError = nameError,
                descriptionError = descriptionError,
                priceError = priceError,
                quantityError = quantityError,
                isValid = isValid
            )
        }

        return isValid
    }

    // Методы обновления полей формы
    fun updateName(name: String) {
        _uiState.update { 
            it.copy(
                name = name,
                nameError = if (name.isBlank()) "Название товара не может быть пустым" else null
            )
        }
        checkFormValidity()
    }

    fun updateDescription(description: String) {
        _uiState.update { 
            it.copy(
                description = description,
                descriptionError = if (description.isBlank()) "Описание товара не может быть пустым" else null
            )
        }
        checkFormValidity()
    }

    fun updateCategory(category: ProductCategory) {
        _uiState.update { it.copy(category = category) }
        checkFormValidity()
    }

    fun updateImageUrl(imageUrl: String) {
        _uiState.update { it.copy(imageUrl = imageUrl) }
    }

    fun updatePrice(price: String) {
        val priceValue = price.toDoubleOrNull()
        _uiState.update { 
            it.copy(
                price = price,
                priceError = if (priceValue == null || priceValue <= 0) "Введите корректную цену" else null
            )
        }
        checkFormValidity()
    }

    fun updateQuantity(quantity: String) {
        val quantityValue = quantity.toIntOrNull()
        _uiState.update { 
            it.copy(
                quantity = quantity,
                quantityError = if (quantityValue == null || quantityValue < 0) "Введите корректное количество" else null
            )
        }
        checkFormValidity()
    }

    fun updateSize(size: String) {
        _uiState.update { it.copy(size = size) }
    }

    fun updateColor(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    private fun checkFormValidity() {
        val currentState = _uiState.value
        val isValid = currentState.name.isNotBlank() &&
                currentState.description.isNotBlank() &&
                currentState.category != null &&
                currentState.price.toDoubleOrNull() != null &&
                (currentState.price.toDoubleOrNull() ?: 0.0) > 0 &&
                currentState.quantity.toIntOrNull() != null &&
                (currentState.quantity.toIntOrNull() ?: -1) >= 0

        _uiState.update { it.copy(isValid = isValid) }
    }
}

data class ProductEditUiState(
    val productId: Long? = null,
    val name: String = "",
    val nameError: String? = null,
    val description: String = "",
    val descriptionError: String? = null,
    val category: ProductCategory? = null,
    val imageUrl: String = "",
    val price: String = "",
    val priceError: String? = null,
    val quantity: String = "0",
    val quantityError: String? = null,
    val size: String = "",
    val color: String = "",
    val isLoading: Boolean = false,
    val isValid: Boolean = false,
    val errorMessage: String? = null
)
