package com.example.know_it_all.presentation.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.know_it_all.data.model.DayOfWeek
import com.example.know_it_all.data.model.SlotType
import com.example.know_it_all.data.model.TimeSlot
import com.example.know_it_all.presentation.viewmodel.AvailabilityViewModel
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.CreamDeep
import com.example.know_it_all.ui.theme.ErrorContainerColor
import com.example.know_it_all.ui.theme.ErrorRed
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.Ochre
import com.example.know_it_all.ui.theme.WarmGray

/**
 * Location: presentation/ui/screen/main/AvailabilityEditorSheet.kt
 *
 * Shown inside SkillProfileScreen.
 * Mentor can add/remove recurring and one-off availability slots.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityEditorSheet(
    availabilityViewModel: AvailabilityViewModel,
    onDismiss: () -> Unit
) {
    val state by availabilityViewModel.uiState.collectAsState()
    var showAddForm   by remember { mutableStateOf(false) }
    var addSlotType   by remember { mutableStateOf(SlotType.RECURRING) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Form state
    var selectedDay      by remember { mutableStateOf(DayOfWeek.MONDAY) }
    var selectedDate     by remember { mutableStateOf(0L) }
    var selectedHour     by remember { mutableIntStateOf(9) }
    var selectedMinute   by remember { mutableIntStateOf(0) }
    var selectedDuration by remember { mutableIntStateOf(60) }

    // Auto-clear success message
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            showAddForm = false
            kotlinx.coroutines.delay(1500)
            availabilityViewModel.clearMessage()
        }
    }

    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis ?: 0L
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "My Availability",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NearBlack
                )
                Text(
                    "Set when you're available to teach",
                    fontSize = 13.sp,
                    color = CharcoalGray
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null, tint = CharcoalGray)
            }
        }

        // Success / error banners
        state.successMessage?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AcidGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(it, fontSize = 13.sp, color = NearBlack, fontWeight = FontWeight.Medium)
            }
        }
        state.error?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ErrorContainerColor, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(it, fontSize = 13.sp, color = ErrorRed)
            }
        }

        // Existing slots list
        if (state.slots.isEmpty() && !showAddForm) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CreamDark, RoundedCornerShape(14.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🗓️", fontSize = 32.sp)
                    Text(
                        "No slots yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NearBlack
                    )
                    Text(
                        "Add your first availability slot\nso learners know when to book you",
                        fontSize = 13.sp,
                        color = CharcoalGray,
                        lineHeight = 19.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((state.slots.size * 68).coerceAtMost(240).dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.slots, key = { it.slotId }) { slot ->
                    SlotRow(
                        slot = slot,
                        onDelete = { availabilityViewModel.deleteSlot(slot.slotId) }
                    )
                }
            }
        }

        // Add slot form
        AnimatedVisibility(
            visible = showAddForm,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            AddSlotForm(
                slotType        = addSlotType,
                onSlotTypeChange = { addSlotType = it },
                selectedDay     = selectedDay,
                onDayChange     = { selectedDay = it },
                selectedDate    = selectedDate,
                onPickDate      = { showDatePicker = true },
                selectedHour    = selectedHour,
                onHourChange    = { selectedHour = it },
                selectedMinute  = selectedMinute,
                onMinuteChange  = { selectedMinute = it },
                selectedDuration = selectedDuration,
                onDurationChange = { selectedDuration = it },
                onConfirm = {
                    if (addSlotType == SlotType.RECURRING) {
                        availabilityViewModel.addRecurringSlot(
                            dayOfWeek       = selectedDay,
                            startHour       = selectedHour,
                            startMinute     = selectedMinute,
                            durationMinutes = selectedDuration
                        )
                    } else {
                        availabilityViewModel.addOneOffSlot(
                            dateEpochMillis = selectedDate,
                            startHour       = selectedHour,
                            startMinute     = selectedMinute,
                            durationMinutes = selectedDuration
                        )
                    }
                },
                onCancel = { showAddForm = false }
            )
        }

        // Add slot button
        if (!showAddForm) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Add recurring
                Button(
                    onClick = {
                        addSlotType = SlotType.RECURRING
                        showAddForm = true
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(NearBlack, AcidGreen)
                ) {
                    Icon(Icons.Default.Repeat, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Recurring", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                // Add one-off
                Button(
                    onClick = {
                        addSlotType = SlotType.ONE_OFF
                        showAddForm = true
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(CreamDark, NearBlack)
                ) {
                    Icon(Icons.Default.Today, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("One-off", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Existing slot row ─────────────────────────────────────────────────────────

@Composable
private fun SlotRow(slot: TimeSlot, onDelete: () -> Unit) {
    val isBooked = !slot.isAvailable
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isBooked) Ochre.copy(alpha = 0.1f) else CreamDark,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Type indicator
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    if (slot.slotType == SlotType.RECURRING) NearBlack else AcidGreen.copy(0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (slot.slotType == SlotType.RECURRING)
                    Icons.Default.Repeat else Icons.Default.Today,
                contentDescription = null,
                tint = if (slot.slotType == SlotType.RECURRING) AcidGreen else NearBlack,
                modifier = Modifier.size(15.dp)
            )
        }

        Column(Modifier.weight(1f)) {
            Text(
                slot.displayLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NearBlack
            )
            Text(
                "${slot.durationMinutes}min · ${if (isBooked) "Booked" else "Available"}",
                fontSize = 11.sp,
                color = if (isBooked) Ochre else CharcoalGray
            )
        }

        if (!isBooked) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = WarmGray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ── Add slot form ─────────────────────────────────────────────────────────────

@Composable
private fun AddSlotForm(
    slotType: SlotType,
    onSlotTypeChange: (SlotType) -> Unit,
    selectedDay: DayOfWeek,
    onDayChange: (DayOfWeek) -> Unit,
    selectedDate: Long,
    onPickDate: () -> Unit,
    selectedHour: Int,
    onHourChange: (Int) -> Unit,
    selectedMinute: Int,
    onMinuteChange: (Int) -> Unit,
    selectedDuration: Int,
    onDurationChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CreamDark, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Add ${if (slotType == SlotType.RECURRING) "Recurring" else "One-off"} Slot",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = NearBlack
        )

        // Slot type toggle
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SlotType.values().forEach { type ->
                val isSel = slotType == type
                Box(
                    modifier = Modifier
                        .background(if (isSel) NearBlack else Cream, RoundedCornerShape(8.dp))
                        .clickable { onSlotTypeChange(type) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        type.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) AcidGreen else CharcoalGray
                    )
                }
            }
        }

        // Day or date
        if (slotType == SlotType.RECURRING) {
            SectionLabel("Day of week")
            // Two rows of day chips
            val days = DayOfWeek.values().toList()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(days.take(4), days.drop(4)).forEach { rowDays ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowDays.forEach { day ->
                            val isSel = selectedDay == day
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSel) NearBlack else Cream,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onDayChange(day) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    day.shortLabel,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AcidGreen else CharcoalGray
                                )
                            }
                        }
                    }
                }
            }
        } else {
            SectionLabel("Date")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Cream, RoundedCornerShape(10.dp))
                    .clickable { onPickDate() }
                    .padding(14.dp)
            ) {
                Text(
                    text = if (selectedDate > 0L) {
                        val cal = java.util.Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                        }
                        val months = listOf("Jan","Feb","Mar","Apr","May","Jun",
                                           "Jul","Aug","Sep","Oct","Nov","Dec")
                        "${cal.get(java.util.Calendar.DAY_OF_MONTH)} " +
                        "${months[cal.get(java.util.Calendar.MONTH)]} " +
                        "${cal.get(java.util.Calendar.YEAR)}"
                    } else "Tap to pick a date",
                    fontSize = 14.sp,
                    color = if (selectedDate > 0L) NearBlack else WarmGray
                )
            }
        }

        // Time picker
        SectionLabel("Start time")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Hours
            Column(Modifier.weight(1f)) {
                Text("Hour", fontSize = 11.sp, color = WarmGray)
                NumberPicker(
                    value = selectedHour,
                    range = 0..23,
                    format = { h ->
                        val amPm = if (h < 12) "AM" else "PM"
                        val disp = if (h % 12 == 0) 12 else h % 12
                        "$disp $amPm"
                    },
                    onValueChange = onHourChange
                )
            }
            // Minutes
            Column(Modifier.weight(1f)) {
                Text("Minute", fontSize = 11.sp, color = WarmGray)
                NumberPicker(
                    value = selectedMinute,
                    range = listOf(0, 15, 30, 45),
                    format = { m -> m.toString().padStart(2, '0') },
                    onValueChange = onMinuteChange
                )
            }
        }

        // Duration
        SectionLabel("Duration")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30, 45, 60, 90, 120).forEach { dur ->
                val isSel = selectedDuration == dur
                val label = when {
                    dur < 60  -> "${dur}m"
                    dur == 60 -> "1h"
                    else      -> "${dur / 60}h${if (dur % 60 > 0) "${dur % 60}m" else ""}"
                }
                Box(
                    modifier = Modifier
                        .background(if (isSel) NearBlack else Cream, RoundedCornerShape(8.dp))
                        .clickable { onDurationChange(dur) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) AcidGreen else CharcoalGray
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(AcidGreen, NearBlack)
            ) {
                Text("Add Slot", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", color = CharcoalGray)
            }
        }
    }
}

// ── Simple number picker ──────────────────────────────────────────────────────

@Composable
private fun NumberPicker(
    value: Int,
    range: IntRange,
    format: (Int) -> String,
    onValueChange: (Int) -> Unit
) {
    val options = range.toList()
    NumberPickerFromList(value, options, format, onValueChange)
}

@Composable
private fun NumberPicker(
    value: Int,
    range: List<Int>,
    format: (Int) -> String,
    onValueChange: (Int) -> Unit
) {
    NumberPickerFromList(value, range, format, onValueChange)
}

@Composable
private fun NumberPickerFromList(
    value: Int,
    options: List<Int>,
    format: (Int) -> String,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .background(Cream, RoundedCornerShape(10.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val currentIdx = options.indexOf(value).coerceAtLeast(0)

        Box(
            modifier = Modifier
                .background(CreamDeep, RoundedCornerShape(8.dp))
                .clickable {
                    val prev = if (currentIdx > 0) options[currentIdx - 1]
                               else options.last()
                    onValueChange(prev)
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) { Text("‹", fontSize = 16.sp, color = NearBlack, fontWeight = FontWeight.Bold) }

        Text(
            format(value),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = NearBlack,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Box(
            modifier = Modifier
                .background(CreamDeep, RoundedCornerShape(8.dp))
                .clickable {
                    val next = if (currentIdx < options.size - 1) options[currentIdx + 1]
                            else options.first()
                    onValueChange(next)
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) { Text("›", fontSize = 16.sp, color = NearBlack, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = WarmGray,
        letterSpacing = 0.5.sp
    )
}