package com.shopapp.presentation.customer.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import com.shopapp.data.session.UserSessionManager
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shopapp.data.model.Product
import com.shopapp.presentation.common.UiState
import com.shopapp.presentation.common.components.LoadingIndicator
import com.shopapp.presentation.common.components.ProductImageView
import com.shopapp.presentation.customer.viewmodel.ProductDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: Long,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    userSessionManager: UserSessionManager = hiltViewModel<ProductDetailViewModel>().userSessionManager
) {
    val productState by viewModel.productState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val addToCartState by viewModel.addToCartState.collectAsState()
    val favoriteActionState by viewModel.favoriteActionState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var quantity by remember { mutableIntStateOf(1) }
    
    // Проверяем, является ли товар избранным
    LaunchedEffect(Unit) {
        userSessionManager.getCurrentUserId()?.let { currentUserId ->
            viewModel.checkIfFavorite(currentUserId)
        }
    }
    
    // Обрабатываем состояние добавления в корзину
    LaunchedEffect(addToCartState) {
        if (addToCartState is UiState.Success) {
            snackbarHostState.showSnackbar("Товар добавлен в корзину")
            viewModel.resetAddToCartState()
        } else if (addToCartState is UiState.Error) {
            snackbarHostState.showSnackbar((addToCartState as UiState.Error).message)
            viewModel.resetAddToCartState()
        }
    }
    
    // Обрабатываем состояние добавления/удаления из избранного
    LaunchedEffect(favoriteActionState) {
        if (favoriteActionState is UiState.Success) {
            val message = if ((favoriteActionState as UiState.Success).data) {
                "Товар добавлен в избранное"
            } else {
                "Товар удален из избранного"
            }
            snackbarHostState.showSnackbar(message)
            viewModel.resetFavoriteActionState()
        } else if (favoriteActionState is UiState.Error) {
            snackbarHostState.showSnackbar((favoriteActionState as UiState.Error).message)
            viewModel.resetFavoriteActionState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Информация о товаре") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        userSessionManager.getCurrentUserId()?.let { currentUserId ->
                            viewModel.toggleFavorite(currentUserId)
                        }
                    }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Добавить в избранное"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = productState) {
                is UiState.Idle -> {
                    // Показываем начальное состояние деталей товара
                    Text(
                        text = "Загрузка информации о товаре...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is UiState.Success -> {
                    val product = state.data
                    ProductDetailContent(
                        product = product,
                        quantity = quantity,
                        onQuantityChange = { newQuantity -> 
                            if (newQuantity > 0 && newQuantity <= product.quantity) {
                                quantity = newQuantity
                            }
                        },
                        onAddToCartClick = {
                            userSessionManager.getCurrentUserId()?.let { currentUserId ->
                                viewModel.addToCart(currentUserId, quantity)
                            }
                        }
                    )
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
fun ProductDetailContent(
    product: Product,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onAddToCartClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // В реальном приложении здесь должно быть изображение товара
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.name.take(1),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = product.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${product.price} ₽",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "В наличии: ${product.quantity}",
                fontSize = 14.sp,
                color = if (product.quantity <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Категория: ${getCategoryDisplayName(product.category)}",
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Divider()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Описание",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = product.description,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Количество:",
                fontSize = 16.sp
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onQuantityChange(quantity - 1) },
                    enabled = quantity > 1
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Уменьшить")
                }
                
                Text(
                    text = "$quantity",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontSize = 16.sp
                )
                
                IconButton(
                    onClick = { onQuantityChange(quantity + 1) },
                    enabled = quantity < product.quantity
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Увеличить")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddToCartClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = product.quantity > 0
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = "Добавить в корзину",
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Добавить в корзину",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
