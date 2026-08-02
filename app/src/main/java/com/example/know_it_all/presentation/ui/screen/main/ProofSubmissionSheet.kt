package com.example.know_it_all.presentation.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.ErrorContainerColor
import com.example.know_it_all.ui.theme.ErrorRed
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.WarmGray

/**
 * Location: presentation/ui/screen/main/ProofSubmissionSheet.kt
 *
 * Shown before marking a TOKEN or HYBRID swap as complete.
 * User submits a text description of what was covered
 * and selects the actual session duration.
 */
@Composable
fun ProofSubmissionSheet(
    skillName: String,
    expectedDurationMinutes: Int,
    onSubmit: (description: String, durationMinutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var description    by remember { mutableStateOf("") }
    var actualDuration by remember { mutableIntStateOf(expectedDurationMinutes) }
    var showError      by remember { mutableStateOf(false) }

    val durationOptions = listOf(15, 30, 45, 60, 90, 120)
        .let { base ->
            // Always include the expected duration
            if (expectedDurationMinutes !in base)
                (base + expectedDurationMinutes).sorted()
            else base
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Column {
            Text(
                "Submit Proof",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = NearBlack
            )
            Text(
                "Verify your $skillName session before completing",
                fontSize = 14.sp,
                color = CharcoalGray
            )
        }

        // Description field
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "WHAT WAS COVERED",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmGray,
                letterSpacing = 1.sp
            )
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    if (it.isNotBlank()) showError = false
                },
                placeholder = {
                    Text(
                        "e.g. Covered Python lists, loops, and functions. " +
                        "Practiced 3 exercises together.",
                        color = WarmGray,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NearBlack,
                    unfocusedBorderColor = WarmGray,
                    focusedContainerColor = Cream,
                    unfocusedContainerColor = Cream
                )
            )
            if (showError) {
                Text(
                    "Please describe what was covered in the session",
                    fontSize = 12.sp,
                    color = ErrorRed
                )
            }
        }

        // Actual duration selector
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "ACTUAL DURATION",
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
                    val isSelected  = actualDuration == mins
                    val isExpected  = mins == expectedDurationMinutes
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
                            .clickable { actualDuration = mins }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AcidGreen else CharcoalGray
                            )
                            if (isExpected) {
                                Text(
                                    "agreed",
                                    fontSize = 9.sp,
                                    color = if (isSelected) AcidGreen.copy(alpha = 0.7f)
                                            else WarmGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Info box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AcidGreen.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(
                "After submitting proof, you can rate the session " +
                "and tokens will be released based on your rating.",
                fontSize = 12.sp,
                color = CharcoalGray,
                lineHeight = 18.sp
            )
        }

        // Submit button
        Button(
            onClick = {
                if (description.isBlank()) {
                    showError = true
                } else {
                    onSubmit(description.trim(), actualDuration)
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(AcidGreen, NearBlack)
        ) {
            Text("Submit Proof", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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