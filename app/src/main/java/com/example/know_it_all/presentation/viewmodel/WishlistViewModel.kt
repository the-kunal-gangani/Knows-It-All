package com.example.know_it_all.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.know_it_all.data.model.SkillCategory
import com.example.know_it_all.data.repository.WishlistItem
import com.example.know_it_all.data.repository.WishlistRepository
import com.example.know_it_all.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Location: presentation/viewmodel/WishlistViewModel.kt
 */
data class WishlistUiState(
    val openWishes: List<WishlistItem> = emptyList(),
    val myWishes: List<WishlistItem> = emptyList(),
    val isLoading: Boolean = false,
    val isPosting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class WishlistViewModel(
    private val wishlistRepository: WishlistRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    private val userId get() = sessionManager.getUserId() ?: ""

    init {
        observeOpenWishes()
        observeMyWishes()
    }

    // ── Real-time streams ─────────────────────────────────────────────────────

    private fun observeOpenWishes() {
        viewModelScope.launch {
            wishlistRepository.observeOpenWishes().collect { wishes ->
                _uiState.value = _uiState.value.copy(
                    openWishes = wishes.filter { it.userId != userId }
                )
            }
        }
    }

    private fun observeMyWishes() {
        viewModelScope.launch {
            wishlistRepository.observeUserWishes(userId).collect { wishes ->
                _uiState.value = _uiState.value.copy(myWishes = wishes)
            }
        }
    }

    // ── Post wish ─────────────────────────────────────────────────────────────

    fun postWish(
        skillName: String,
        category: SkillCategory,
        description: String
    ) {
        if (skillName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a skill name")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPosting = true, error = null)
            wishlistRepository.postWish(userId, skillName, category, description).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isPosting = false,
                        successMessage = "Wish posted! Matching mentors have been notified."
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isPosting = false,
                        error = e.message ?: "Failed to post wish"
                    )
                }
            )
        }
    }

    // ── Close wish ────────────────────────────────────────────────────────────

    fun closeWish(wishId: String) {
        viewModelScope.launch {
            wishlistRepository.closeWish(wishId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Wish marked as fulfilled ✅"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to close wish"
                    )
                }
            )
        }
    }

    // ── Delete wish ───────────────────────────────────────────────────────────

    fun deleteWish(wishId: String) {
        viewModelScope.launch {
            wishlistRepository.deleteWish(wishId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMessage = "Wish removed")
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to delete wish"
                    )
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}