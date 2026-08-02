package com.example.know_it_all.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.know_it_all.util.SkillPassportShareHelper
import androidx.lifecycle.viewModelScope
import com.example.know_it_all.data.model.TrustLedger
import com.example.know_it_all.data.repository.FirebaseLedgerRepository
import com.example.know_it_all.data.repository.FirebaseSkillRepository
import com.example.know_it_all.data.repository.FirebaseUserRepository
import com.example.know_it_all.util.SessionManager
import com.example.know_it_all.util.SkillPassportGenerator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class LedgerUiState(
    val ledgerEntries: List<TrustLedger> = emptyList(),
    val trustScore: Float = 0f,
    val tokenBalance: Long = 0L,
    val totalSwaps: Int = 0,
    val completedSwapCount: Int = 0,
    val averageRating: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null
)

class LedgerViewModel(
    private val ledgerRepository: FirebaseLedgerRepository,
    private val skillRepository: FirebaseSkillRepository,
    private val userRepository: FirebaseUserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()

    // Passport PDF — emitted as an event so the screen can open it
    private val _passportEvent = MutableSharedFlow<File?>()
    val passportEvent: SharedFlow<File?> = _passportEvent.asSharedFlow()

    private val userId get() = sessionManager.getUserId() ?: ""

    init {
        observeLedger()
        observeTokenBalance()
        loadTrustScore()
    }

    // ── Real-time ledger ──────────────────────────────────────────────────────

    private fun observeLedger() {
        viewModelScope.launch {
            ledgerRepository.observeUserLedger(userId).collect { entries ->
                _uiState.value = _uiState.value.copy(
                    ledgerEntries = entries,
                    totalSwaps = entries.size
                )
            }
        }
    }

    // ── Real-time token balance ───────────────────────────────────────────────

    private fun observeTokenBalance() {
        viewModelScope.launch {
            userRepository.observeTokenBalance(userId).collect { balance ->
                _uiState.value = _uiState.value.copy(tokenBalance = balance)
            }
        }
    }

    // ── Trust score ───────────────────────────────────────────────────────────

    private fun loadTrustScore() {
        viewModelScope.launch {
            val avgRating = ledgerRepository.getAverageRating(userId)
            val count     = ledgerRepository.getCompletedCount(userId)
            _uiState.value = _uiState.value.copy(
                trustScore  = avgRating ?: 0f,
                averageRating = avgRating ?: 0f,
                totalSwaps  = count,
                completedSwapCount = count
            )
        }
    }

    // ── Verify transaction ────────────────────────────────────────────────────

    fun verifyTransaction(transactionId: String) {
        viewModelScope.launch {
            ledgerRepository.verifyTransaction(transactionId).fold(
                onSuccess = { isValid ->
                    _uiState.value = _uiState.value.copy(
                        error = if (isValid) null else "Transaction verification failed"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Verification failed"
                    )
                }
            )
        }
    }

    // ── Dispute transaction ───────────────────────────────────────────────────

    fun disputeTransaction(transactionId: String, reason: String) {
        viewModelScope.launch {
            ledgerRepository.disputeTransaction(transactionId, reason).fold(
                onSuccess = { loadTrustScore() },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to raise dispute"
                    )
                }
            )
        }
    }

    // ── Generate skill passport PDF ───────────────────────────────────────────

    fun generateSkillPassport(context: android.content.Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val userName  = sessionManager.getUserName() ?: "User"
            val userEmail = sessionManager.getUserEmail() ?: ""

            // Fetch user skills for the passport
            skillRepository.getUserSkills(userId).fold(
                onSuccess = { skills ->
                    val skillInfoList = skills.map { skill ->
                        SkillPassportGenerator.SkillInfo(
                            name        = skill.skillName,
                            category    = skill.category.name,
                            proficiency = skill.proficiencyLevel.name,
                            year        = java.util.Calendar.getInstance()
                                .get(java.util.Calendar.YEAR)
                        )
                    }

                    val file = SkillPassportGenerator.generatePDF(
                        context     = context,
                        userName    = userName,
                        email       = userEmail,
                        foundedDate = "2026",
                        skills      = skillInfoList,
                        trustScore  = _uiState.value.trustScore
                    )

                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _passportEvent.emit(file)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to generate passport"
                    )
                    _passportEvent.emit(null)
                }
            )
        }
    }


    // ── Share PDF ─────────────────────────────────────────────────────────────

    fun sharePDF(context: android.content.Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userName  = sessionManager.getUserName() ?: "User"
            val userEmail = sessionManager.getUserEmail() ?: ""

            skillRepository.getUserSkills(userId).fold(
                onSuccess = { skills ->
                    val file = SkillPassportGenerator.generatePDF(
                        context    = context,
                        userName   = userName,
                        email      = userEmail,
                        foundedDate = "2026",
                        skills     = skills.map { skill ->
                            SkillPassportGenerator.SkillInfo(
                                name        = skill.skillName,
                                category    = skill.category.name,
                                proficiency = skill.proficiencyLevel.name,
                                year        = java.util.Calendar.getInstance()
                                    .get(java.util.Calendar.YEAR)
                            )
                        },
                        trustScore = _uiState.value.trustScore
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    if (file != null) {
                        SkillPassportShareHelper.sharePDF(context, file)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to generate PDF"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to generate passport"
                    )
                }
            )
        }
    }

    // ── Generate and share web link ───────────────────────────────────────────

    fun shareWebLink(context: android.content.Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userName  = sessionManager.getUserName() ?: "User"
            val userEmail = sessionManager.getUserEmail() ?: ""

            skillRepository.getUserSkills(userId).fold(
                onSuccess = { skills ->
                    SkillPassportShareHelper.generateWebLink(
                        userId        = userId,
                        userName      = userName,
                        userEmail     = userEmail,
                        trustScore    = _uiState.value.trustScore,
                        completedSwaps = _uiState.value.totalSwaps,
                        skills        = skills
                    ).fold(
                        onSuccess = { url ->
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            SkillPassportShareHelper.shareWebLink(context, url, userName)
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to generate link"
                            )
                        }
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load skills"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}