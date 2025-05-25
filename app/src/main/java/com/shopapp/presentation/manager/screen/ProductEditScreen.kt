package com.shopapp.presentation.manager.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shopapp.data.model.Product
import com.shopapp.data.model.ProductCategory
import com.shopapp.data.model.displayName
import com.shopapp.presentation.common.components.LoadingIndicator
import com.shopapp.presentation.manager.viewmodel.ProductEditViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    productId: Long?,
    navigateBack: () -> Unit,
    viewModel: ProductEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(productId) {
        if (productId != null && productId > 0) {
            viewModel.loadProduct(productId)
        }
    }

    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == null) "Добавить товар" else "Редактировать товар") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val success = viewModel.saveProduct()
                                if (success) {
                                    navigateBack()
                                }
                            }
                        },
                        enabled = !uiState.isLoading && uiState.isValid
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить")
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Поле названия товара
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Название товара") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.nameError != null,
                        supportingText = {
                            if (uiState.nameError != null) {
                                Text(uiState.nameError!!)
                            }
                        }
                    )

                    // Поле описания товара
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Описание товара") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        isError = uiState.descriptionError != null,
                        supportingText = {
                            if (uiState.descriptionError != null) {
                                Text(uiState.descriptionError!!)
                            }
                        }
                    )

                    // Выпадающее меню для категории
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.category?.displayName ?: "Выберите категорию",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Категория") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            ProductCategory.values().forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.displayName) },
                                    onClick = {
                                        viewModel.updateCategory(category)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Поле URL изображения
                    OutlinedTextField(
                        value = uiState.imageUrl,
                        onValueChange = { viewModel.updateImageUrl(it) },
                        label = { Text("URL изображения") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Поле цены
                    OutlinedTextField(
                        value = uiState.price,
                        onValueChange = { viewModel.updatePrice(it) },
                        label = { Text("Цена (₽)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.priceError != null,
                        supportingText = {
                            if (uiState.priceError != null) {
                                Text(uiState.priceError!!)
                            }
                        }
                    )

                    // Поле количества
                    OutlinedTextField(
                        value = uiState.quantity,
                        onValueChange = { viewModel.updateQuantity(it) },
                        label = { Text("Количество на складе") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.quantityError != null,
                        supportingText = {
                            if (uiState.quantityError != null) {
                                Text(uiState.quantityError!!)
                            }
                        }
                    )

                    // Поле размера
                    OutlinedTextField(
                        value = uiState.size,
                        onValueChange = { viewModel.updateSize(it) },
                        label = { Text("Размер") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Поле цвета
                    OutlinedTextField(
                        value = uiState.color,
                        onValueChange = { viewModel.updateColor(it) },
                        label = { Text("Цвет") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val success = viewModel.saveProduct()
                                if (success) {
                                    navigateBack()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading && uiState.isValid
                    ) {
                        Text("Сохранить")
                    }

                    if (productId != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.deleteProduct()
                                    navigateBack()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Удалить товар")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Отображение сообщений об ошибках
            if (uiState.errorMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(uiState.errorMessage!!)
                }
            }
        }
    }
}
