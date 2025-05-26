package com.shopapp.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopapp.data.model.User
import com.shopapp.data.session.UserSessionManager
import com.shopapp.domain.usecase.auth.LoginUseCase
import com.shopapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val loginState: StateFlow<UiState<User>> = _loginState

    fun login(username: String, password: String) {
        _loginState.value = UiState.Loading
        
        viewModelScope.launch {
            loginUseCase(username, password)
                .onSuccess { user ->
                    // Сохраняем информацию о пользователе в сессию
                    userSessionManager.saveSession(user)
                    _loginState.value = UiState.Success(user)
                }
                .onFailure { error ->
                    _loginState.value = UiState.Error(error.message ?: "Неизвестная ошибка")
                }
        }
    }

    fun resetState() {
        _loginState.value = UiState.Idle
    }
}
