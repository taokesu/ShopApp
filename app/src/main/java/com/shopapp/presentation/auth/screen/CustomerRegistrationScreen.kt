package com.shopapp.presentation.auth.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import com.shopapp.presentation.common.components.RequiredValidatedTextField
import com.shopapp.presentation.common.components.OptionalValidatedTextField
import com.shopapp.presentation.common.validation.ValidationUtils
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shopapp.presentation.auth.viewmodel.RegisterViewModel
import com.shopapp.presentation.common.UiState
import com.shopapp.presentation.common.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerRegistrationScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val registrationState by viewModel.registrationState.collectAsState()
    
    LaunchedEffect(registrationState) {
        when (registrationState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Регистрация успешна! Теперь вы можете войти в систему.")
                navController.navigate(Screen.CustomerLogin.route) {
                    popUpTo(Screen.CustomerRegistration.route) { inclusive = true }
                }
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((registrationState as UiState.Error).message)
            }
            is UiState.Loading -> {}
            is UiState.Idle -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Регистрация") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Создание аккаунта",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                RequiredValidatedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Имя пользователя",
                    validate = { ValidationUtils.validateUsername(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                RequiredValidatedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль",
                    validate = { ValidationUtils.validatePassword(it) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                RequiredValidatedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Подтверждение пароля",
                    validate = { ValidationUtils.validatePasswordConfirmation(password, it) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                RequiredValidatedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    validate = { ValidationUtils.validateEmail(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OptionalValidatedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "ФИО",
                    validate = { ValidationUtils.validateFullName(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OptionalValidatedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Телефон",
                    validate = { ValidationUtils.validatePhone(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OptionalValidatedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Адрес",
                    validate = { ValidationUtils.validateAddress(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Проверяем все поля перед отправкой
                val usernameValidation = ValidationUtils.validateUsername(username)
                val passwordValidation = ValidationUtils.validatePassword(password)
                val passwordConfirmationValidation = ValidationUtils.validatePasswordConfirmation(password, confirmPassword)
                val emailValidation = ValidationUtils.validateEmail(email)
                val phoneValidation = ValidationUtils.validatePhone(phone)
                val fullNameValidation = ValidationUtils.validateFullName(fullName)
                val addressValidation = ValidationUtils.validateAddress(address)
                
                val formIsValid = usernameValidation.isValid && 
                                  passwordValidation.isValid && 
                                  passwordConfirmationValidation.isValid && 
                                  emailValidation.isValid && 
                                  phoneValidation.isValid && 
                                  fullNameValidation.isValid && 
                                  addressValidation.isValid
                
                Button(
                    onClick = {
                        if (formIsValid) {
                            viewModel.register(username, password, email, fullName, phone, address)
                        }
                    },
                    enabled = formIsValid && registrationState !is UiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Показываем текст кнопки или индикатор загрузки внутри кнопки
                    if (registrationState is UiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Зарегистрироваться", modifier = Modifier.padding(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "* - обязательные поля",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
    }
}
