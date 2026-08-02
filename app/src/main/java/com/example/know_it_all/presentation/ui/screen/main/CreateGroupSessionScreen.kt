package com.example.know_it_all.presentation.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.know_it_all.data.model.SkillCategory
import com.example.know_it_all.presentation.viewmodel.GroupSessionViewModel
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.ErrorContainerColor
import com.example.know_it_all.ui.theme.ErrorRed
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.WarmGray

/**
 * Location: presentation/ui/screen/main/CreateGroupSessionScreen.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupSessionScreen(
    navController: NavHostController,
    groupSessionViewModel: GroupSessionViewModel
) {
    val state by groupSessionViewModel.uiState.collectAsState()

    var skillName    by remember { mutableStateOf("") }
    var description  by remember { mutableStateOf("") }
    var category     by remember { mutableStateOf(SkillCategory.DIGITAL) }
    var maxLearners  by remember { mutableIntStateOf(5) }
    var duration     by remember { mutableIntStateOf(60) }
    var pricePerLearner by remember { mutableLongStateOf(10L) }

    // Navigate back on success
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(1200)
            groupSessionViewModel.clearMessage()
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CREATE", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp, color = WarmGray)
                        Text("Group Session", fontSize = 20.sp,
                            fontWeight = FontWeight.Black, color = NearBlack)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back", tint = NearBlack)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Success / error
            state.successMessage?.let {
                StatusBox(it, isError = false)
            }
            state.error?.let {
                StatusBox(it, isError = true)
            }

            // Skill name
            FormSection("SKILL TO TEACH") {
                OutlinedTextField(
                    value = skillName,
                    onValueChange = { skillName = it },
                    placeholder = { Text("e.g. Python Basics, Guitar Chords", color = WarmGray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors()
                )
            }

            // Description
            FormSection("SESSION DESCRIPTION") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = {
                        Text("What will learners get from this session?", color = WarmGray)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4,
                    colors = fieldColors()
                )
            }

            // Category
            FormSection("CATEGORY") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkillCategory.values().forEach { cat ->
                        val isSel = category == cat
                        val (emoji, label) = when (cat) {
                            SkillCategory.DIGITAL  -> "💻" to "Digital"
                            SkillCategory.PHYSICAL -> "🔨" to "Physical"
                            SkillCategory.HYBRID   -> "⚡" to "Hybrid"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSel) NearBlack else CreamDark,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { category = cat }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(emoji, fontSize = 20.sp)
                                Text(
                                    label, fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AcidGreen else CharcoalGray
                                )
                            }
                        }
                    }
                }
            }

            // Max learners
            FormSection("MAX LEARNERS") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 3, 5, 8, 10).forEach { n ->
                        val isSel = maxLearners == n
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSel) NearBlack else CreamDark,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { maxLearners = n }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$n", fontSize = 15.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) AcidGreen else CharcoalGray
                            )
                        }
                    }
                }
            }

            // Duration
            FormSection("DURATION") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 45, 60, 90, 120).forEach { mins ->
                        val isSel = duration == mins
                        val label = when {
                            mins < 60  -> "${mins}m"
                            mins == 60 -> "1h"
                            else -> "${mins / 60}h${if (mins % 60 > 0) "${mins % 60}m" else ""}"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSel) NearBlack else CreamDark,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { duration = mins }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label, fontSize = 13.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) AcidGreen else CharcoalGray
                            )
                        }
                    }
                }
            }

            // Price per learner
            FormSection("PRICE PER LEARNER") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5L, 10L, 15L, 20L, 30L, 50L).forEach { price ->
                            val isSel = pricePerLearner == price
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSel) AcidGreen else CreamDark,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { pricePerLearner = price }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    "${price}T", fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) NearBlack else CharcoalGray
                                )
                            }
                        }
                    }
                    // Total earnings preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AcidGreen.copy(0.1f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("If session fills up:", fontSize = 13.sp, color = CharcoalGray)
                            Text(
                                "You earn ${pricePerLearner * maxLearners}T total",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NearBlack
                            )
                        }
                    }
                }
            }

            // Create button
            Button(
                onClick = {
                    groupSessionViewModel.createSession(
                        skillName            = skillName.trim(),
                        skillId              = "",
                        description          = description.trim(),
                        category             = category,
                        maxLearners          = maxLearners,
                        durationMinutes      = duration,
                        tokenPricePerLearner = pricePerLearner
                    )
                },
                enabled = !state.isLoading && skillName.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NearBlack, contentColor = AcidGreen,
                    disabledContainerColor = CreamDark, disabledContentColor = WarmGray
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp),
                        color = AcidGreen, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Text("Create Group Session", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            color = WarmGray, letterSpacing = 1.sp)
        content()
    }
}

@Composable
private fun StatusBox(message: String, isError: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isError) ErrorContainerColor else AcidGreen.copy(0.15f),
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            message, fontSize = 13.sp,
            color = if (isError) ErrorRed else NearBlack,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NearBlack,
    unfocusedBorderColor = WarmGray,
    focusedContainerColor = Cream,
    unfocusedContainerColor = Cream
)