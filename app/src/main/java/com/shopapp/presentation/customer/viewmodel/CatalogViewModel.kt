package com.shopapp.presentation.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.Product
import com.shopapp.data.model.ProductCategory
import com.shopapp.data.session.UserSessionManager
import com.shopapp.domain.usecase.product.GetProductsUseCase
import com.shopapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _productsState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val productsState: StateFlow<UiState<List<Product>>> = _productsState

    private var currentCategory: ProductCategory? = null
    private var currentSortType: SortType = SortType.DEFAULT

    init {
        loadProducts()
    }

    fun loadProducts() {
        _productsState.value = UiState.Loading
        
        when {
            currentCategory != null -> {
                getProductsUseCase.byCategory(currentCategory!!)
            }
            currentSortType == SortType.NAME -> {
                getProductsUseCase.byNameOrder()
            }
            currentSortType == SortType.PRICE_ASC -> {
                getProductsUseCase.byPriceAscOrder()
            }
            currentSortType == SortType.PRICE_DESC -> {
                getProductsUseCase.byPriceDescOrder()
            }
            else -> {
                getProductsUseCase()
            }
        }.onEach { products ->
            _productsState.value = UiState.Success(products)
        }.catch { error ->
            _productsState.value = UiState.Error(error.message ?: "Не удалось загрузить товары")
        }.launchIn(viewModelScope)
    }

    fun filterByCategory(category: ProductCategory?) {
        currentCategory = category
        loadProducts()
    }

    fun sortBy(sortType: SortType) {
        currentSortType = sortType
        loadProducts()
    }

    enum class SortType {
        DEFAULT,
        NAME,
        PRICE_ASC,
        PRICE_DESC
    }
}
