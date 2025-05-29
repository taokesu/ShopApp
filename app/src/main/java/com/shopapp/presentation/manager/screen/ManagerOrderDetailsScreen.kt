package com.shopapp.presentation.manager.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shopapp.data.model.Order
import com.shopapp.data.model.OrderItem
import com.shopapp.data.model.OrderItemWithProduct
import com.shopapp.data.model.OrderStatus
import com.shopapp.presentation.common.components.LoadingIndicator
import com.shopapp.presentation.common.components.ProductImageView
import com.shopapp.presentation.manager.viewmodel.ManagerOrdersViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerOrderDetailsScreen(
    orderId: Long,
    navigateBack: () -> Unit,
    viewModel: ManagerOrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showStatusChangeDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(orderId) {
        viewModel.loadOrderDetails(orderId)
    }
    
    val order = uiState.currentOrder
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали заказа #$orderId") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showStatusChangeDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Изменить статус")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingIndicator()
            } else if (order == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Заказ не найден",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = navigateBack) {
                        Text("Вернуться к списку заказов")
                    }
                }
            } else {
                OrderDetailsContent(
                    order = order,
                    onStatusChangeClick = { showStatusChangeDialog = true }
                )
            }
            
            if (showStatusChangeDialog && order != null) {
                StatusChangeDialog(
                    currentStatus = order.status,
                    onDismiss = { showStatusChangeDialog = false },
                    onStatusSelected = { newStatus ->
                        viewModel.updateOrderStatus(orderId, newStatus)
                        showStatusChangeDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun OrderDetailsContent(
    order: Order,
    onStatusChangeClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(order.orderDate)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Информация о заказе
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Информация о заказе",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OrderInfoRow("Номер заказа:", "#${order.id}")
                    OrderInfoRow("Дата заказа:", formattedDate)
                    OrderInfoRow("Клиент:", order.userName)
                    OrderInfoRow("Телефон:", order.userPhone ?: "Не указан")
                    OrderInfoRow("Адрес доставки:", order.deliveryAddress ?: "Не указан")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Статус заказа:",
                            fontWeight = FontWeight.Bold
                        )
                        
                        FilterChip(
                            selected = false,
                            onClick = { onStatusChangeClick() },
                            label = { Text(order.status.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = when (order.status) {
                                    OrderStatus.PENDING -> androidx.compose.ui.graphics.Color(0xFFFFA000).copy(alpha = 0.2f)
                                    OrderStatus.PROCESSING -> androidx.compose.ui.graphics.Color(0xFF2196F3).copy(alpha = 0.2f)
                                    OrderStatus.SHIPPED -> androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    OrderStatus.DELIVERED -> androidx.compose.ui.graphics.Color(0xFF388E3C).copy(alpha = 0.2f)
                                    OrderStatus.CANCELLED -> androidx.compose.ui.graphics.Color(0xFFF44336).copy(alpha = 0.2f)
                                },
                                labelColor = when (order.status) {
                                    OrderStatus.PENDING -> androidx.compose.ui.graphics.Color(0xFFFFA000)
                                    OrderStatus.PROCESSING -> androidx.compose.ui.graphics.Color(0xFF2196F3)
                                    OrderStatus.SHIPPED -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                    OrderStatus.DELIVERED -> androidx.compose.ui.graphics.Color(0xFF388E3C)
                                    OrderStatus.CANCELLED -> androidx.compose.ui.graphics.Color(0xFFF44336)
                                }
                            )
                        )
                    }
                }
            }
        }
        
        // Товары в заказе
        item {
            Text(
                text = "Товары в заказе",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(order.items) { item ->
            OrderItemCard(item = item)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Итоговая информация
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5) // Светлый серый цвет
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Итого",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Количество товаров:",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${order.items.sumOf { it.orderItem.quantity }} шт."
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Стоимость товаров:",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${order.totalAmount} ₽"
                        )
                    }
                    
                    if (order.deliveryPrice != null && order.deliveryPrice > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Стоимость доставки:",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${order.deliveryPrice} ₽"
                            )
                        }
                    }
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ИТОГО К ОПЛАТЕ:",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${order.totalAmount + (order.deliveryPrice ?: 0.0)} ₽",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OrderInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium
        )
        Text(text = value)
    }
}

@Composable
fun OrderItemCard(item: OrderItemWithProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5) // Светлый серый цвет
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImageView(
                imageUrl = item.product.imageUrl,
                modifier = Modifier.size(60.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                if (!item.product.size.isNullOrBlank() || !item.product.color.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            if (!item.product.size.isNullOrBlank()) append("Размер: ${item.product.size}")
                            if (!item.product.size.isNullOrBlank() && !item.product.color.isNullOrBlank()) append(" | ")
                            if (!item.product.color.isNullOrBlank()) append("Цвет: ${item.product.color}")
                        },
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${item.orderItem.quantity} × ${item.product.price} ₽",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${item.orderItem.quantity * item.product.price} ₽",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
