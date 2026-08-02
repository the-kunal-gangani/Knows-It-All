package com.example.know_it_all.presentation.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.know_it_all.data.model.SlotType
import com.example.know_it_all.data.model.TimeSlot
import com.example.know_it_all.data.repository.AvailabilityRepository
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.Ochre
import com.example.know_it_all.ui.theme.WarmGray

/**
 * Location: presentation/ui/screen/main/SlotPickerSheet.kt
 *
 * Shown inside RequestSwapSheet when the learner picks a time slot
 * from the mentor's availability.
 *
 * If the mentor has no slots set, shows a message saying to coordinate
 * via chat after the swap is accepted.
 */
@Composable
fun SlotPickerSheet(
    mentorUserId: String,
    mentorName: String,
    onSlotSelected: (TimeSlot?) -> Unit,
    onDismiss: () -> Unit
) {
    val availabilityRepository = remember { AvailabilityRepository() }
    var slots     by remember { mutableStateOf<List<TimeSlot>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selected  by remember { mutableStateOf<TimeSlot?>(null) }

    LaunchedEffect(mentorUserId) {
        isLoading = true
        availabilityRepository.getAvailableSlots(mentorUserId).fold(
            onSuccess = { slots = it },
            onFailure = { slots = emptyList() }
        )
        isLoading = false
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
                    "$mentorName's Availability",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = NearBlack
                )
                Text(
                    "Pick a time slot for your session",
                    fontSize = 13.sp,
                    color = CharcoalGray
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null, tint = CharcoalGray)
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = NearBlack,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            slots.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CreamDark, RoundedCornerShape(14.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🗓️", fontSize = 28.sp)
                        Text(
                            "$mentorName hasn't set availability yet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NearBlack,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "You can still send the request and\ncoordinate the time via chat",
                            fontSize = 12.sp,
                            color = CharcoalGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Skip slot selection — proceed without one
                Button(
                    onClick = { onSlotSelected(null) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(AcidGreen, NearBlack)
                ) {
                    Text(
                        "Continue without a slot",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel", color = CharcoalGray)
                }
            }

            else -> {
                // Group by type
                val recurring = slots.filter { it.slotType == SlotType.RECURRING }
                val oneOff    = slots.filter { it.slotType == SlotType.ONE_OFF }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((slots.size * 68 + 60).coerceAtMost(320).dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (recurring.isNotEmpty()) {
                        item {
                            SlotGroupHeader("RECURRING", Icons.Default.Repeat)
                        }
                        items(recurring, key = { it.slotId }) { slot ->
                            SelectableSlotRow(
                                slot       = slot,
                                isSelected = selected?.slotId == slot.slotId,
                                onClick    = { selected = slot }
                            )
                        }
                    }
                    if (oneOff.isNotEmpty()) {
                        item {
                            SlotGroupHeader("ONE-OFF DATES", Icons.Default.Today)
                        }
                        items(oneOff, key = { it.slotId }) { slot ->
                            SelectableSlotRow(
                                slot       = slot,
                                isSelected = selected?.slotId == slot.slotId,
                                onClick    = { selected = slot }
                            )
                        }
                    }
                }

                // Confirm button
                Button(
                    onClick = {
                        if (selected != null) onSlotSelected(selected)
                    },
                    enabled = selected != null,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcidGreen,
                        contentColor = NearBlack,
                        disabledContainerColor = CreamDark,
                        disabledContentColor = WarmGray
                    )
                ) {
                    Text(
                        if (selected != null)
                            "Book — ${selected!!.displayTime}"
                        else
                            "Select a slot",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel", color = CharcoalGray)
                }
            }
        }
    }
}

@Composable
private fun SlotGroupHeader(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = WarmGray, modifier = Modifier.size(13.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
            color = WarmGray, letterSpacing = 1.sp)
    }
}

@Composable
private fun SelectableSlotRow(
    slot: TimeSlot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) NearBlack else CreamDark,
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (isSelected) AcidGreen else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (isSelected) AcidGreen else WarmGray, CircleShape)
        )
        Column(Modifier.weight(1f)) {
            Text(
                slot.displayLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Cream else NearBlack
            )
            Text(
                "${slot.durationMinutes}min session",
                fontSize = 11.sp,
                color = if (isSelected) WarmGray else CharcoalGray
            )
        }
        if (isSelected) {
            Text("✓", fontSize = 14.sp, color = AcidGreen, fontWeight = FontWeight.Bold)
        }
    }
}