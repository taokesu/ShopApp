package com.shopapp.presentation.customer.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shopapp.data.model.OrderStatus
import com.shopapp.data.model.OrderItemWithProduct
import com.shopapp.presentation.common.UiState
import com.shopapp.presentation.customer.viewmodel.OrderWithItems
import com.shopapp.presentation.customer.viewmodel.OrdersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    navController: NavController,
    viewModel: OrdersViewModel = hiltViewModel(),
    orderId: Long
) {
    val orderDetailsState by viewModel.orderDetailsState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    
    LaunchedEffect(orderId) {
        viewModel.loadOrderDetails(orderId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали заказа #$orderId") },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.resetOrderDetailsState()
                        navController.navigateUp() 
                    }) {
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
            when (val state = orderDetailsState) {
                is UiState.Idle -> {
                    // Показываем начальное состояние деталей заказа
                    Text(
                        text = "Загрузка информации о заказе...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is UiState.Success -> {
                    OrderDetailsContent(
                        orderWithItems = state.data,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(scrollState)
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
                
                null -> {
                    // Ничего не отображаем, состояние еще не инициализировано
                }
            }
        }
    }
}

@Composable
fun OrderDetailsContent(
    orderWithItems: OrderWithItems,
    modifier: Modifier = Modifier
) {
    val order = orderWithItems.order
    val items = orderWithItems.items
    
    Column(modifier = modifier) {
        // Информация о заказе
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Информация о заказе",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Номер заказа:",
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "#${order.id}"
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Дата заказа:",
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = formatDate(order.orderDate)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Статус:",
                        fontWeight = FontWeight.Medium
                    )
                    
                    OrderStatusBadge(status = order.status)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Итоговая сумма:",
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "${formatPrice(order.totalAmount)} ₽",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Контактная информация
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Контактная информация",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "ФИО: ${order.userName}",
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                
                Text(
                    text = "Телефон: ${order.userPhone ?: "-"}",
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                
                Text(
                    text = "Email: ${order.userEmail ?: "-"}",
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                
                Text(
                    text = "Адрес: ${order.deliveryAddress ?: "-"}",
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Товары в заказе
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Товары в заказе",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                items.forEach { orderItemWithProduct ->
                    OrderItemRow(orderItemWithProduct = orderItemWithProduct)
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Итого:",
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${formatPrice(order.totalAmount)} ₽",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(
    orderItemWithProduct: OrderItemWithProduct
) {
    val orderItem = orderItemWithProduct.orderItem
    val product = orderItemWithProduct.product
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // В реальном приложении здесь должно быть изображение товара
        Box(
            modifier = Modifier
                .size(50.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = product.name.take(1),
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = product.name,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "Категория: ${getCategoryDisplayName(product.category)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${formatPrice(orderItem.pricePerItem)} ₽",
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "x${orderItem.quantity}",
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "${formatPrice(orderItem.pricePerItem * orderItem.quantity)} ₽",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
