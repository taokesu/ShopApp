package com.shopapp.presentation.manager.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.shopapp.data.model.OrderStatus
import com.shopapp.presentation.common.components.LoadingIndicator
import com.shopapp.presentation.manager.viewmodel.ManagerOrdersViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerOrdersScreen(
    navigateToOrderDetails: (Long) -> Unit,
    viewModel: ManagerOrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val orders = uiState.orders
    
    var showFilterOptions by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
    var selectedOrderForStatusChange by remember { mutableStateOf<Order?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление заказами") },
                actions = {
                    IconButton(onClick = { showSortOptions = !showSortOptions }) {
                        Icon(Icons.Default.Sort, contentDescription = "Сортировать")
                    }
                    IconButton(onClick = { showFilterOptions = !showFilterOptions }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтровать")
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
            } else if (orders.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Нет заказов",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(orders) { order ->
                        OrderItem(
                            order = order,
                            onOrderClick = { navigateToOrderDetails(order.id) },
                            onStatusChangeClick = { selectedOrderForStatusChange = order }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Диалог изменения статуса
            selectedOrderForStatusChange?.let { order ->
                StatusChangeDialog(
                    currentStatus = order.status,
                    onDismiss = { selectedOrderForStatusChange = null },
                    onStatusSelected = { newStatus ->
                        viewModel.updateOrderStatus(order.id, newStatus)
                        selectedOrderForStatusChange = null
                    }
                )
            }

            // Сортировка
            if (showSortOptions) {
                SortOrdersDialog(
                    onDismiss = { showSortOptions = false },
                    onSortByDateNewest = {
                        viewModel.sortOrdersByDateDesc()
                        showSortOptions = false
                    },
                    onSortByDateOldest = {
                        viewModel.sortOrdersByDateAsc()
                        showSortOptions = false
                    },
                    onSortByPriceHighest = {
                        viewModel.sortOrdersByTotalDesc()
                        showSortOptions = false
                    },
                    onSortByPriceLowest = {
                        viewModel.sortOrdersByTotalAsc()
                        showSortOptions = false
                    }
                )
            }

            // Фильтрация
            if (showFilterOptions) {
                FilterOrdersDialog(
                    onDismiss = { showFilterOptions = false },
                    onShowAll = {
                        viewModel.loadAllOrders()
                        showFilterOptions = false
                    },
                    onFilterByStatus = { status ->
                        viewModel.filterOrdersByStatus(status)
                        showFilterOptions = false
                    }
                )
            }
        }
    }
}

@Composable
fun OrderItem(
    order: Order,
    onOrderClick: () -> Unit,
    onStatusChangeClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(order.orderDate)
    val statusColor = when (order.status) {
        OrderStatus.PENDING -> Color(0xFFFFA000) // Оранжевый
        OrderStatus.PROCESSING -> Color(0xFF2196F3) // Синий
        OrderStatus.SHIPPED -> Color(0xFF4CAF50) // Зеленый
        OrderStatus.DELIVERED -> Color(0xFF388E3C) // Темно-зеленый
        OrderStatus.CANCELLED -> Color(0xFFF44336) // Красный
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOrderClick() }
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5) // Светлый серый цвет
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Добавляем тень
        border = BorderStroke(1.dp, Color.LightGray) // Добавляем обводку
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Заказ #${order.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                
                // Используем FilterChip вместо Chip
                val statusColor = when (order.status) {
                    OrderStatus.PENDING -> Color(0xFF2196F3) // Blue
                    OrderStatus.PROCESSING -> Color(0xFFFFA000) // Amber
                    OrderStatus.SHIPPED -> Color(0xFF4CAF50) // Green
                    OrderStatus.DELIVERED -> Color(0xFF009688) // Teal
                    OrderStatus.CANCELLED -> Color(0xFFF44336) // Red
                }
                
                FilterChip(
                    selected = false,
                    onClick = { onStatusChangeClick() },
                    label = { Text(order.status.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = statusColor.copy(alpha = 0.2f),
                        labelColor = statusColor
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Форматируем дату заказа
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                // Прямой формат даты, так как order.orderDate уже имеет тип Date
                val formattedDate = dateFormat.format(order.orderDate)
                Text(
                    text = "Дата: $formattedDate",
                    color = Color.Black
                )
                Text(
                    text = "Сумма: ${order.totalAmount} ₽",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Клиент: ${order.userName}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                
                IconButton(onClick = onOrderClick) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Подробности"
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChangeDialog(
    currentStatus: OrderStatus,
    onDismiss: () -> Unit,
    onStatusSelected: (OrderStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить статус заказа") },
        text = {
            Column {
                OrderStatus.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStatusSelected(status) }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = status == currentStatus,
                            onClick = { onStatusSelected(status) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(status.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun SortOrdersDialog(
    onDismiss: () -> Unit,
    onSortByDateNewest: () -> Unit,
    onSortByDateOldest: () -> Unit,
    onSortByPriceHighest: () -> Unit,
    onSortByPriceLowest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сортировка заказов") },
        text = {
            Column {
                TextButton(onClick = onSortByDateNewest) {
                    Text("По дате (сначала новые)")
                }
                TextButton(onClick = onSortByDateOldest) {
                    Text("По дате (сначала старые)")
                }
                TextButton(onClick = onSortByPriceHighest) {
                    Text("По сумме (по убыванию)")
                }
                TextButton(onClick = onSortByPriceLowest) {
                    Text("По сумме (по возрастанию)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun FilterOrdersDialog(
    onDismiss: () -> Unit,
    onShowAll: () -> Unit,
    onFilterByStatus: (OrderStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Фильтрация заказов") },
        text = {
            Column {
                TextButton(onClick = onShowAll) {
                    Text("Все заказы")
                }
                Divider()
                Text(
                    text = "По статусу:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                OrderStatus.values().forEach { status ->
                    TextButton(onClick = { onFilterByStatus(status) }) {
                        Text(status.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
