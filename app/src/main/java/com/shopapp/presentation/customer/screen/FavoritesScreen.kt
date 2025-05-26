package com.shopapp.presentation.customer.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import com.shopapp.data.session.UserSessionManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shopapp.presentation.common.UiState
import com.shopapp.presentation.common.navigation.Screen
import com.shopapp.presentation.customer.viewmodel.FavoriteItemWithProduct
import com.shopapp.presentation.customer.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = hiltViewModel(),
    userSessionManager: UserSessionManager = hiltViewModel<FavoritesViewModel>().userSessionManager
) {
    val favoritesState by viewModel.favoritesState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Получаем ID текущего пользователя из UserSessionManager
    LaunchedEffect(Unit) {
        userSessionManager.getCurrentUserId()?.let { currentUserId ->
            viewModel.loadFavorites(currentUserId)
        }
    }
    
    LaunchedEffect(actionState) {
        if (actionState is UiState.Success) {
            snackbarHostState.showSnackbar((actionState as UiState.Success).data)
            viewModel.resetActionState()
        } else if (actionState is UiState.Error) {
            snackbarHostState.showSnackbar((actionState as UiState.Error).message)
            viewModel.resetActionState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Избранное") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = favoritesState) {
                is UiState.Idle -> {
                    // Показываем начальное состояние избранного
                    Text(
                        text = "Загрузка избранных товаров...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "У вас нет избранных товаров",
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { navController.navigate(Screen.CustomerCatalog.route) }
                            ) {
                                Text("Перейти в каталог")
                            }
                        }
                    } else {
                        FavoritesList(
                            favorites = state.data,
                            onFavoriteClick = { productId ->
                                navController.navigate(Screen.CustomerProductDetail.createRoute(productId))
                            },
                            onRemoveClick = { productId ->
                                userSessionManager.getCurrentUserId()?.let { currentUserId ->
                                    viewModel.removeFromFavorites(currentUserId, productId)
                                }
                            },
                            onAddToCartClick = { productId ->
                                userSessionManager.getCurrentUserId()?.let { currentUserId ->
                                    viewModel.addToCart(currentUserId, productId)
                                }
                            }
                        )
                    }
                }
                
                is UiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesList(
    favorites: List<FavoriteItemWithProduct>,
    onFavoriteClick: (Long) -> Unit,
    onRemoveClick: (Long) -> Unit,
    onAddToCartClick: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(favorites) { favoriteWithProduct ->
            FavoriteItem(
                favoriteWithProduct = favoriteWithProduct,
                onClick = { onFavoriteClick(favoriteWithProduct.product.id) },
                onRemoveClick = { onRemoveClick(favoriteWithProduct.product.id) },
                onAddToCartClick = { onAddToCartClick(favoriteWithProduct.product.id) }
            )
        }
    }
}

@Composable
fun FavoriteItem(
    favoriteWithProduct: FavoriteItemWithProduct,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onAddToCartClick: () -> Unit
) {
    val product = favoriteWithProduct.product
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // В реальном приложении здесь должно быть изображение товара
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = product.name.take(1),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Категория: ${getCategoryDisplayName(product.category)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${formatPrice(product.price)} ₽",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onAddToCartClick,
                    enabled = product.quantity > 0
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Добавить в корзину",
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text("В корзину")
                }
                
                IconButton(onClick = onRemoveClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить из избранного",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
