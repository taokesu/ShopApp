package com.shopapp.presentation.manager.screen

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shopapp.presentation.common.components.LoadingIndicator
import com.shopapp.presentation.manager.viewmodel.StatisticsExportViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Экран для экспорта статистики в Excel
 */
@Composable
fun StatisticsExportScreen(
    navController: NavController,
    viewModel: StatisticsExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    
    // Для создания файла Excel при выборе типа экспорта
    var selectedExportType by remember { mutableStateOf<ExportType?>(null) }
    
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let { selectedUri ->
            selectedExportType?.let { exportType ->
                when (exportType) {
                    ExportType.PRODUCTS -> viewModel.exportProductCatalog(selectedUri)
                    ExportType.ORDERS -> viewModel.exportOrders(uiState.startDate, uiState.endDate, selectedUri)
                    ExportType.STATISTICS -> viewModel.exportSalesStatistics(uiState.startDate, uiState.endDate, selectedUri)
                }
            }
        }
    }
    
    // Эффект для показа сообщений об успехе или ошибке
    LaunchedEffect(uiState.isExportSuccess, uiState.errorMessage) {
        uiState.isExportSuccess?.let { success ->
            if (success) {
                // Тут можно добавить показ Snackbar или Toast
            }
        }
        
        uiState.errorMessage?.let { error ->
            // Тут можно добавить показ Snackbar или Toast с ошибкой
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Экспорт данных",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Секция выбора периода
        DateRangeSelector(
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            onDateRangeChanged = { start, end ->
                viewModel.setDateRange(start, end)
            },
            dateFormat = dateFormat,
            context = context
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Секция выбора типа экспорта
        ExportTypeSelector(
            onExportTypeSelected = { exportType ->
                selectedExportType = exportType
                val fileName = when (exportType) {
                    ExportType.PRODUCTS -> "product_catalog_${System.currentTimeMillis()}.xlsx"
                    ExportType.ORDERS -> "orders_${dateFormat.format(uiState.startDate)}_${dateFormat.format(uiState.endDate)}.xlsx"
                    ExportType.STATISTICS -> "sales_statistics_${dateFormat.format(uiState.startDate)}_${dateFormat.format(uiState.endDate)}.xlsx"
                }
                createDocumentLauncher.launch(fileName)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Показываем сообщение об успехе или ошибке
        uiState.isExportSuccess?.let { success ->
            val bgColor = if (success) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                          else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            val textColor = if (success) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
            val message = if (success) "Экспорт выполнен успешно!"
                          else "Ошибка при экспорте: ${uiState.errorMessage ?: "неизвестная ошибка"}"
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(bgColor, RoundedCornerShape(8.dp))
                    .border(1.dp, textColor, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = message,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = { viewModel.clearMessages() }) {
                Text("Закрыть")
            }
        }
        
        // Индикатор загрузки
        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            LoadingIndicator()
        }
    }
}

/**
 * Компонент для выбора периода дат
 */
@Composable
fun DateRangeSelector(
    startDate: Date,
    endDate: Date,
    onDateRangeChanged: (Date, Date) -> Unit,
    dateFormat: SimpleDateFormat,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5) // Светлый серый цвет
        ),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Выберите период",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Выбор начальной даты
                OutlinedButton(
                    onClick = {
                        showDatePicker(context, startDate) { newDate ->
                            if (newDate.before(endDate) || newDate == endDate) {
                                onDateRangeChanged(newDate, endDate)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Начальная дата"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "С: ${dateFormat.format(startDate)}")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Выбор конечной даты
                OutlinedButton(
                    onClick = {
                        showDatePicker(context, endDate) { newDate ->
                            if (newDate.after(startDate) || newDate == startDate) {
                                onDateRangeChanged(startDate, newDate)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Конечная дата"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "По: ${dateFormat.format(endDate)}")
                }
            }
            
            // Кнопки быстрого выбора периодов
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickDateButton("Сегодня") {
                    val today = Calendar.getInstance()
                    val startOfDay = Calendar.getInstance().apply {
                        time = today.time
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }.time
                    val endOfDay = Calendar.getInstance().apply {
                        time = today.time
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                    }.time
                    onDateRangeChanged(startOfDay, endOfDay)
                }
                
                QuickDateButton("Неделя") {
                    val end = Calendar.getInstance()
                    val start = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, -7)
                    }
                    onDateRangeChanged(start.time, end.time)
                }
                
                QuickDateButton("Месяц") {
                    val end = Calendar.getInstance()
                    val start = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    onDateRangeChanged(start.time, end.time)
                }
            }
        }
    }
}

/**
 * Кнопка быстрого выбора периода
 */
@Composable
fun QuickDateButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text)
    }
}

/**
 * Компонент для выбора типа экспорта
 */
@Composable
fun ExportTypeSelector(onExportTypeSelected: (ExportType) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5) // Светлый серый цвет
        ),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Выберите тип экспорта",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            ExportTypeButton(
                title = "Каталог товаров",
                description = "Экспорт всех товаров в магазине",
                icon = Icons.Default.Inventory
            ) {
                onExportTypeSelected(ExportType.PRODUCTS)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ExportTypeButton(
                title = "Заказы",
                description = "Экспорт заказов за выбранный период",
                icon = Icons.Default.ReceiptLong
            ) {
                onExportTypeSelected(ExportType.ORDERS)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ExportTypeButton(
                title = "Статистика продаж",
                description = "Экспорт подробной статистики продаж",
                icon = Icons.Default.FileDownload
            ) {
                onExportTypeSelected(ExportType.STATISTICS)
            }
        }
    }
}

/**
 * Кнопка выбора типа экспорта
 */
@Composable
fun ExportTypeButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = icon, contentDescription = title)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }
            
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Экспортировать"
            )
        }
    }
}

/**
 * Показывает диалог выбора даты
 */
private fun showDatePicker(
    context: Context,
    initialDate: Date,
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance().apply { time = initialDate }
    
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            onDateSelected(selectedCalendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

/**
 * Типы экспорта
 */
enum class ExportType {
    PRODUCTS,
    ORDERS,
    STATISTICS
}
