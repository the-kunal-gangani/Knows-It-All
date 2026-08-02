package com.example.know_it_all.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.know_it_all.data.model.User
import com.example.know_it_all.data.repository.FirebaseUserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class AuthViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    private lateinit var userRepository: FirebaseUserRepository
    
    private lateinit var authViewModel: AuthViewModel
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        authViewModel = AuthViewModel(userRepository)
    }
    
    @Test
    fun testLoginSuccess() = runTest(testDispatcher) {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val expectedToken = "token_abc123"
        
        whenever(userRepository.login(email, password)).thenReturn(
            Result.success(expectedToken)
        )
        
        // Act
        authViewModel.login(email, password)
        advanceUntilIdle()
        
        // Assert - Verify login was successful
        assertEquals(expectedToken, authViewModel.currentToken.value)
    }
    
    @Test
    fun testLoginFailure() = runTest(testDispatcher) {
        // Arrange
        val email = "test@example.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"
        
        whenever(userRepository.login(email, password)).thenReturn(
            Result.failure(Exception(errorMessage))
        )
        
        // Act
        authViewModel.login(email, password)
        advanceUntilIdle()
        
        // Assert
        assertTrue(authViewModel.currentToken.value.isEmpty())
    }
    
    @Test
    fun testRegisterSuccess() = runTest(testDispatcher) {
        // Arrange
        val user = User(
            uid = "user_123",
            name = "John Doe",
            email = "john@example.com",
            tokenBalance = 100.0,
            trustScore = 0.0
        )
        
        whenever(userRepository.register(user)).thenReturn(
            Result.success(user)
        )
        
        // Act
        authViewModel.register(user.name, user.email, "password123")
        advanceUntilIdle()
        
        // Assert
        assertNotNull(authViewModel.currentUser.value)
        assertEquals(user.email, authViewModel.currentUser.value?.email)
    }
    
    @Test
    fun testRegisterEmptyEmail() = runTest(testDispatcher) {
        // Act
        authViewModel.register("John", "", "password123")
        advanceUntilIdle()
        
        // Assert
        assertTrue(authViewModel.currentToken.value.isEmpty())
    }
    
    @Test
    fun testLogout() = runTest(testDispatcher) {
        // Arrange - set initial state as logged in
        authViewModel.currentToken.value = "token_123"
        authViewModel.currentUser.value = User(
            uid = "user_123",
            name = "John",
            email = "john@example.com"
        )
        
        // Act
        authViewModel.logout()
        advanceUntilIdle()
        
        // Assert
        assertTrue(authViewModel.currentToken.value.isEmpty())
    }
}
