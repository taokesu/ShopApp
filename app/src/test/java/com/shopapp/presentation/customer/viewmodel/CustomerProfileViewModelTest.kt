package com.shopapp.presentation.customer.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopapp.data.model.User
import com.shopapp.data.model.UserRole
import com.shopapp.data.repository.UserRepository
import com.shopapp.data.session.UserSessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerProfileViewModelTest {
    
    private lateinit var viewModel: CustomerProfileViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var userSessionManager: UserSessionManager
    private val testDispatcher = StandardTestDispatcher()
    
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
        Dispatchers.setMain(testDispatcher)
        
        userRepository = mockk(relaxed = true)
        userSessionManager = mockk(relaxed = true)
        
        coEvery { userSessionManager.getCurrentUserId() } returns 1L
        coEvery { userRepository.getUserById(1L) } returns testUser
        
        viewModel = CustomerProfileViewModel(userRepository, userSessionManager)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadUserProfile should update state with user data`() = runTest {
        // Given
        val expectedUser = testUser
        
        // When
        viewModel.loadUserProfile()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.user).isEqualTo(expectedUser)
            assertThat(state.isLoading).isFalse()
            assertThat(state.errorMessage).isNull()
        }
        
        coVerify { userSessionManager.getCurrentUserId() }
        coVerify { userRepository.getUserById(1L) }
    }
    
    @Test
    fun `loadUserProfile should handle error when user ID is null`() = runTest {
        // Given
        coEvery { userSessionManager.getCurrentUserId() } returns null
        
        // When
        viewModel.loadUserProfile()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.errorMessage).isNotNull()
            assertThat(state.user).isNull()
        }
        
        coVerify { userSessionManager.getCurrentUserId() }
        coVerify(exactly = 0) { userRepository.getUserById(any()) }
    }
    
    @Test
    fun `updateUserProfile should update user data`() = runTest {
        // Given
        // Загружаем пользователя перед обновлением
        viewModel.loadUserProfile()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val updatedFullName = "Updated Name"
        val updatedPhone = "987654321"
        val updatedAddress = "Updated Address"
        
        coEvery { userRepository.updateUser(any()) } returns Unit
        
        // When
        viewModel.updateUserProfile(updatedFullName, updatedPhone, updatedAddress)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.user?.fullName).isEqualTo(updatedFullName)
            assertThat(state.user?.phone).isEqualTo(updatedPhone)
            assertThat(state.user?.address).isEqualTo(updatedAddress)
            assertThat(state.successMessage).isNotNull()
        }
        
        coVerify { 
            userRepository.updateUser(match { user ->
                user.id == 1L &&
                user.fullName == updatedFullName &&
                user.phone == updatedPhone &&
                user.address == updatedAddress
            })
        }
    }
    
    @Test
    fun `updateUserProfile should handle empty values`() = runTest {
        // Given
        // Загружаем пользователя перед обновлением
        viewModel.loadUserProfile()
        testDispatcher.scheduler.advanceUntilIdle()
        
        coEvery { userRepository.updateUser(any()) } returns Unit
        
        // When
        viewModel.updateUserProfile("", "", "")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            // Пустые строки должны быть преобразованы в null
            assertThat(state.user?.fullName).isNull()
            assertThat(state.user?.phone).isNull()
            assertThat(state.user?.address).isNull()
            assertThat(state.successMessage).isNotNull()
        }
        
        coVerify { 
            userRepository.updateUser(match { user ->
                user.id == 1L &&
                user.fullName == null &&
                user.phone == null &&
                user.address == null
            })
        }
    }
    
    @Test
    fun `clearMessages should reset error and success messages`() = runTest {
        // Given - Set error and success messages directly
        viewModel.loadUserProfile() // Load the default test user
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Set error and success messages using reflection
        val currentState = viewModel.uiState.value.copy(
            errorMessage = "Test error",
            successMessage = "Test success"
        )
        val field = CustomerProfileViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        field.set(viewModel, kotlinx.coroutines.flow.MutableStateFlow(currentState))
        
        // When
        viewModel.clearMessages()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Only check that messages are cleared
        assertThat(viewModel.uiState.value.errorMessage).isNull()
        assertThat(viewModel.uiState.value.successMessage).isNull()
    }
}
