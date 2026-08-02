package com.example.know_it_all.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.know_it_all.data.model.dto.SwapDTO
import com.example.know_it_all.data.model.dto.SwapRequestBody
import com.example.know_it_all.data.repository.FirebaseSwapRepository
import com.example.know_it_all.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TradeUiState(
    val activeSwaps: List<SwapDTO> = emptyList(),
    val swapHistory: List<SwapDTO> = emptyList(),
    val pendingRequestCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class TradeViewModel(
    private val swapRepository: FirebaseSwapRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradeUiState())
    val uiState: StateFlow<TradeUiState> = _uiState.asStateFlow()

    private val userId get() = sessionManager.getUserId() ?: ""

    init {
        observePendingCount()
        loadActiveSwaps()
    }

    private fun observePendingCount() {
        viewModelScope.launch {
            swapRepository.observePendingCount(userId).collect { count ->
                _uiState.value = _uiState.value.copy(pendingRequestCount = count)
            }
        }
    }

    fun loadActiveSwaps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            swapRepository.observeActiveSwaps(userId).collect { swaps ->
                _uiState.value = _uiState.value.copy(activeSwaps = swaps, isLoading = false)
            }
        }
    }

    fun loadSwapHistory() {
        viewModelScope.launch {
            swapRepository.getSwapHistory(userId).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(swapHistory = it) },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to load history")
                }
            )
        }
    }

    fun requestSwap(request: SwapRequestBody) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            swapRepository.requestSwap(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false,
                        successMessage = "Swap request sent!")
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false,
                        error = e.message ?: "Failed to request swap")
                }
            )
        }
    }

    fun acceptSwap(swapId: String) {
        viewModelScope.launch {
            swapRepository.acceptSwap(swapId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Swap accepted! Tokens locked in escrow.")
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to accept swap")
                }
            )
        }
    }

    fun completeSession(swapId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
        
            swapRepository.completeSession(swapId).fold(
                onSuccess = { swap ->
                    val remainingCount = if (swap.totalSessions > 0) 
                        swap.totalSessions - swap.completedSessions 
                    else 0
                    
                    val msg = if (swap.isSessionComplete)
                        "All sessions done! Submit proof to complete."
                    else
                        "Session recorded. $remainingCount remaining."
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = msg
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to record session"
                    )
                }
            )
        }
    }

    fun submitProof(swapId: String, description: String, durationMinutes: Int) {
        viewModelScope.launch {
            swapRepository.submitProof(swapId, description, durationMinutes).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Proof submitted. You can now complete the swap.")
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to submit proof")
                }
            )
        }
    }

    fun verifyQRScan(swapId: String, scannedPayload: String) {
        viewModelScope.launch {
            swapRepository.verifyQRScan(swapId, scannedPayload, userId).fold(
                onSuccess = { result ->
                    val msg = when (result) {
                        FirebaseSwapRepository.QRVerifyResult.BOTH_VERIFIED ->
                            "✅ Both verified! Session marked complete."
                        FirebaseSwapRepository.QRVerifyResult.ONE_SIDE_DONE ->
                            "Your side verified. Waiting for counterpart to scan."
                        FirebaseSwapRepository.QRVerifyResult.WRONG_SWAP ->
                            "QR code is for a different swap."
                        FirebaseSwapRepository.QRVerifyResult.INVALID ->
                            "Invalid QR code. Please try again."
                    }
                    _uiState.value = _uiState.value.copy(successMessage = msg)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "QR verification failed")
                }
            )
        }
    }

    fun completeSwap(swapId: String) {
        viewModelScope.launch {
            swapRepository.completeSwapWithRating(swapId, 5f, "").fold(
                onSuccess = { _uiState.value = _uiState.value.copy(successMessage = "Swap completed!") },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to complete swap")
                }
            )
        }
    }

    fun rateSwap(swapId: String, rating: Int, comment: String = "") {
        viewModelScope.launch {
            swapRepository.completeSwapWithRating(swapId, rating.toFloat(), comment).fold(
                onSuccess = { swap ->
                    val msg = if (swap.tokensHeld > 0)
                        "Rated! ${swap.tokensToMentor}T released, ${swap.tokensHeld}T held in escrow."
                    else
                        "Rated! ${swap.tokensToMentor}T released to mentor."
                    _uiState.value = _uiState.value.copy(successMessage = msg)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to submit rating")
                }
            )
        }
    }

    fun cancelSwap(swapId: String) {
        viewModelScope.launch {
            swapRepository.cancelSwap(swapId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMessage = "Cancelled. Tokens returned.")
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to cancel")
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}