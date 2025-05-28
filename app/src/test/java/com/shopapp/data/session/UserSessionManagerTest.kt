package com.shopapp.data.session

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.shopapp.data.model.User
import com.shopapp.data.model.UserRole
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UserSessionManagerTest {
    
    private lateinit var userSessionManager: UserSessionManager
    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    
    private val testUser = User(
        id = 1L,
        username = "testuser",
        password = "password",
        email = "test@example.com",
        fullName = "Test User",
        phone = "123456789",
        address = "Test Address",
        role = UserRole.CUSTOMER
    )

    @Before
    fun setup() {
        mockContext = mockk()
        mockSharedPreferences = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)

        // Mock all required SharedPreferences methods
        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockSharedPreferences.getBoolean("is_logged_in", any()) } returns false
        every { mockSharedPreferences.getLong("user_id", any()) } returns 0L
        every { mockSharedPreferences.getString("username", any()) } returns ""
        every { mockSharedPreferences.getString("user_role", any()) } returns ""

        // Initialize with custom setup to avoid calling loadSession()
        val field = UserSessionManager::class.java.getDeclaredField("_userSession")
        field.isAccessible = true
        userSessionManager = UserSessionManager(mockContext)
        field.set(userSessionManager, kotlinx.coroutines.flow.MutableStateFlow<User?>(null))
    }

    @Test
    fun `saveSession should save user data to preferences`() {
        // Given
        every { mockEditor.putLong(any(), any()) } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.putBoolean(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs

        // When
        userSessionManager.saveSession(testUser)

        // Then
        verify { mockEditor.putLong("user_id", 1L) }
        verify { mockEditor.putString("username", "testuser") }
        verify { mockEditor.putString("user_role", "CUSTOMER") }
        verify { mockEditor.putBoolean("is_logged_in", true) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `clearSession should clear preferences`() {
        // Given
        every { mockEditor.clear() } returns mockEditor
        every { mockEditor.apply() } just Runs
        
        // When
        userSessionManager.clearSession()
        
        // Then
        verify { mockEditor.clear() }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `getCurrentUserId should return null when not logged in`() {
        // Given
        every { mockSharedPreferences.getBoolean("is_logged_in", false) } returns false
        
        // Заменяем init блок
        val field = UserSessionManager::class.java.getDeclaredField("_userSession")
        field.isAccessible = true
        field.set(userSessionManager, kotlinx.coroutines.flow.MutableStateFlow<User?>(null))
        
        // When
        val result = userSessionManager.getCurrentUserId()
        
        // Then
        assertThat(result).isNull()
    }
    
    @Test
    fun `getCurrentUserId should return user id when logged in`() = runTest {
        // Given
        val field = UserSessionManager::class.java.getDeclaredField("_userSession")
        field.isAccessible = true
        field.set(userSessionManager, kotlinx.coroutines.flow.MutableStateFlow(testUser))
        
        // When
        val result = userSessionManager.getCurrentUserId()
        
        // Then
        assertThat(result).isEqualTo(1L)
    }
    
    @Test
    fun `isLoggedIn should return true when user session exists`() = runTest {
        // Given
        val field = UserSessionManager::class.java.getDeclaredField("_userSession")
        field.isAccessible = true
        field.set(userSessionManager, kotlinx.coroutines.flow.MutableStateFlow(testUser))
        
        // When
        val result = userSessionManager.isLoggedIn()
        
        // Then
        assertThat(result).isTrue()
    }
    
    @Test
    fun `isLoggedIn should return false when user session is null`() = runTest {
        // Given
        val field = UserSessionManager::class.java.getDeclaredField("_userSession")
        field.isAccessible = true
        field.set(userSessionManager, kotlinx.coroutines.flow.MutableStateFlow<User?>(null))
        
        // When
        val result = userSessionManager.isLoggedIn()
        
        // Then
        assertThat(result).isFalse()
    }
    
    @Test
    fun `getCurrentUserRole should return user role when logged in`() = runTest {
        // Given
        val field = UserSessionManager::class.java.getDeclaredField("_userSession")
        field.isAccessible = true
        field.set(userSessionManager, kotlinx.coroutines.flow.MutableStateFlow(testUser))
        
        // When
        val result = userSessionManager.getCurrentUserRole()
        
        // Then
        assertThat(result).isEqualTo(UserRole.CUSTOMER)
    }
    
    @Test
    fun `loadSession should return false when user is not logged in`() {
        // Given
        every { mockSharedPreferences.getBoolean("is_logged_in", false) } returns false
        
        // When
        val result = userSessionManager.loadSession()
        
        // Then
        assertThat(result).isFalse()
    }
}
