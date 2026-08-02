package com.example.know_it_all.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.know_it_all.data.local.dao.UserDao
import com.example.know_it_all.data.model.User
import com.example.know_it_all.data.remote.api.UserService
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserRepositoryTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @Mock
    private lateinit var userDao: UserDao
    
    @Mock
    private lateinit var userService: UserService
    
    private lateinit var userRepository: UserRepository
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        userRepository = UserRepository(userDao)
    }
    
    @Test
    fun testGetUserById() = runTest {
        // Arrange
        val userId = "user_123"
        val expectedUser = User(
            uid = userId,
            name = "Alice",
            email = "alice@example.com",
            tokenBalance = 50.0,
            trustScore = 4.5
        )
        
        whenever(userDao.getUserById(userId)).thenReturn(expectedUser)
        
        // Act
        val result = userRepository.getUserById(userId)
        
        // Assert
        assertEquals(expectedUser, result)
        verify(userDao).getUserById(userId)
    }
    
    @Test
    fun testSaveUser() = runTest {
        // Arrange
        val user = User(
            uid = "user_456",
            name = "Bob",
            email = "bob@example.com"
        )
        
        // Act
        userRepository.saveUserLocally(user)
        
        // Assert
        verify(userDao).insertUser(user)
    }
    
    @Test
    fun testUpdateUserLocation() = runTest {
        // Arrange
        val userId = "user_123"
        val latitude = 18.5204
        val longitude = 73.8567
        
        // Act
        userRepository.updateUserLocation(userId, latitude, longitude)
        
        // Assert
        verify(userDao).updateUserLocation(userId, latitude, longitude)
    }
    
    @Test
    fun testGetNearbyUsers() = runTest {
        // Arrange
        val userLatitude = 18.5204
        val userLongitude = 73.8567
        val radiusKm = 5
        
        val nearbyUsers = listOf(
            User(uid = "user_1", name = "User1", email = "user1@example.com"),
            User(uid = "user_2", name = "User2", email = "user2@example.com")
        )
        
        whenever(userDao.getNearbyUsers(userLatitude, userLongitude, radiusKm))
            .thenReturn(nearbyUsers)
        
        // Act
        val result = userRepository.getNearbyUsers(userLatitude, userLongitude, radiusKm)
        
        // Assert
        assertEquals(2, result.size)
        assertTrue(result.any { it.uid == "user_1" })
        assertTrue(result.any { it.uid == "user_2" })
    }
    
    @Test
    fun testGetUserByEmail() = runTest {
        // Arrange
        val email = "alice@example.com"
        val expectedUser = User(
            uid = "user_123",
            name = "Alice",
            email = email
        )
        
        whenever(userDao.getUserByEmail(email)).thenReturn(expectedUser)
        
        // Act
        val result = userRepository.getUserByEmail(email)
        
        // Assert
        assertNotNull(result)
        assertEquals(email, result?.email)
    }
    
    @Test
    fun testDeleteUser() = runTest {
        // Arrange
        val userId = "user_123"
        
        // Act
        userRepository.deleteUser(userId)
        
        // Assert
        verify(userDao).deleteUserById(userId)
    }
}
