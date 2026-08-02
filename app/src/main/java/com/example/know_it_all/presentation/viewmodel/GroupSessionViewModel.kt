package com.example.know_it_all.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.know_it_all.data.model.GroupEnrollment
import com.example.know_it_all.data.model.GroupSession
import com.example.know_it_all.data.model.SkillCategory
import com.example.know_it_all.data.repository.GroupSessionRepository
import com.example.know_it_all.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Location: presentation/viewmodel/GroupSessionViewModel.kt
 */
data class GroupSessionUiState(
    val openSessions: List<GroupSession> = emptyList(),
    val mySessions: List<GroupSession> = emptyList(),
    val currentEnrollments: List<GroupEnrollment> = emptyList(),
    val selectedSession: GroupSession? = null,
    val isLoading: Boolean = false,
    val isJoining: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class GroupSessionViewModel(
    private val groupSessionRepository: GroupSessionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupSessionUiState())
    val uiState: StateFlow<GroupSessionUiState> = _uiState.asStateFlow()

    private val userId get() = sessionManager.getUserId() ?: ""

    init {
        observeOpenSessions()
        observeMySessions()
    }

    // ── Real-time streams ─────────────────────────────────────────────────────

    private fun observeOpenSessions() {
        viewModelScope.launch {
            groupSessionRepository.observeOpenSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(
                    openSessions = sessions.filter { it.mentorId != userId }
                )
            }
        }
    }

    private fun observeMySessions() {
        viewModelScope.launch {
            groupSessionRepository.observeMentorSessions(userId).collect { sessions ->
                _uiState.value = _uiState.value.copy(mySessions = sessions)
            }
        }
    }

    fun observeEnrollments(sessionId: String) {
        viewModelScope.launch {
            groupSessionRepository.observeEnrollments(sessionId).collect { enrollments ->
                _uiState.value = _uiState.value.copy(currentEnrollments = enrollments)
            }
        }
    }

    // ── Create session ────────────────────────────────────────────────────────

    fun createSession(
        skillName: String,
        skillId: String,
        description: String,
        category: SkillCategory,
        maxLearners: Int,
        durationMinutes: Int,
        tokenPricePerLearner: Long,
        scheduledAt: Long = 0L,
        isScheduled: Boolean = false
    ) {
        if (skillName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a skill name")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            groupSessionRepository.createSession(
                mentorId             = userId,
                skillName            = skillName,
                skillId              = skillId,
                description          = description,
                category             = category,
                maxLearners          = maxLearners,
                durationMinutes      = durationMinutes,
                tokenPricePerLearner = tokenPricePerLearner,
                scheduledAt          = scheduledAt,
                isScheduled          = isScheduled
            ).fold(
                onSuccess = { session ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Group session created! It's now live in the Feed."
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create session"
                    )
                }
            )
        }
    }

    // ── Join session ──────────────────────────────────────────────────────────

    fun joinSession(sessionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isJoining = true, error = null)
            groupSessionRepository.joinSession(sessionId, userId).fold(
                onSuccess = { enrollment ->
                    _uiState.value = _uiState.value.copy(
                        isJoining = false,
                        successMessage = "Joined! ${enrollment.tokensLocked}T locked. " +
                            "You'll be notified when the session starts."
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isJoining = false,
                        error = e.message ?: "Failed to join session"
                    )
                }
            )
        }
    }

    // ── Start session ─────────────────────────────────────────────────────────

    fun startSession(sessionId: String) {
        viewModelScope.launch {
            groupSessionRepository.startSession(sessionId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Session started! All learners notified."
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to start session"
                    )
                }
            )
        }
    }

    // ── Complete session ──────────────────────────────────────────────────────

    fun completeSession(sessionId: String) {
        viewModelScope.launch {
            groupSessionRepository.completeSession(sessionId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Session completed! Learners can now rate."
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to complete session"
                    )
                }
            )
        }
    }

    // ── Rate session ──────────────────────────────────────────────────────────

    fun rateSession(sessionId: String, rating: Float, comment: String = "") {
        viewModelScope.launch {
            groupSessionRepository.rateSession(sessionId, userId, rating, comment).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Rating submitted! Tokens released to mentor."
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to submit rating"
                    )
                }
            )
        }
    }

    // ── Cancel session ────────────────────────────────────────────────────────

    fun cancelSession(sessionId: String) {
        viewModelScope.launch {
            groupSessionRepository.cancelSession(sessionId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Session cancelled. All learners refunded."
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to cancel session"
                    )
                }
            )
        }
    }

    // ── Leave session ─────────────────────────────────────────────────────────

    fun leaveSession(sessionId: String) {
        viewModelScope.launch {
            groupSessionRepository.leaveSession(sessionId, userId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Left session. Tokens refunded."
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to leave session"
                    )
                }
            )
        }
    }

    fun selectSession(session: GroupSession?) {
        _uiState.value = _uiState.value.copy(selectedSession = session)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}