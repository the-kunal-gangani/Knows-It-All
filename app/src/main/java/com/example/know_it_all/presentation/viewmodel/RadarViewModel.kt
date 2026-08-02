package com.example.know_it_all.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.know_it_all.data.model.User
import com.example.know_it_all.data.repository.FirebaseUserRepository
import com.example.know_it_all.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RadarUiState(
    val nearbyUsers: List<User> = emptyList(),
    val currentLat: Double = 0.0,
    val currentLon: Double = 0.0,
    val radiusKm: Double = 5.0,
    val onlineOnly: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedUser: User? = null
)

class RadarViewModel(
    private val userRepository: FirebaseUserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadarUiState())
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    private val userId get() = sessionManager.getUserId() ?: ""

    // ── Load nearby users ─────────────────────────────────────────────────────

    fun loadNearbyUsers() {
        val lat = _uiState.value.currentLat
        val lon = _uiState.value.currentLon

        // Don't query if location not yet available
        if (lat == 0.0 && lon == 0.0) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            userRepository.getNearbyUsers(
                currentUserId = userId,
                latitude      = lat,
                longitude     = lon,
                radiusKm      = _uiState.value.radiusKm
            ).fold(
                onSuccess = { users ->
                    val filtered = if (_uiState.value.onlineOnly)
                        users.filter { it.isOnline }
                    else users

                    _uiState.value = _uiState.value.copy(
                        nearbyUsers = filtered,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load nearby users"
                    )
                }
            )
        }
    }

    // ── Update location ───────────────────────────────────────────────────────

    fun updateLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            currentLat = latitude,
            currentLon = longitude
        )
        // Update location in Firestore so other users can see us on the map
        viewModelScope.launch {
            userRepository.updateLocation(userId, latitude, longitude)
        }
    }

    // ── Refresh location and reload ───────────────────────────────────────────

    fun refreshLocationAndLoad(context: android.content.Context) {
        viewModelScope.launch {
            val location = com.example.know_it_all.util.LocationService(context)
                .getBestAvailableLocation()
            if (location != null) {
                updateLocation(location.latitude, location.longitude)
            }
            loadNearbyUsers()
        }
    }

    // ── Filters ───────────────────────────────────────────────────────────────

    fun toggleOnlineFilter() {
        _uiState.value = _uiState.value.copy(
            onlineOnly = !_uiState.value.onlineOnly
        )
        loadNearbyUsers()
    }

    fun selectUser(user: User?) {
        _uiState.value = _uiState.value.copy(selectedUser = user)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}