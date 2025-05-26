package com.shopapp.presentation.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.User
import com.shopapp.data.repository.UserRepository
import com.shopapp.data.session.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class CustomerProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    val userSessionManager: UserSessionManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CustomerProfileUiState())
    val uiState: StateFlow<CustomerProfileUiState> = _uiState.asStateFlow()
    
    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val userId = userSessionManager.getCurrentUserId()
                if (userId != null) {
                    val user = userRepository.getUserById(userId)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            user = user
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Пользователь не авторизован"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка загрузки данных: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun updateUserProfile(fullName: String, phone: String, address: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val currentUser = _uiState.value.user
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        fullName = fullName.takeIf { it.isNotBlank() },
                        phone = phone.takeIf { it.isNotBlank() },
                        address = address.takeIf { it.isNotBlank() }
                    )
                    
                    userRepository.updateUser(updatedUser)
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            user = updatedUser,
                            successMessage = "Профиль успешно обновлен"
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Пользователь не найден"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка обновления профиля: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { 
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }
}
