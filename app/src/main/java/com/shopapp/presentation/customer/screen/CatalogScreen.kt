package com.shopapp.presentation.customer.screen

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import com.shopapp.data.session.UserSessionManager
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shopapp.data.model.Product
import com.shopapp.data.model.ProductCategory
import com.shopapp.presentation.common.UiState
import com.shopapp.presentation.common.navigation.LogoutCallback
import com.shopapp.presentation.common.navigation.Screen
import com.shopapp.presentation.customer.viewmodel.CatalogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    navController: NavHostController,
    viewModel: CatalogViewModel = hiltViewModel(),
    logoutCallback: LogoutCallback? = null
) {
    val userSessionManager: UserSessionManager = hiltViewModel<CatalogViewModel>().userSessionManager
    val productsState by viewModel.productsState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showSortDropdown by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Каталог товаров") },
                actions = {
                    IconButton(onClick = { showCategoryDropdown = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтр по категориям")
                    }
                    IconButton(onClick = { showSortDropdown = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Сортировка")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
            
            // Выпадающее меню категорий
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                DropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Все категории") },
                        onClick = {
                            viewModel.filterByCategory(null)
                            showCategoryDropdown = false
                        }
                    )
                    
                    ProductCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(getCategoryDisplayName(category)) },
                            onClick = {
                                viewModel.filterByCategory(category)
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Выпадающее меню сортировки
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                DropdownMenu(
                    expanded = showSortDropdown,
                    onDismissRequest = { showSortDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("По умолчанию") },
                        onClick = {
                            viewModel.sortBy(CatalogViewModel.SortType.DEFAULT)
                            showSortDropdown = false
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("По названию") },
                        onClick = {
                            viewModel.sortBy(CatalogViewModel.SortType.NAME)
                            showSortDropdown = false
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("По возрастанию цены") },
                        onClick = {
                            viewModel.sortBy(CatalogViewModel.SortType.PRICE_ASC)
                            showSortDropdown = false
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("По убыванию цены") },
                        onClick = {
                            viewModel.sortBy(CatalogViewModel.SortType.PRICE_DESC)
                            showSortDropdown = false
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = productsState) {
                is UiState.Idle -> {
                    // Показываем начальное состояние каталога
                    Text(
                        text = "Загрузка каталога...",
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
                        Text(
                            text = "Нет доступных товаров",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        ProductList(
                            products = state.data,
                            onProductClick = { product ->
                                navController.navigate(Screen.CustomerProductDetail.createRoute(product.id))
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
fun ProductList(
    products: List<Product>,
    onProductClick: (Product) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            ProductItem(
                product = product,
                onClick = { onProductClick(product) }
            )
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // В реальном приложении здесь должен быть код для загрузки изображений
            // Например, с использованием Coil:
            // Image(
            //     painter = rememberCoilPainter(request = product.imageUrl),
            //     contentDescription = product.name,
            //     modifier = Modifier.size(80.dp),
            //     contentScale = ContentScale.Crop
            // )
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.name.take(1),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Категория: ${getCategoryDisplayName(product.category)}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${product.price} ₽",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (product.quantity <= 5 && product.quantity > 0) {
                        Text(
                            text = "Осталось: ${product.quantity} шт.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

fun getCategoryDisplayName(category: ProductCategory): String {
    return when (category) {
        ProductCategory.SHIRTS -> "Рубашки"
        ProductCategory.PANTS -> "Брюки"
        ProductCategory.DRESSES -> "Платья"
        ProductCategory.OUTERWEAR -> "Верхняя одежда"
        ProductCategory.SHOES -> "Обувь"
        ProductCategory.ACCESSORIES -> "Аксессуары"
        ProductCategory.UNDERWEAR -> "Нижнее белье"
        ProductCategory.SPORTSWEAR -> "Спортивная одежда"
        ProductCategory.OTHER -> "Другое"
    }
}
