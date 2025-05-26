package com.shopapp.presentation.manager.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shopapp.presentation.common.navigation.LogoutCallback
import com.shopapp.presentation.manager.viewmodel.ManagerProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerProfileScreen(
    navController: NavHostController,
    viewModel: ManagerProfileViewModel = hiltViewModel(),
    logoutCallback: LogoutCallback? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var isEditing by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    // Загружаем данные при первом открытии экрана
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }
    
    // Обновляем локальные состояния при обновлении данных из ViewModel
    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            fullName = user.fullName ?: ""
            phone = user.phone ?: ""
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль менеджера") },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            viewModel.updateUserProfile(fullName, phone)
                            isEditing = false
                        }) {
                            Icon(Icons.Default.Save, contentDescription = "Сохранить")
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.CenterHorizontally),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Имя пользователя",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = uiState.user?.username ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = uiState.user?.email ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Личные данные",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { if (isEditing) fullName = it },
                        label = { Text("ФИО") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditing,
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (isEditing) phone = it },
                        label = { Text("Телефон") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditing,
                        singleLine = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { logoutCallback?.onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выйти из аккаунта")
            }
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Показываем сообщение об ошибке или успехе
            LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
                uiState.errorMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearMessages()
                }
                
                uiState.successMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearMessages()
                }
            }
        }
    }
}
