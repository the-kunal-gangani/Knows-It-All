package com.example.know_it_all.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.know_it_all.data.model.DayOfWeek
import com.example.know_it_all.data.model.SlotType
import com.example.know_it_all.data.model.TimeSlot
import com.example.know_it_all.data.repository.AvailabilityRepository
import com.example.know_it_all.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Location: presentation/viewmodel/AvailabilityViewModel.kt
 */
data class AvailabilityUiState(
    val slots: List<TimeSlot> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class AvailabilityViewModel(
    private val availabilityRepository: AvailabilityRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvailabilityUiState())
    val uiState: StateFlow<AvailabilityUiState> = _uiState.asStateFlow()

    private val userId get() = sessionManager.getUserId() ?: ""

    init { observeSlots() }

    // ── Real-time slots ───────────────────────────────────────────────────────

    private fun observeSlots() {
        viewModelScope.launch {
            availabilityRepository.observeUserSlots(userId).collect { slots ->
                _uiState.value = _uiState.value.copy(slots = slots)
            }
        }
    }

    // ── Load mentor's slots (read-only — used by learner) ─────────────────────

    fun loadMentorSlots(mentorUserId: String, onResult: (List<TimeSlot>) -> Unit) {
        viewModelScope.launch {
            availabilityRepository.getAvailableSlots(mentorUserId).fold(
                onSuccess = { onResult(it) },
                onFailure = { onResult(emptyList()) }
            )
        }
    }

    // ── Add recurring slot ────────────────────────────────────────────────────

    fun addRecurringSlot(
        dayOfWeek: DayOfWeek,
        startHour: Int,
        startMinute: Int,
        durationMinutes: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val slot = TimeSlot(
                userId          = userId,
                slotType        = SlotType.RECURRING,
                dayOfWeek       = dayOfWeek,
                startHour       = startHour,
                startMinute     = startMinute,
                durationMinutes = durationMinutes
            )
            availabilityRepository.addSlot(slot).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Slot added — every ${dayOfWeek.label}"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to add slot"
                    )
                }
            )
        }
    }

    // ── Add one-off slot ──────────────────────────────────────────────────────

    fun addOneOffSlot(
        dateEpochMillis: Long,
        startHour: Int,
        startMinute: Int,
        durationMinutes: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Convert to calendar day for dayOfWeek
            val cal = Calendar.getInstance().apply { timeInMillis = dateEpochMillis }
            val dow = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY    -> DayOfWeek.MONDAY
                Calendar.TUESDAY   -> DayOfWeek.TUESDAY
                Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
                Calendar.THURSDAY  -> DayOfWeek.THURSDAY
                Calendar.FRIDAY    -> DayOfWeek.FRIDAY
                Calendar.SATURDAY  -> DayOfWeek.SATURDAY
                else               -> DayOfWeek.SUNDAY
            }

            val slot = TimeSlot(
                userId          = userId,
                slotType        = SlotType.ONE_OFF,
                dayOfWeek       = dow,
                specificDate    = dateEpochMillis,
                startHour       = startHour,
                startMinute     = startMinute,
                durationMinutes = durationMinutes
            )
            availabilityRepository.addSlot(slot).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "One-off slot added"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to add slot"
                    )
                }
            )
        }
    }

    // ── Delete slot ───────────────────────────────────────────────────────────

    fun deleteSlot(slotId: String) {
        viewModelScope.launch {
            availabilityRepository.deleteSlot(userId, slotId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMessage = "Slot removed")
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to delete slot"
                    )
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}