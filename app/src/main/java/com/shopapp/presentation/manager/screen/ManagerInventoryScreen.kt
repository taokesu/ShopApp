package com.shopapp.presentation.manager.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.shopapp.data.session.UserSessionManager
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shopapp.data.model.Product
import com.shopapp.data.model.ProductCategory
import com.shopapp.data.model.displayName
import com.shopapp.presentation.common.components.LoadingIndicator
import com.shopapp.presentation.common.components.ProductImageView
import com.shopapp.presentation.common.navigation.LogoutCallback
import com.shopapp.presentation.common.navigation.Screen
import com.shopapp.presentation.manager.viewmodel.ManagerInventoryViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerInventoryScreen(
    navigateToAddProduct: () -> Unit,
    navigateToEditProduct: (Long) -> Unit,
    navController: NavHostController,
    viewModel: ManagerInventoryViewModel = hiltViewModel(),
    logoutCallback: LogoutCallback? = null
) {
    val userSessionManager: UserSessionManager = hiltViewModel<ManagerInventoryViewModel>().userSessionManager
    val uiState by viewModel.uiState.collectAsState()
    val products = uiState.products
    val scope = rememberCoroutineScope()
    
    var showSortOptions by remember { mutableStateOf(false) }
    var showFilterOptions by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProductForAction by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление инвентаризацией") },
                actions = {
                    IconButton(onClick = { showSortOptions = !showSortOptions }) {
                        Icon(Icons.Default.Sort, contentDescription = "Сортировать")
                    }
                    IconButton(onClick = { showFilterOptions = !showFilterOptions }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтровать")
                    }
                    IconButton(onClick = { navigateToAddProduct() }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить товар")
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
            } else if (products.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Нет товаров в инвентаре",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navigateToAddProduct() }) {
                        Text("Добавить новый товар")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    item {
                        if (uiState.showLowStockWarning) {
                            LowStockWarningCard(
                                lowStockCount = uiState.lowStockCount,
                                onClick = { viewModel.loadLowStockProducts() }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    items(products) { product ->
                        ProductInventoryItem(
                            product = product,
                            onEditClick = { navigateToEditProduct(product.id) },
                            onIncreaseQuantity = {
                                scope.launch {
                                    viewModel.increaseProductQuantity(product.id, 1)
                                }
                            },
                            onDecreaseQuantity = {
                                scope.launch {
                                    viewModel.decreaseProductQuantity(product.id, 1)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Сортировка
            if (showSortOptions) {
                SortOptionsDialog(
                    onDismiss = { showSortOptions = false },
                    onSortByName = {
                        viewModel.sortProductsByName()
                        showSortOptions = false
                    },
                    onSortByPriceAsc = {
                        viewModel.sortProductsByPriceAsc()
                        showSortOptions = false
                    },
                    onSortByPriceDesc = {
                        viewModel.sortProductsByPriceDesc()
                        showSortOptions = false
                    },
                    onSortByQuantityAsc = {
                        viewModel.sortProductsByQuantityAsc()
                        showSortOptions = false
                    },
                    onSortByQuantityDesc = {
                        viewModel.sortProductsByQuantityDesc()
                        showSortOptions = false
                    }
                )
            }

            // Фильтрация
            if (showFilterOptions) {
                FilterOptionsDialog(
                    onDismiss = { showFilterOptions = false },
                    onShowAll = {
                        viewModel.loadAllProducts()
                        showFilterOptions = false
                    },
                    onShowLowStock = {
                        viewModel.loadLowStockProducts()
                        showFilterOptions = false
                    },
                    onFilterByCategory = { category ->
                        viewModel.filterProductsByCategory(category)
                        showFilterOptions = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProductInventoryItem(
    product: Product,
    onEditClick: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImageView(
                imageUrl = product.imageUrl,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Категория: ${product.category.displayName}",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Цена: ${product.price} ₽",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "В наличии: ",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${product.quantity}",
                        fontSize = 14.sp,
                        color = if (product.quantity <= 5) Color.Red else Color.Unspecified,
                        fontWeight = if (product.quantity <= 5) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = onIncreaseQuantity) {
                    Icon(Icons.Default.Add, contentDescription = "Увеличить количество")
                }
                
                Text(text = "${product.quantity}", fontSize = 16.sp)
                
                IconButton(
                    onClick = onDecreaseQuantity,
                    enabled = product.quantity > 0
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Уменьшить количество",
                        tint = if (product.quantity > 0) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
            
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать")
            }
        }
    }
}

@Composable
fun LowStockWarningCard(
    lowStockCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Предупреждение",
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Внимание! $lowStockCount ${
                    when {
                        lowStockCount % 10 == 1 && lowStockCount % 100 != 11 -> "товар"
                        lowStockCount % 10 in 2..4 && lowStockCount % 100 !in 12..14 -> "товара"
                        else -> "товаров"
                    }
                } заканчивается на складе",
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SortOptionsDialog(
    onDismiss: () -> Unit,
    onSortByName: () -> Unit,
    onSortByPriceAsc: () -> Unit,
    onSortByPriceDesc: () -> Unit,
    onSortByQuantityAsc: () -> Unit,
    onSortByQuantityDesc: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сортировка") },
        text = {
            Column {
                TextButton(onClick = onSortByName) {
                    Text("По названию")
                }
                TextButton(onClick = onSortByPriceAsc) {
                    Text("По цене (возрастание)")
                }
                TextButton(onClick = onSortByPriceDesc) {
                    Text("По цене (убывание)")
                }
                TextButton(onClick = onSortByQuantityAsc) {
                    Text("По количеству (возрастание)")
                }
                TextButton(onClick = onSortByQuantityDesc) {
                    Text("По количеству (убывание)")
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
fun FilterOptionsDialog(
    onDismiss: () -> Unit,
    onShowAll: () -> Unit,
    onShowLowStock: () -> Unit,
    onFilterByCategory: (ProductCategory) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Фильтры") },
        text = {
            Column {
                TextButton(onClick = onShowAll) {
                    Text("Все товары")
                }
                TextButton(onClick = onShowLowStock) {
                    Text("Заканчивающиеся товары")
                }
                Divider()
                Text(
                    text = "Категории:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                ProductCategory.values().forEach { category ->
                    TextButton(onClick = { onFilterByCategory(category) }) {
                        Text(category.displayName)
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
