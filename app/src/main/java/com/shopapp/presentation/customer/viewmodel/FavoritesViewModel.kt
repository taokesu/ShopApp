package com.shopapp.presentation.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.FavoriteItem
import com.shopapp.data.model.Product
import com.shopapp.domain.usecase.cart.AddToCartUseCase
import com.shopapp.domain.usecase.favorite.GetFavoriteItemsUseCase
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

data class FavoriteItemWithProduct(
    val favoriteItem: FavoriteItem,
    val product: Product
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoriteItemsUseCase: GetFavoriteItemsUseCase,
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val addToCartUseCase: AddToCartUseCase
) : ViewModel() {

    private val _favoritesState = MutableStateFlow<UiState<List<FavoriteItemWithProduct>>>(UiState.Loading)
    val favoritesState: StateFlow<UiState<List<FavoriteItemWithProduct>>> = _favoritesState

    private val _actionState = MutableStateFlow<UiState<String>?>(null)
    val actionState: StateFlow<UiState<String>?> = _actionState

    fun loadFavorites(userId: Long) {
        _favoritesState.value = UiState.Loading
        
        getFavoriteItemsUseCase(userId)
            .onEach { favoriteItems ->
                if (favoriteItems.isEmpty()) {
                    _favoritesState.value = UiState.Success(emptyList())
                    return@onEach
                }
                
                val favoriteItemsWithProducts = mutableListOf<FavoriteItemWithProduct>()
                
                for (favoriteItem in favoriteItems) {
                    val productResult = getProductByIdUseCase(favoriteItem.productId)
                    productResult.onSuccess { product ->
                        favoriteItemsWithProducts.add(FavoriteItemWithProduct(favoriteItem, product))
                    }
                }
                
                _favoritesState.value = UiState.Success(favoriteItemsWithProducts)
            }
            .catch { error ->
                _favoritesState.value = UiState.Error(error.message ?: "Не удалось загрузить избранные товары")
            }
            .launchIn(viewModelScope)
    }

    fun removeFromFavorites(userId: Long, productId: Long) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            
            removeFromFavoritesUseCase(userId, productId)
                .onSuccess {
                    loadFavorites(userId)
                    _actionState.value = UiState.Success("Товар удален из избранного")
                }
                .onFailure { error ->
                    _actionState.value = UiState.Error(error.message ?: "Не удалось удалить товар из избранного")
                }
        }
    }

    fun addToCart(userId: Long, productId: Long, quantity: Int = 1) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            
            addToCartUseCase(userId, productId, quantity)
                .onSuccess {
                    _actionState.value = UiState.Success("Товар добавлен в корзину")
                }
                .onFailure { error ->
                    _actionState.value = UiState.Error(error.message ?: "Не удалось добавить товар в корзину")
                }
        }
    }

    fun resetActionState() {
        _actionState.value = null
    }
}
