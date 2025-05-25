package com.shopapp.presentation.customer.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.Product
import com.shopapp.domain.usecase.cart.AddToCartUseCase
import com.shopapp.domain.usecase.favorite.AddToFavoritesUseCase
import com.shopapp.domain.usecase.favorite.RemoveFromFavoritesUseCase
import com.shopapp.domain.usecase.product.GetProductByIdUseCase
import com.shopapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _productState = MutableStateFlow<UiState<Product>>(UiState.Loading)
    val productState: StateFlow<UiState<Product>> = _productState

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _addToCartState = MutableStateFlow<UiState<Boolean>?>(null)
    val addToCartState: StateFlow<UiState<Boolean>?> = _addToCartState

    private val _favoriteActionState = MutableStateFlow<UiState<Boolean>?>(null)
    val favoriteActionState: StateFlow<UiState<Boolean>?> = _favoriteActionState

    private val productId: Long = checkNotNull(savedStateHandle["productId"])

    init {
        loadProduct()
    }

    fun loadProduct() {
        viewModelScope.launch {
            _productState.value = UiState.Loading
            
            getProductByIdUseCase(productId)
                .onSuccess { product ->
                    _productState.value = UiState.Success(product)
                }
                .onFailure { error ->
                    _productState.value = UiState.Error(error.message ?: "Не удалось загрузить товар")
                }
        }
    }

    fun addToCart(userId: Long, quantity: Int = 1) {
        _addToCartState.value = UiState.Loading
        
        viewModelScope.launch {
            addToCartUseCase(userId, productId, quantity)
                .onSuccess {
                    _addToCartState.value = UiState.Success(true)
                }
                .onFailure { error ->
                    _addToCartState.value = UiState.Error(error.message ?: "Не удалось добавить товар в корзину")
                }
        }
    }

    fun toggleFavorite(userId: Long) {
        _favoriteActionState.value = UiState.Loading
        
        viewModelScope.launch {
            if (_isFavorite.value) {
                removeFromFavoritesUseCase(userId, productId)
                    .onSuccess {
                        _isFavorite.value = false
                        _favoriteActionState.value = UiState.Success(false)
                    }
                    .onFailure { error ->
                        _favoriteActionState.value = UiState.Error(error.message ?: "Не удалось удалить товар из избранного")
                    }
            } else {
                addToFavoritesUseCase(userId, productId)
                    .onSuccess {
                        _isFavorite.value = true
                        _favoriteActionState.value = UiState.Success(true)
                    }
                    .onFailure { error ->
                        _favoriteActionState.value = UiState.Error(error.message ?: "Не удалось добавить товар в избранное")
                    }
            }
        }
    }

    fun resetAddToCartState() {
        _addToCartState.value = null
    }

    fun resetFavoriteActionState() {
        _favoriteActionState.value = null
    }

    fun checkIfFavorite(userId: Long) {
        // В реальном приложении здесь должен быть запрос к репозиторию для проверки,
        // находится ли товар в избранном
        // Пример:
        // favoriteRepository.isProductFavorite(userId, productId)
        //     .onEach { isFavorite -> _isFavorite.value = isFavorite }
        //     .catch { }
        //     .launchIn(viewModelScope)
    }
}
