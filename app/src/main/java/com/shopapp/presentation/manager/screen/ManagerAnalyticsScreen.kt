package com.shopapp.presentation.manager.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shopapp.data.model.ProductCategory
import com.shopapp.data.model.ProductSalesInfo
import com.shopapp.data.model.displayName
import com.shopapp.presentation.common.components.LoadingIndicator
import com.shopapp.presentation.common.components.ProductImageView
import com.shopapp.presentation.manager.viewmodel.ManagerAnalyticsViewModel
import java.text.NumberFormat
import java.util.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerAnalyticsScreen(
    viewModel: ManagerAnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val locale = Locale("ru", "RU")
    val currencyFormat = NumberFormat.getCurrencyInstance(locale).apply {
        maximumFractionDigits = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аналитика продаж") }
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
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Общая статистика
                    item {
                        Text(
                            text = "Общая статистика",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        StatisticsGrid(
                            totalSales = currencyFormat.format(uiState.totalSales),
                            totalOrders = uiState.totalCompletedOrders.toString(),
                            averageOrder = currencyFormat.format(uiState.averageOrderValue)
                        )
                    }
                    
                    // Продажи по периодам
                    item {
                        Text(
                            text = "Продажи по периодам",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                        )
                        
                        PeriodSalesCards(
                            todaySales = currencyFormat.format(uiState.todaySales),
                            weekSales = currencyFormat.format(uiState.weekSales),
                            monthSales = currencyFormat.format(uiState.monthSales)
                        )
                    }
                    
                    // График продаж по дням недели
                    item {
                        Text(
                            text = "Продажи по дням недели",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                        )
                        
                        WeeklySalesChart(
                            salesData = uiState.salesByDayOfWeek,
                            currencyFormat = currencyFormat
                        )
                    }
                    
                    // Популярные товары
                    item {
                        Text(
                            text = "Самые популярные товары",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                        )
                    }
                    
                    if (uiState.topSellingProducts.isEmpty()) {
                        item {
                            Text(
                                text = "Нет данных о продажах",
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(uiState.topSellingProducts) { productInfo ->
                            TopSellingProductItem(
                                productInfo = productInfo,
                                currencyFormat = currencyFormat
                            )
                        }
                    }
                    
                    // Продажи по категориям
                    item {
                        Text(
                            text = "Продажи по категориям",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                        )
                        
                        CategorySalesChart(
                            salesByCategory = uiState.salesByCategory,
                            currencyFormat = currencyFormat
                        )
                    }
                    
                    // Пустое пространство внизу
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Отображение ошибки
            if (uiState.errorMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(text = uiState.errorMessage!!)
                }
            }
        }
    }
}

@Composable
fun StatisticsGrid(
    totalSales: String,
    totalOrders: String,
    averageOrder: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Общие продажи",
                value = totalSales,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary
            )
            
            StatCard(
                title = "Заказов",
                value = totalOrders,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        StatCard(
            title = "Средний чек",
            value = averageOrder,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5) // Светлый серый цвет
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun PeriodSalesCards(
    todaySales: String,
    weekSales: String,
    monthSales: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PeriodCard(
            title = "Сегодня",
            value = todaySales,
            modifier = Modifier.weight(1f),
            color = Color(0xFF6200EA)
        )
        
        PeriodCard(
            title = "Неделя",
            value = weekSales,
            modifier = Modifier.weight(1f),
            color = Color(0xFF0091EA)
        )
        
        PeriodCard(
            title = "Месяц",
            value = monthSales,
            modifier = Modifier.weight(1f),
            color = Color(0xFF00BFA5)
        )
    }
}

@Composable
fun PeriodCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5) // Светлый серый цвет
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun WeeklySalesChart(
    salesData: Map<String, Double>,
    currencyFormat: NumberFormat
) {
    val sortedDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val maxValue = salesData.values.maxOrNull() ?: 0.0
    val normalizedData = sortedDays.map { day ->
        day to (salesData[day] ?: 0.0) / if (maxValue > 0) maxValue else 1.0
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5) // Светлый серый цвет
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Ось Y и вертикальная линия
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 32.dp, end = 16.dp)
            ) {
                // Y-ось с метками
                Column(
                    modifier = Modifier
                        .width(50.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = currencyFormat.format(maxValue),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = currencyFormat.format(maxValue / 2),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "0",
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Горизонтальные линии сетки
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        
                        // Рисуем горизонтальные линии
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, 0f),
                            end = Offset(canvasWidth, 0f),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, canvasHeight / 2),
                            end = Offset(canvasWidth, canvasHeight / 2),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, canvasHeight),
                            end = Offset(canvasWidth, canvasHeight),
                            strokeWidth = 1f
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        normalizedData.forEach { (day, normalizedValue) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                val barHeight = max(0.05f, normalizedValue.toFloat())
                                
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .fillMaxHeight(barHeight)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = day,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopSellingProductItem(
    productInfo: ProductSalesInfo,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                imageUrl = productInfo.product.imageUrl,
                modifier = Modifier.size(60.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = productInfo.product.name,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Продано: ${productInfo.quantitySold} шт",
                    fontSize = 14.sp
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = currencyFormat.format(productInfo.totalRevenue),
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val trendIcon = if (productInfo.trend > 0) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                }
                
                val trendColor = if (productInfo.trend > 0) {
                    Color.Green
                } else {
                    Color.Red
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = "Тренд",
                        tint = trendColor,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = "${kotlin.math.abs(productInfo.trend)}%",
                        color = trendColor,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySalesChart(
    salesByCategory: Map<ProductCategory, Double>,
    currencyFormat: NumberFormat
) {
    if (salesByCategory.isEmpty()) {
        Text(
            text = "Нет данных о продажах по категориям",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
        return
    }
    
    val totalSales = salesByCategory.values.sum()
    val sortedCategories = salesByCategory.toList().sortedByDescending { it.second }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
            sortedCategories.forEach { (category, sales) ->
                val percentage = if (totalSales > 0) (sales / totalSales * 100).toInt() else 0
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category.displayName,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = "$percentage%",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    LinearProgressIndicator(
                        progress = (sales / totalSales).toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = getCategoryColor(category),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = currencyFormat.format(sales),
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

fun getCategoryColor(category: ProductCategory): Color {
    return when (category) {
        ProductCategory.SHIRTS -> Color(0xFF2196F3)
        ProductCategory.PANTS -> Color(0xFF4CAF50)
        ProductCategory.DRESSES -> Color(0xFFE91E63)
        ProductCategory.OUTERWEAR -> Color(0xFFFF9800)
        ProductCategory.SHOES -> Color(0xFF9C27B0)
        ProductCategory.ACCESSORIES -> Color(0xFF00BCD4)
        ProductCategory.UNDERWEAR -> Color(0xFFFF4081)
        ProductCategory.SPORTSWEAR -> Color(0xFF8BC34A)
        ProductCategory.OTHER -> Color(0xFF607D8B)
    }
}
