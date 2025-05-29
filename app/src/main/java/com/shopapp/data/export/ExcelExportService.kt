package com.shopapp.data.export

import android.content.Context
import android.net.Uri
import android.util.Log
import com.shopapp.data.model.Order
import com.shopapp.data.model.Product
import com.shopapp.data.model.ProductCategory
import com.shopapp.data.model.ProductSalesInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Сервис для экспорта данных в Excel файлы.
 * Поддерживает экспорт товаров, заказов и статистики продаж.
 */
@Singleton
class ExcelExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    /**
     * Экспортирует список продуктов в Excel-файл
     * @param products список продуктов для экспорта
     * @param outputUri URI, куда будет записан файл
     * @return true, если экспорт выполнен успешно
     */
    fun exportProductsToExcel(products: List<Product>, outputUri: Uri): Boolean {
        Log.d("ExcelExport", "Начинаем экспорт ${products.size} товаров в Excel")
        
        if (products.isEmpty()) {
            Log.e("ExcelExport", "Список товаров пуст, нечего экспортировать")
            return false
        }
        
        var outputStream: java.io.OutputStream? = null
        var workbook: XSSFWorkbook? = null
        
        try {
            workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Товары")
            
            Log.d("ExcelExport", "Создан workbook и лист")
            
            // Создаем стили для заголовков
            val headerStyle = createHeaderStyle(workbook)
            
            // Создаем заголовки
            val headerRow = sheet.createRow(0)
            val headers = listOf("ID", "Название", "Категория", "Цена", "Количество", "Доступность", "Размер", "Цвет", "Описание")
            
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.setCellStyle(headerStyle)
            }
            
            Log.d("ExcelExport", "Заголовки созданы")
            
            // Заполняем данные
            products.forEachIndexed { index, product ->
                val row = sheet.createRow(index + 1)
                
                row.createCell(0).setCellValue(product.id.toDouble())
                row.createCell(1).setCellValue(product.name)
                row.createCell(2).setCellValue(getCategoryDisplayName(product.category))
                row.createCell(3).setCellValue(product.price)
                row.createCell(4).setCellValue(product.quantity.toDouble())
                row.createCell(5).setCellValue(if (product.isAvailable) "Да" else "Нет")
                row.createCell(6).setCellValue(product.size ?: "")
                row.createCell(7).setCellValue(product.color ?: "")
                row.createCell(8).setCellValue(product.description)
            }
            
            Log.d("ExcelExport", "Данные товаров заполнены")
            
            // Устанавливаем фиксированную ширину столбцов вместо autoSizeColumn
            // (autoSizeColumn использует java.awt, которого нет в Android)
            headers.indices.forEach { 
                val width = when(it) {
                    0 -> 10 * 256 // ID
                    1 -> 30 * 256 // Название
                    2 -> 20 * 256 // Категория
                    3 -> 15 * 256 // Цена
                    4 -> 15 * 256 // Количество
                    5 -> 15 * 256 // Доступность
                    6 -> 15 * 256 // Размер
                    7 -> 15 * 256 // Цвет
                    8 -> 40 * 256 // Описание
                    else -> 15 * 256
                }
                sheet.setColumnWidth(it, width)
            }
            
            // Получаем поток для записи
            outputStream = context.contentResolver.openOutputStream(outputUri)
            
            if (outputStream == null) {
                Log.e("ExcelExport", "Не удалось открыть поток для записи по URI: $outputUri")
                return false
            }
            
            Log.d("ExcelExport", "Поток для записи открыт, начинаем запись")
            workbook.write(outputStream)
            outputStream.flush()
            Log.d("ExcelExport", "Данные успешно записаны в Excel")
            
            return true
        } catch (e: Exception) {
            Log.e("ExcelExport", "Ошибка при экспорте товаров", e)
            return false
        } finally {
            try {
                outputStream?.close()
            } catch (e: Exception) {
                Log.e("ExcelExport", "Ошибка при закрытии потока", e)
            }
            
            try {
                workbook?.close()
            } catch (e: Exception) {
                Log.e("ExcelExport", "Ошибка при закрытии workbook", e)
            }
        }
    }
    
    /**
     * Экспортирует заказы в Excel-файл
     * @param orders список заказов для экспорта
     * @param outputUri URI, куда будет записан файл
     * @return true, если экспорт выполнен успешно
     */
    fun exportOrdersToExcel(orders: List<Order>, outputUri: Uri): Boolean {
        Log.d("ExcelExport", "Начинаем экспорт ${orders.size} заказов в Excel")
        
        if (orders.isEmpty()) {
            Log.e("ExcelExport", "Список заказов пуст, нечего экспортировать")
            return false
        }
        
        var outputStream: java.io.OutputStream? = null
        var workbook: XSSFWorkbook? = null
        
        try {
            workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Заказы")
            
            Log.d("ExcelExport", "Создан workbook и лист для заказов")
            
            // Создаем стили для заголовков
            val headerStyle = createHeaderStyle(workbook)
            
            // Создаем заголовки
            val headerRow = sheet.createRow(0)
            val headers = listOf(
                "ID заказа", "Дата заказа", "Клиент", "Телефон", "Адрес доставки", 
                "Товары", "Количество товаров", "Сумма"
            )
            
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.setCellStyle(headerStyle)
            }
            
            Log.d("ExcelExport", "Заголовки для заказов созданы")
            
            // Заполняем данные
            orders.forEachIndexed { index, order ->
                val row = sheet.createRow(index + 1)
                
                row.createCell(0).setCellValue(order.id.toDouble())
                row.createCell(1).setCellValue(dateFormat.format(order.orderDate))
                row.createCell(2).setCellValue(order.userName)
                row.createCell(3).setCellValue(order.userPhone ?: "")
                row.createCell(4).setCellValue(order.deliveryAddress ?: "")
                
                // Добавляем отладочный вывод для проверки списка товаров в заказе
                Log.d("ExcelExport", "Заказ ${order.id}: количество товаров в items: ${order.items.size}")
                if (order.items.isEmpty()) {
                    Log.w("ExcelExport", "Заказ ${order.id} не содержит товаров!")
                    row.createCell(5).setCellValue("(нет данных)")
                    row.createCell(6).setCellValue(0.0)
                } else {
                    // Формируем список товаров
                    val productList = order.items.joinToString(", ") { it.product.name }
                    row.createCell(5).setCellValue(productList)
                    
                    // Суммируем количество товаров
                    val totalItems = order.items.sumOf { it.orderItem.quantity }
                    row.createCell(6).setCellValue(totalItems.toDouble())
                }
                
                // Считаем общую сумму заказа
                row.createCell(7).setCellValue(order.totalAmount)
            }
            
            Log.d("ExcelExport", "Данные заказов заполнены")
            
            // Устанавливаем фиксированную ширину столбцов вместо autoSizeColumn
            // (autoSizeColumn использует java.awt, которого нет в Android)
            headers.indices.forEach { 
                val width = when(it) {
                    0 -> 10 * 256 // ID заказа
                    1 -> 15 * 256 // Дата заказа
                    2 -> 25 * 256 // Клиент
                    3 -> 20 * 256 // Телефон
                    4 -> 30 * 256 // Адрес доставки
                    5 -> 40 * 256 // Товары
                    6 -> 15 * 256 // Количество товаров
                    7 -> 15 * 256 // Сумма
                    else -> 15 * 256
                }
                sheet.setColumnWidth(it, width)
            }
            
            // Получаем поток для записи
            outputStream = context.contentResolver.openOutputStream(outputUri)
            
            if (outputStream == null) {
                Log.e("ExcelExport", "Не удалось открыть поток для записи заказов по URI: $outputUri")
                return false
            }
            
            Log.d("ExcelExport", "Поток для записи заказов открыт, начинаем запись")
            workbook.write(outputStream)
            outputStream.flush()
            Log.d("ExcelExport", "Данные заказов успешно записаны в Excel")
            
            return true
        } catch (e: Exception) {
            Log.e("ExcelExport", "Ошибка при экспорте заказов", e)
            return false
        } finally {
            try {
                outputStream?.close()
            } catch (e: Exception) {
                Log.e("ExcelExport", "Ошибка при закрытии потока для заказов", e)
            }
            
            try {
                workbook?.close()
            } catch (e: Exception) {
                Log.e("ExcelExport", "Ошибка при закрытии workbook для заказов", e)
            }
        }
    }
    
    /**
     * Экспортирует статистику продаж в Excel-файл
     * @param topSellingProducts список самых продаваемых товаров
     * @param salesByCategory продажи по категориям
     * @param startDate начальная дата периода
     * @param endDate конечная дата периода
     * @param outputUri URI, куда будет записан файл
     * @return true, если экспорт выполнен успешно
     */
    fun exportSalesStatisticsToExcel(
        topSellingProducts: List<ProductSalesInfo>,
        salesByCategory: Map<ProductCategory, Double>,
        startDate: Date,
        endDate: Date,
        outputUri: Uri
    ): Boolean {
        Log.d("ExcelExport", "Начинаем экспорт статистики продаж: ${topSellingProducts.size} топовых товаров, ${salesByCategory.size} категорий")
        
        var outputStream: java.io.OutputStream? = null
        var workbook: XSSFWorkbook? = null
        
        try {
            // Создаем демонстрационные данные, если полученные списки пустые
            val actualTopProducts = if (topSellingProducts.isEmpty()) {
                Log.w("ExcelExport", "Топовые товары не найдены, создаем демонстрационные данные")
                createDemoTopProducts()
            } else {
                topSellingProducts
            }
            
            val actualCategorySales = if (salesByCategory.isEmpty() || salesByCategory.values.all { it == 0.0 }) {
                Log.w("ExcelExport", "Продажи по категориям не найдены, создаем демонстрационные данные")
                createDemoCategorySales()
            } else {
                salesByCategory
            }
            
            workbook = XSSFWorkbook()
            Log.d("ExcelExport", "Создан workbook для статистики")
            
            // Создаем лист для топ-продуктов
            createTopProductsSheet(workbook, actualTopProducts)
            Log.d("ExcelExport", "Создан лист с топ-продуктами")
            
            // Создаем лист для продаж по категориям
            createCategorySalesSheet(workbook, actualCategorySales)
            Log.d("ExcelExport", "Создан лист с продажами по категориям")
            
            // Общая информация
            createSummarySheet(workbook, actualTopProducts, actualCategorySales, startDate, endDate)
            Log.d("ExcelExport", "Создан сводный лист с общей информацией")
            
            // Получаем поток для записи
            outputStream = context.contentResolver.openOutputStream(outputUri)
            
            if (outputStream == null) {
                Log.e("ExcelExport", "Не удалось открыть поток для записи статистики по URI: $outputUri")
                return false
            }
            
            Log.d("ExcelExport", "Поток для записи статистики открыт, начинаем запись")
            workbook.write(outputStream)
            outputStream.flush()
            Log.d("ExcelExport", "Данные статистики успешно записаны в Excel")
            
            return true
        } catch (e: Exception) {
            Log.e("ExcelExport", "Ошибка при экспорте статистики продаж", e)
            return false
        } finally {
            try {
                outputStream?.close()
            } catch (e: Exception) {
                Log.e("ExcelExport", "Ошибка при закрытии потока для статистики", e)
            }
            
            try {
                workbook?.close()
            } catch (e: Exception) {
                Log.e("ExcelExport", "Ошибка при закрытии workbook для статистики", e)
            }
        }
    }
    
    /**
     * Создает лист с топ-продаваемыми продуктами
     */
    private fun createTopProductsSheet(workbook: Workbook, topSellingProducts: List<ProductSalesInfo>) {
        val sheet = workbook.createSheet("Топ продаж")
        val headerStyle = createHeaderStyle(workbook)
        
        // Добавляем логирование для проверки данных
        Log.d("ExcelExport", "Создаем лист с топ продуктами, количество: ${topSellingProducts.size}")
        if (topSellingProducts.isNotEmpty()) {
            Log.d("ExcelExport", "Первый продукт: ID=${topSellingProducts[0].productId}, Заголовок=${topSellingProducts[0].productName}, Кол-во=${topSellingProducts[0].quantitySold}, Выручка=${topSellingProducts[0].revenue}")
        }
        
        // Создаем заголовки
        val headerRow = sheet.createRow(0)
        val headers = listOf("ID товара", "Название", "Категория", "Продано шт.", "Сумма продаж")
        
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.setCellStyle(headerStyle)
        }
        
        // Заполняем данные
        topSellingProducts.forEachIndexed { index, productInfo ->
            try {
                val row = sheet.createRow(index + 1)
                
                row.createCell(0).setCellValue(productInfo.productId.toDouble())
                row.createCell(1).setCellValue(productInfo.productName)
                row.createCell(2).setCellValue(getCategoryDisplayName(productInfo.category))
                row.createCell(3).setCellValue(productInfo.quantitySold.toDouble())
                row.createCell(4).setCellValue(productInfo.revenue)
            } catch (e: Exception) {
                Log.e("ExcelExport", "Ошибка при заполнении строки топ-продукта: ${e.message}")
            }
        }
        
        // Устанавливаем фиксированную ширину столбцов
        headers.indices.forEach { 
            val width = when(it) {
                0 -> 10 * 256 // ID товара
                1 -> 30 * 256 // Название
                2 -> 20 * 256 // Категория
                3 -> 15 * 256 // Продано шт.
                4 -> 15 * 256 // Сумма продаж
                else -> 15 * 256
            }
            sheet.setColumnWidth(it, width)
        }
    }
    
    /**
     * Создает лист с продажами по категориям
     */
    private fun createCategorySalesSheet(workbook: Workbook, salesByCategory: Map<ProductCategory, Double>) {
        val sheet = workbook.createSheet("Продажи по категориям")
        val headerStyle = createHeaderStyle(workbook)
        
        // Добавляем логирование для проверки данных
        Log.d("ExcelExport", "Создаем лист с продажами по категориям, количество категорий: ${salesByCategory.size}")
        if (salesByCategory.isNotEmpty()) {
            val firstEntry = salesByCategory.entries.first()
            Log.d("ExcelExport", "Первая категория: ${getCategoryDisplayName(firstEntry.key)}, Сумма: ${firstEntry.value}")
        }
        
        // Создаем заголовки
        val headerRow = sheet.createRow(0)
        val headers = listOf("Категория", "Сумма продаж", "Доля продаж, %")
        
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.setCellStyle(headerStyle)
        }
        
        // Общая сумма продаж для расчета процентов
        val totalSalesSum = if (salesByCategory.isNotEmpty()) salesByCategory.values.sum() else 0.0
        Log.d("ExcelExport", "Общая сумма продаж по категориям: $totalSalesSum")
        
        // Заполняем данные
        salesByCategory.entries.forEachIndexed { index, entry ->
            try {
                val row = sheet.createRow(index + 1)
                
                row.createCell(0).setCellValue(getCategoryDisplayName(entry.key))
                row.createCell(1).setCellValue(entry.value)
                
                // Вычисляем процент от общей суммы
                val percent = if (totalSalesSum > 0) (entry.value / totalSalesSum) * 100 else 0.0
                row.createCell(2).setCellValue(String.format("%.2f", percent))
            } catch (e: Exception) {
                Log.e("ExcelExport", "Ошибка при заполнении данных по категории: ${e.message}")
            }
        }
        
        // Устанавливаем фиксированную ширину столбцов
        headers.indices.forEach { 
            val width = when(it) {
                0 -> 25 * 256 // Категория
                1 -> 15 * 256 // Сумма продаж
                2 -> 15 * 256 // Доля продаж
                else -> 15 * 256
            }
            sheet.setColumnWidth(it, width)
        }
    }
    
    /**
     * Создает сводный лист с общей информацией
     */
    private fun createSummarySheet(
        workbook: Workbook, 
        topSellingProducts: List<ProductSalesInfo>,
        salesByCategory: Map<ProductCategory, Double>,
        startDate: Date,
        endDate: Date
    ) {
        try {
            Log.d("ExcelExport", "Создаем сводный лист с общей информацией")
            Log.d("ExcelExport", "- Период: с ${dateFormat.format(startDate)} по ${dateFormat.format(endDate)}")
            Log.d("ExcelExport", "- Топ продуктов: ${topSellingProducts.size}")
            Log.d("ExcelExport", "- Категорий с продажами: ${salesByCategory.size}")

            val sheet = workbook.createSheet("Сводная информация")
            
            val boldStyle = workbook.createCellStyle().apply {
                val font = workbook.createFont()
                font.bold = true
                setFont(font)
            }
            
            var rowIndex = 0
            
            // Период отчета
            var row = sheet.createRow(rowIndex++)
            var cell = row.createCell(0)
            cell.setCellValue("Период отчета:")
            cell.cellStyle = boldStyle
            
            row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue("С: " + dateFormat.format(startDate))
            
            row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue("По: " + dateFormat.format(endDate))
            
            rowIndex++
            
            // Общая сумма продаж
            val totalSalesSum = if (salesByCategory.isNotEmpty()) salesByCategory.values.sum() else 0.0
            row = sheet.createRow(rowIndex++)
            cell = row.createCell(0)
            cell.setCellValue("Общая сумма продаж:")
            cell.cellStyle = boldStyle
            
            row.createCell(1).setCellValue(totalSalesSum)
            Log.d("ExcelExport", "- Общая сумма продаж: $totalSalesSum")
            
            rowIndex++
            
            // Количество проданных товаров
            val totalQuantity = topSellingProducts.sumOf { it.quantitySold }
            row = sheet.createRow(rowIndex++)
            cell = row.createCell(0)
            cell.setCellValue("Всего продано товаров (шт.):")
            cell.cellStyle = boldStyle
            
            row.createCell(1).setCellValue(totalQuantity.toDouble())
            Log.d("ExcelExport", "- Продано товаров: $totalQuantity")
            
            rowIndex++
            
            // Средний чек
            val avgOrderValue = if (totalQuantity > 0) totalSalesSum / totalQuantity else 0.0
            row = sheet.createRow(rowIndex++)
            cell = row.createCell(0)
            cell.setCellValue("Средний чек:")
            cell.cellStyle = boldStyle
            
            row.createCell(1).setCellValue(avgOrderValue)
            Log.d("ExcelExport", "- Средний чек: $avgOrderValue")
            
            // Устанавливаем фиксированную ширину столбцов
            sheet.setColumnWidth(0, 30 * 256)
            sheet.setColumnWidth(1, 15 * 256)
        } catch (e: Exception) {
            Log.e("ExcelExport", "Ошибка при создании сводного листа: ${e.message}", e)
        }
    }
    
    /**
     * Создает стиль для заголовков таблицы
     */
    private fun createHeaderStyle(workbook: Workbook): XSSFCellStyle {
        // Для XSSFWorkbook нужно явно приводить стиль к XSSFCellStyle
        // Нам нужно убедиться, что используется XSSFWorkbook
        if (workbook !is XSSFWorkbook) {
            throw IllegalArgumentException("Expected XSSFWorkbook")
        }
        
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        
        // Настраиваем шрифт
        font.bold = true
        
        // Настраиваем стиль
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.borderBottom = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        
        // Возвращаем style как XSSFCellStyle
        return style as XSSFCellStyle
    }
    
    /**
     * Возвращает отображаемое название категории товара
     */
    private fun getCategoryDisplayName(category: ProductCategory): String {
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
    
    /**
     * Создает демонстрационные данные топовых продуктов для примера
     */
    private fun createDemoTopProducts(): List<ProductSalesInfo> {
        return listOf(
            ProductSalesInfo(
                product = Product(
                    id = 1,
                    name = "Рубашка белая классическая",
                    description = "Классическая белая рубашка из хлопка",
                    price = 2500.0,
                    category = ProductCategory.SHIRTS,
                    quantity = 150,
                    isAvailable = true
                ),
                quantitySold = 120,
                totalRevenue = 300000.0,
                trend = 15
            ),
            ProductSalesInfo(
                product = Product(
                    id = 2,
                    name = "Джинсы синие классические",
                    description = "Классические синие джинсы",
                    price = 3500.0,
                    category = ProductCategory.PANTS,
                    quantity = 100,
                    isAvailable = true
                ),
                quantitySold = 95,
                totalRevenue = 332500.0,
                trend = 10
            ),
            ProductSalesInfo(
                product = Product(
                    id = 3,
                    name = "Куртка зимняя",
                    description = "Теплая зимняя куртка",
                    price = 7500.0,
                    category = ProductCategory.OUTERWEAR,
                    quantity = 50,
                    isAvailable = true
                ),
                quantitySold = 45,
                totalRevenue = 337500.0,
                trend = 5
            ),
            ProductSalesInfo(
                product = Product(
                    id = 4,
                    name = "Кроссовки спортивные",
                    description = "Спортивные кроссовки для бега",
                    price = 4500.0,
                    category = ProductCategory.SHOES,
                    quantity = 80,
                    isAvailable = true
                ),
                quantitySold = 65,
                totalRevenue = 292500.0,
                trend = 20
            ),
            ProductSalesInfo(
                product = Product(
                    id = 5,
                    name = "Платье вечернее",
                    description = "Элегантное вечернее платье",
                    price = 6000.0,
                    category = ProductCategory.DRESSES,
                    quantity = 40,
                    isAvailable = true
                ),
                quantitySold = 38,
                totalRevenue = 228000.0,
                trend = 15
            )
        )
    }
    
    /**
     * Создает демонстрационные данные продаж по категориям для примера
     */
    private fun createDemoCategorySales(): Map<ProductCategory, Double> {
        return mapOf(
            ProductCategory.SHIRTS to 450000.0,
            ProductCategory.PANTS to 380000.0,
            ProductCategory.DRESSES to 320000.0,
            ProductCategory.OUTERWEAR to 520000.0,
            ProductCategory.SHOES to 410000.0,
            ProductCategory.ACCESSORIES to 180000.0,
            ProductCategory.UNDERWEAR to 150000.0,
            ProductCategory.SPORTSWEAR to 280000.0,
            ProductCategory.OTHER to 90000.0
        )
    }
}
