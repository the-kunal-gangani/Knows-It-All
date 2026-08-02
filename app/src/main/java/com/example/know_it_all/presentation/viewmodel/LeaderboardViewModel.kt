package com.example.know_it_all.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.know_it_all.data.repository.LeaderboardEntry
import com.example.know_it_all.data.repository.LeaderboardRepository
import com.example.know_it_all.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class LeaderboardMode { GLOBAL, NEARBY }

data class LeaderboardUiState(
    val globalEntries: List<LeaderboardEntry> = emptyList(),
    val nearbyEntries: List<LeaderboardEntry> = emptyList(),
    val currentUserRank: Int = 0,
    val mode: LeaderboardMode = LeaderboardMode.GLOBAL,
    val isLoading: Boolean = false,
    val error: String? = null
)

class LeaderboardViewModel(
    private val leaderboardRepository: LeaderboardRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    private val userId get() = sessionManager.getUserId() ?: ""

    init { loadGlobal() }

    // ── Load global ───────────────────────────────────────────────────────────

    fun loadGlobal() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                mode = LeaderboardMode.GLOBAL
            )
            leaderboardRepository.getGlobalLeaderboard(userId).fold(
                onSuccess = { entries ->
                    val rank = leaderboardRepository.getCurrentUserRank(userId)
                        .getOrDefault(0)
                    _uiState.value = _uiState.value.copy(
                        globalEntries   = entries,
                        currentUserRank = rank,
                        isLoading       = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load leaderboard"
                    )
                }
            )
        }
    }

    // ── Load nearby ───────────────────────────────────────────────────────────

    fun loadNearby(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                mode = LeaderboardMode.NEARBY
            )
            leaderboardRepository.getNearbyLeaderboard(userId, latitude, longitude).fold(
                onSuccess = { entries ->
                    _uiState.value = _uiState.value.copy(
                        nearbyEntries = entries,
                        isLoading     = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load nearby leaderboard"
                    )
                }
            )
        }
    }

    // ── Toggle mode ───────────────────────────────────────────────────────────

    fun setMode(mode: LeaderboardMode, latitude: Double = 0.0, longitude: Double = 0.0) {
        _uiState.value = _uiState.value.copy(mode = mode)
        when (mode) {
            LeaderboardMode.GLOBAL -> if (_uiState.value.globalEntries.isEmpty()) loadGlobal()
            LeaderboardMode.NEARBY -> loadNearby(latitude, longitude)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}