package com.shopapp.data.session

import android.content.Context
import android.content.SharedPreferences
import com.shopapp.data.model.User
import com.shopapp.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Менеджер сессии пользователя, отвечающий за хранение информации о текущем авторизованном пользователе.
 */
@Singleton
class UserSessionManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "user_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "user_role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Для реактивной обработки изменений состояния сессии
    private val _userSession = MutableStateFlow<User?>(null)
    val userSession: Flow<User?> = _userSession

    init {
        // Восстанавливаем сессию при создании менеджера
        loadSession()
    }

    /**
     * Сохраняет информацию о сессии пользователя после успешной авторизации.
     * @param user объект пользователя
     */
    fun saveSession(user: User) {
        sharedPreferences.edit().apply {
            putLong(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_ROLE, user.role.name)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
        _userSession.value = user
    }

    /**
     * Загружает сохраненную сессию пользователя.
     * @return true если сессия успешно загружена, false в противном случае
     */
    fun loadSession(): Boolean {
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        
        if (isLoggedIn) {
            val userId = sharedPreferences.getLong(KEY_USER_ID, -1)
            val username = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
            val roleStr = sharedPreferences.getString(KEY_ROLE, null)
            
            if (userId != -1L && username.isNotEmpty() && roleStr != null) {
                try {
                    val role = UserRole.valueOf(roleStr)
                    val user = User(
                        id = userId,
                        username = username,
                        password = "", // Мы не храним пароль в сессии по соображениям безопасности
                        email = "",
                        role = role
                    )
                    _userSession.value = user
                    return true
                } catch (e: Exception) {
                    clearSession()
                }
            }
        }
        
        _userSession.value = null
        return false
    }

    /**
     * Очищает сессию пользователя при выходе из системы.
     */
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
        _userSession.value = null
    }

    /**
     * Получает ID текущего авторизованного пользователя.
     * @return ID пользователя или null, если пользователь не авторизован
     */
    fun getCurrentUserId(): Long? {
        return _userSession.value?.id
    }

    /**
     * Проверяет, авторизован ли пользователь.
     * @return true если пользователь авторизован, false в противном случае
     */
    fun isLoggedIn(): Boolean {
        return _userSession.value != null
    }

    /**
     * Получает роль текущего авторизованного пользователя.
     * @return роль пользователя или null, если пользователь не авторизован
     */
    fun getCurrentUserRole(): UserRole? {
        return _userSession.value?.role
    }
}
