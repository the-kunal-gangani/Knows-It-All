package com.example.know_it_all.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.know_it_all.data.repository.StreakData
import com.example.know_it_all.data.repository.StreakRepository
import com.example.know_it_all.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Location: presentation/viewmodel/StreakViewModel.kt
 */
data class StreakUiState(
    val streakData: StreakData = StreakData(),
    val isLoading: Boolean = false,
    val isClaiming: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class StreakViewModel(
    private val streakRepository: StreakRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreakUiState())
    val uiState: StateFlow<StreakUiState> = _uiState.asStateFlow()

    private val userId get() = sessionManager.getUserId() ?: ""

    init { loadStreak() }

    // ── Load streak ───────────────────────────────────────────────────────────

    fun loadStreak() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            streakRepository.getStreak(userId).fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        streakData = data,
                        isLoading  = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load streak"
                    )
                }
            )
        }
    }

    // ── Record activity ───────────────────────────────────────────────────────
    // Call this from any user activity: addSkill, sendMessage, completeSwap

    fun recordActivity() {
        viewModelScope.launch {
            streakRepository.recordActivity(userId).fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(streakData = data)
                },
                onFailure = { /* silent fail */ }
            )
        }
    }

    // ── Claim milestone reward ─────────────────────────────────────────────────

    fun claimMilestone(milestoneDays: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isClaiming = true)
            streakRepository.claimMilestone(userId, milestoneDays).fold(
                onSuccess = { bonusTokens ->
                    // Reload streak to get updated claimed milestones
                    streakRepository.getStreak(userId).onSuccess { data ->
                        _uiState.value = _uiState.value.copy(
                            streakData     = data,
                            isClaiming     = false,
                            successMessage = "+${bonusTokens} tokens claimed! 🎉"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isClaiming = false,
                        error = e.message ?: "Failed to claim reward"
                    )
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}