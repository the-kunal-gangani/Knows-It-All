package com.example.know_it_all.presentation.ui.screen.main

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.WarmGray

/**
 * Bottom sheet shown when requesting a TOKEN or HYBRID swap.
 * Learner picks how many sessions and how long each one is.
 * These values are saved to Firestore with the swap.
 */
@Composable
fun SessionSetupSheet(
    swapTypeName: String,
    onConfirm: (totalSessions: Int, durationMinutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSessions by remember { mutableIntStateOf(1) }
    var selectedDuration by remember { mutableIntStateOf(60) }

    val sessionOptions  = listOf(1, 2, 3, 5, 8, 10)
    val durationOptions = listOf(30, 45, 60, 90, 120)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column {
            Text(
                "Session Setup",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = NearBlack
            )
            Text(
                "Configure your $swapTypeName learning plan",
                fontSize = 14.sp,
                color = CharcoalGray
            )
        }

        // Total sessions
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "NUMBER OF SESSIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmGray,
                letterSpacing = 1.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                sessionOptions.forEach { count ->
                    val isSelected = selectedSessions == count
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) NearBlack else CreamDark,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedSessions = count }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$count",
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AcidGreen else CharcoalGray
                        )
                    }
                }
            }
        }

        // Duration per session
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "DURATION PER SESSION",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmGray,
                letterSpacing = 1.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                durationOptions.forEach { mins ->
                    val isSelected = selectedDuration == mins
                    val label = when {
                        mins < 60  -> "${mins}m"
                        mins == 60 -> "1h"
                        else       -> "${mins / 60}h${if (mins % 60 > 0) "${mins % 60}m" else ""}"
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) NearBlack else CreamDark,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedDuration = mins }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AcidGreen else CharcoalGray
                        )
                    }
                }
            }
        }

        // Summary card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AcidGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Session Summary",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NearBlack
                )
                val totalHours = (selectedSessions * selectedDuration) / 60f
                Text(
                    "$selectedSessions sessions × ${selectedDuration}min = " +
                    "${String.format("%.1f", totalHours)} hours total",
                    fontSize = 13.sp,
                    color = CharcoalGray
                )
                Text(
                    "Each session verified via Jitsi video call",
                    fontSize = 11.sp,
                    color = WarmGray
                )
            }
        }

        // Buttons
        Button(
            onClick = { onConfirm(selectedSessions, selectedDuration) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(AcidGreen, NearBlack)
        ) {
            Text(
                "Confirm — $selectedSessions Session${if (selectedSessions > 1) "s" else ""}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Back", color = CharcoalGray)
        }
    }
}