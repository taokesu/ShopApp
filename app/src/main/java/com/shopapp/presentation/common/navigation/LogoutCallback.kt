package com.shopapp.presentation.common.navigation

/**
 * Интерфейс для обратного вызова при выходе из аккаунта
 * Позволяет осуществлять навигацию из вложенных графов к родительской навигации
 */
interface LogoutCallback {
    fun onLogout()
}
