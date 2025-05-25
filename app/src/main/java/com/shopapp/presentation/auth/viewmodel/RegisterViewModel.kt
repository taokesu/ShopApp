package com.shopapp.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.User
import com.shopapp.data.model.UserRole
import com.shopapp.domain.usecase.auth.RegisterUseCase
import com.shopapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _registrationState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val registrationState: StateFlow<UiState<Long>> = _registrationState

    fun register(
        username: String,
        password: String,
        email: String,
        fullName: String? = null,
        phone: String? = null,
        address: String? = null
    ) {
        _registrationState.value = UiState.Loading
        
        val user = User(
            username = username,
            password = password,
            email = email,
            fullName = fullName,
            phone = phone,
            address = address,
            role = UserRole.CUSTOMER // По умолчанию регистрируем покупателя
        )
        
        viewModelScope.launch {
            registerUseCase(user)
                .onSuccess { userId ->
                    _registrationState.value = UiState.Success(userId)
                }
                .onFailure { error ->
                    _registrationState.value = UiState.Error(error.message ?: "Ошибка регистрации")
                }
        }
    }

    fun resetState() {
        _registrationState.value = UiState.Idle
    }
}
