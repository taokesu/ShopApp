package com.shopapp.presentation.auth.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shopapp.data.model.UserRole
import com.shopapp.presentation.auth.viewmodel.LoginViewModel
import com.shopapp.presentation.common.UiState
import com.shopapp.presentation.common.navigation.Screen
import com.shopapp.presentation.common.components.RequiredValidatedTextField
import com.shopapp.presentation.common.validation.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val loginState by viewModel.loginState.collectAsState()
    
    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Success -> {
                val user = (loginState as UiState.Success).data
                if (user.role == UserRole.CUSTOMER) {
                    navController.navigate(Screen.CustomerCatalog.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                } else {
                    snackbarHostState.showSnackbar("Неверная роль пользователя. Пожалуйста, войдите как менеджер.")
                    viewModel.resetState()
                }
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((loginState as UiState.Error).message)
            }
            is UiState.Loading -> {}
            is UiState.Idle -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Вход для покупателя") },
                navigationIcon = { },
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Вход в аккаунт",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                RequiredValidatedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Логин",
                    validate = { ValidationUtils.validateUsername(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                RequiredValidatedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль",
                    validate = { ValidationUtils.validatePassword(it) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Проверяем валидность данных перед отправкой
                val usernameValidation = ValidationUtils.validateUsername(username)
                val passwordValidation = ValidationUtils.validatePassword(password)
                val formIsValid = usernameValidation.isValid && passwordValidation.isValid
                
                Button(
                    onClick = { viewModel.login(username, password) },
                    enabled = formIsValid && loginState !is UiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (loginState is UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Войти", modifier = Modifier.padding(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Нет аккаунта? Зарегистрироваться",
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Screen.CustomerRegistration.route) }
                )
                
                // Индикатор загрузки теперь отображается внутри кнопки
            }
        }
    }
}
