package com.example.know_it_all.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.know_it_all.data.model.Skill
import com.example.know_it_all.data.model.dto.SkillCreateRequest
import com.example.know_it_all.data.repository.FirebaseSkillRepository
import com.example.know_it_all.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SkillUiState(
    val skills: List<Skill> = emptyList(),
    val searchResults: List<Skill> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class SkillViewModel(
    private val skillRepository: FirebaseSkillRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkillUiState())
    val uiState: StateFlow<SkillUiState> = _uiState.asStateFlow()

    private val userId get() = sessionManager.getUserId() ?: ""

    init {
        observeSkills()
    }

    // ── Real-time observation ─────────────────────────────────────────────────

    private fun observeSkills() {
        viewModelScope.launch {
            skillRepository.observeUserSkills(userId).collect { skills ->
                _uiState.value = _uiState.value.copy(skills = skills)
            }
        }
    }

    // ── Load skills ───────────────────────────────────────────────────────────

    fun loadSkills() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            skillRepository.getUserSkills(userId).fold(
                onSuccess = { skills ->
                    _uiState.value = _uiState.value.copy(
                        skills = skills,
                        isLoading = false
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

    // ── Add skill ─────────────────────────────────────────────────────────────

    fun addSkill(request: SkillCreateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            skillRepository.addSkill(userId, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Skill added successfully"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to add skill"
                    )
                }
            )
        }
    }

    // ── Update skill ──────────────────────────────────────────────────────────

    fun updateSkill(skillId: String, request: SkillCreateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            skillRepository.updateSkill(skillId, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Skill updated"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update skill"
                    )
                }
            )
        }
    }

    // ── Delete skill ──────────────────────────────────────────────────────────

    fun deleteSkill(skillId: String) {
        viewModelScope.launch {
            skillRepository.deleteSkill(skillId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Skill deleted"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to delete skill"
                    )
                }
            )
        }
    }

    // ── Endorse skill ─────────────────────────────────────────────────────────

    fun endorseSkill(skillId: String) {
        viewModelScope.launch {
            skillRepository.endorseSkill(skillId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Skill endorsed"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to endorse skill"
                    )
                }
            )
        }
    }

    // ── Search skills ─────────────────────────────────────────────────────────

    fun searchSkills(query: String, category: String? = null) {
        viewModelScope.launch {
            skillRepository.searchSkills(query, category).fold(
                onSuccess = { results ->
                    _uiState.value = _uiState.value.copy(searchResults = results)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Search failed"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}