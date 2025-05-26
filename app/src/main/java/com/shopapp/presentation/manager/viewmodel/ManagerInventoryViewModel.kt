package com.shopapp.presentation.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.Product
import com.shopapp.data.model.ProductCategory
import com.shopapp.data.session.UserSessionManager
import com.shopapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagerInventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerInventoryUiState())
    val uiState: StateFlow<ManagerInventoryUiState> = _uiState.asStateFlow()

    init {
        loadAllProducts()
        checkLowStockProducts()
    }

    fun loadAllProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                productRepository.getAllProducts().collect { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка загрузки товаров: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun loadLowStockProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                productRepository.getLowStockProducts().collect { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка загрузки товаров: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private fun checkLowStockProducts() {
        viewModelScope.launch {
            try {
                productRepository.getLowStockProducts().collect { products ->
                    _uiState.update {
                        it.copy(
                            showLowStockWarning = products.isNotEmpty(),
                            lowStockCount = products.size
                        )
                    }
                }
            } catch (e: Exception) {
                // Если произошла ошибка, не показываем предупреждение
                _uiState.update {
                    it.copy(
                        showLowStockWarning = false,
                        lowStockCount = 0
                    )
                }
            }
        }
    }
    
    fun filterProductsByCategory(category: ProductCategory) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                productRepository.getProductsByCategory(category).collect { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка фильтрации товаров: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun sortProductsByName() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                productRepository.getAllProductsOrderedByName().collect { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка сортировки товаров: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun sortProductsByPriceAsc() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                productRepository.getAllProductsOrderedByPriceAsc().collect { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка сортировки товаров: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun sortProductsByPriceDesc() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                productRepository.getAllProductsOrderedByPriceDesc().collect { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка сортировки товаров: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun sortProductsByQuantityAsc() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                productRepository.getProductsOrderedByQuantityAsc().collect { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка сортировки товаров: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun sortProductsByQuantityDesc() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                productRepository.getProductsOrderedByQuantityDesc().collect { products ->
                    _uiState.update {
                        it.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка сортировки товаров: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    suspend fun increaseProductQuantity(productId: Long, amount: Int): Boolean {
        return try {
            val result = productRepository.increaseProductQuantity(productId, amount)
            checkLowStockProducts()
            result
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = "Ошибка увеличения количества товара: ${e.localizedMessage}"
                )
            }
            false
        }
    }
    
    suspend fun decreaseProductQuantity(productId: Long, amount: Int): Boolean {
        return try {
            val result = productRepository.decreaseProductQuantity(productId, amount)
            checkLowStockProducts()
            result
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = "Ошибка уменьшения количества товара: ${e.localizedMessage}"
                )
            }
            false
        }
    }
    
    fun clearMessage() {
        _uiState.update {
            it.copy(errorMessage = null, successMessage = null)
        }
    }
}

data class ManagerInventoryUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showLowStockWarning: Boolean = false,
    val lowStockCount: Int = 0
)
