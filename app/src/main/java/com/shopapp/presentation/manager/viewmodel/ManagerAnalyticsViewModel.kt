package com.shopapp.presentation.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.OrderStatus
import com.shopapp.data.model.ProductCategory
import com.shopapp.data.repository.OrderRepository
import com.shopapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ManagerAnalyticsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadSalesData()
        loadTopSellingProducts()
        loadCategorySalesData()
    }

    fun loadSalesData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Получаем данные о продажах за последние периоды
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val lastWeekStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.DAY_OF_MONTH, -7)
                }.time

                val lastMonthStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.MONTH, -1)
                }.time

                // Получаем заказы
                orderRepository.getAllOrders().collect { orders ->
                    val completedOrders = orders.filter { it.status == OrderStatus.DELIVERED }
                    
                    // Суммарные продажи
                    val totalSales = completedOrders.sumOf { it.totalAmount }
                    
                    // Количество завершенных заказов
                    val totalCompletedOrders = completedOrders.size
                    
                    // Продажи по периодам
                    val todaySales = completedOrders
                        .filter { it.orderDate >= today }
                        .sumOf { it.totalAmount }
                    
                    val weekSales = completedOrders
                        .filter { it.orderDate >= lastWeekStart }
                        .sumOf { it.totalAmount }
                    
                    val monthSales = completedOrders
                        .filter { it.orderDate >= lastMonthStart }
                        .sumOf { it.totalAmount }
                    
                    // Средний чек
                    val averageOrderValue = if (completedOrders.isNotEmpty()) {
                        totalSales / completedOrders.size
                    } else {
                        0.0
                    }
                    
                    // Данные для графика по дням недели
                    val salesByDayOfWeek = getSalesByDayOfWeek(completedOrders)
                    
                    _uiState.update {
                        it.copy(
                            totalSales = totalSales,
                            totalCompletedOrders = totalCompletedOrders,
                            todaySales = todaySales,
                            weekSales = weekSales,
                            monthSales = monthSales,
                            averageOrderValue = averageOrderValue,
                            salesByDayOfWeek = salesByDayOfWeek,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка загрузки данных аналитики: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadTopSellingProducts(limit: Int = 5) {
        viewModelScope.launch {
            try {
                orderRepository.getTopSellingProducts(limit).collect { topProducts ->
                    _uiState.update {
                        it.copy(
                            topSellingProducts = topProducts
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка загрузки популярных товаров: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun loadCategorySalesData() {
        viewModelScope.launch {
            try {
                orderRepository.getSalesByCategory().collect { categorySales ->
                    _uiState.update {
                        it.copy(
                            salesByCategory = categorySales
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка загрузки данных по категориям: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun getSalesByDayOfWeek(orders: List<com.shopapp.data.model.Order>): Map<String, Double> {
        val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        val salesByDay = mutableMapOf<String, Double>()
        
        // Инициализируем все дни нулевыми продажами
        dayNames.forEach { day -> salesByDay[day] = 0.0 }
        
        // Формат для определения дня недели
        val dateFormat = SimpleDateFormat("EEEE", Locale("ru"))
        
        // Заполняем данные по продажам
        orders.forEach { order ->
            val dayOfWeek = dateFormat.format(order.orderDate)
            val day = when (dayOfWeek) {
                "понедельник" -> "Пн"
                "вторник" -> "Вт"
                "среда" -> "Ср"
                "четверг" -> "Чт"
                "пятница" -> "Пт"
                "суббота" -> "Сб"
                "воскресенье" -> "Вс"
                else -> dayOfWeek
            }
            
            salesByDay[day] = (salesByDay[day] ?: 0.0) + order.totalAmount
        }
        
        return salesByDay
    }

    fun clearMessage() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }
}

data class AnalyticsUiState(
    val totalSales: Double = 0.0,
    val totalCompletedOrders: Int = 0,
    val todaySales: Double = 0.0,
    val weekSales: Double = 0.0,
    val monthSales: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val topSellingProducts: List<com.shopapp.data.model.ProductSalesInfo> = emptyList(),
    val salesByCategory: Map<ProductCategory, Double> = emptyMap(),
    val salesByDayOfWeek: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
