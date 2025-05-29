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
import com.shopapp.presentation.common.components.RequiredValidatedTextField
import com.shopapp.presentation.common.components.OptionalValidatedTextField
import com.shopapp.presentation.common.validation.ValidationResult
import com.shopapp.presentation.common.validation.ValidationUtils
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
                    RequiredValidatedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = "Название товара",
                        validate = { ValidationUtils.validateProductName(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Поле описания товара
                    OptionalValidatedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = "Описание товара",
                        validate = { ValidationUtils.validateProductDescription(it) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        singleLine = false,
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
                    OptionalValidatedTextField(
                        value = uiState.imageUrl,
                        onValueChange = { viewModel.updateImageUrl(it) },
                        label = "URL изображения",
                        validate = { ValidationResult(isValid = true) }, // Простая валидация URL
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Поле цены
                    RequiredValidatedTextField(
                        value = uiState.price,
                        onValueChange = { viewModel.updatePrice(it) },
                        label = "Цена (₽)",
                        validate = { ValidationUtils.validatePrice(it) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    // Поле количества
                    RequiredValidatedTextField(
                        value = uiState.quantity,
                        onValueChange = { viewModel.updateQuantity(it) },
                        label = "Количество на складе",
                        validate = { ValidationUtils.validateQuantity(it) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Поле размера
                    OptionalValidatedTextField(
                        value = uiState.size,
                        onValueChange = { viewModel.updateSize(it) },
                        label = "Размер",
                        validate = { ValidationResult(isValid = true) }, // Простая валидация
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Поле цвета
                    OptionalValidatedTextField(
                        value = uiState.color,
                        onValueChange = { viewModel.updateColor(it) },
                        label = "Цвет",
                        validate = { ValidationResult(isValid = true) }, // Простая валидация
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
