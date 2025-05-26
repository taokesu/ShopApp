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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shopapp.presentation.common.UiState
import com.shopapp.presentation.common.navigation.Screen
import com.shopapp.presentation.customer.viewmodel.CartItemWithProduct
import com.shopapp.presentation.customer.viewmodel.CartViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = hiltViewModel(),
    userSessionManager: UserSessionManager = hiltViewModel<CartViewModel>().userSessionManager
) {
    val cartItemsState by viewModel.cartItemsState.collectAsState()
    val totalPrice by viewModel.totalPriceState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearCartDialog by remember { mutableStateOf(false) }
    
    // Получаем ID текущего пользователя из UserSessionManager
    LaunchedEffect(Unit) {
        userSessionManager.getCurrentUserId()?.let { currentUserId ->
            viewModel.loadCartItems(currentUserId)
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
    
    if (showClearCartDialog) {
        AlertDialog(
            onDismissRequest = { showClearCartDialog = false },
            title = { Text("Очистить корзину") },
            text = { Text("Вы уверены, что хотите удалить все товары из корзины?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userSessionManager.getCurrentUserId()?.let { currentUserId ->
                            viewModel.clearCart(currentUserId)
                            showClearCartDialog = false
                        }
                    }
                ) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearCartDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Корзина") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showClearCartDialog = true },
                        enabled = cartItemsState is UiState.Success && (cartItemsState as UiState.Success).data.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистить корзину")
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
            when (val state = cartItemsState) {
                is UiState.Idle -> {
                    // Показываем пустой экран или сообщение о загрузке данных
                    Text(
                        text = "Загрузка данных корзины...",
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
                                text = "Ваша корзина пуста",
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
                        CartContent(
                            cartItems = state.data,
                            totalPrice = totalPrice,
                            onQuantityChanged = { productId, quantity ->
                                userSessionManager.getCurrentUserId()?.let { currentUserId ->
                                    viewModel.updateQuantity(currentUserId, productId, quantity)
                                }
                            },
                            onRemoveItem = { productId ->
                                userSessionManager.getCurrentUserId()?.let { currentUserId ->
                                    viewModel.removeItem(currentUserId, productId)
                                }
                            },
                            onCheckoutClick = {
                                navController.navigate(Screen.CustomerCheckout.route)
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
fun CartContent(
    cartItems: List<CartItemWithProduct>,
    totalPrice: Double,
    onQuantityChanged: (Long, Int) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onCheckoutClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(cartItems) { cartItemWithProduct ->
                CartItemRow(
                    cartItemWithProduct = cartItemWithProduct,
                    onQuantityChanged = onQuantityChanged,
                    onRemoveItem = onRemoveItem
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Итого:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    
                    Text(
                        text = "${formatPrice(totalPrice)} ₽",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onCheckoutClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Оформить заказ",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    cartItemWithProduct: CartItemWithProduct,
    onQuantityChanged: (Long, Int) -> Unit,
    onRemoveItem: (Long) -> Unit
) {
    val cartItem = cartItemWithProduct.cartItem
    val product = cartItemWithProduct.product
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // В реальном приложении здесь должно быть изображение товара
        Box(
            modifier = Modifier
                .size(60.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = product.name.take(1),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${formatPrice(product.price)} ₽",
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onQuantityChanged(product.id, cartItem.quantity - 1) },
                enabled = cartItem.quantity > 1,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Уменьшить",
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Text(
                text = "${cartItem.quantity}",
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            IconButton(
                onClick = { onQuantityChanged(product.id, cartItem.quantity + 1) },
                enabled = cartItem.quantity < product.quantity,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Увеличить",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        IconButton(
            onClick = { onRemoveItem(product.id) }
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Удалить",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

fun formatPrice(price: Double): String {
    val decimalFormat = DecimalFormat("#,##0.00")
    return decimalFormat.format(price)
}
