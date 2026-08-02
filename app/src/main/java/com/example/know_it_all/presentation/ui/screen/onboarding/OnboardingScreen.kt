package com.example.know_it_all.presentation.ui.screen.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.know_it_all.data.model.DayOfWeek
import com.example.know_it_all.data.model.SkillCategory
import com.example.know_it_all.data.model.SlotType
import com.example.know_it_all.data.model.TimeSlot
import com.example.know_it_all.data.model.dto.SkillCreateRequest
import com.example.know_it_all.data.repository.AvailabilityRepository
import com.example.know_it_all.data.repository.FirebaseSkillRepository
import com.example.know_it_all.presentation.ui.navigation.Screen
import com.example.know_it_all.presentation.viewmodel.AuthViewModel
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.CreamDeep
import com.example.know_it_all.ui.theme.ErrorRed
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.Ochre
import com.example.know_it_all.ui.theme.WarmGray
import kotlinx.coroutines.launch

/**
 * Location: presentation/ui/screen/onboarding/OnboardingScreen.kt
 *
 * 4-step onboarding shown once after first registration.
 * Controlled via SharedPreferences flag "onboarding_complete".
 *
 * Step 1 — Welcome
 * Step 2 — Add first skill
 * Step 3 — Set availability
 * Step 4 — Find mentors (radar preview)
 */
@Composable
fun OnboardingScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    userId: String,
    userName: String
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var currentStep  by remember { mutableIntStateOf(0) }
    var isLoading    by remember { mutableStateOf(false) }
    var error        by remember { mutableStateOf<String?>(null) }

    // Step 2 — skill form state
    var skillName        by remember { mutableStateOf("") }
    var skillDescription by remember { mutableStateOf("") }
    var skillCategory    by remember { mutableStateOf(SkillCategory.DIGITAL) }
    var skillLevel       by remember { mutableStateOf("BEGINNER") }
    var skillTokens      by remember { mutableIntStateOf(10) }

    // Step 3 — availability state
    var availDay      by remember { mutableStateOf(DayOfWeek.MONDAY) }
    var availHour     by remember { mutableIntStateOf(9) }
    var availDuration by remember { mutableIntStateOf(60) }
    var availAdded    by remember { mutableStateOf(false) }

    val skillRepository        = remember { FirebaseSkillRepository() }
    val availabilityRepository = remember { AvailabilityRepository() }

    val totalSteps = 4

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress dots
            StepIndicator(current = currentStep, total = totalSteps)

            Spacer(Modifier.height(32.dp))

            // Animated step content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (slideInHorizontally(tween(350)) { it / 3 } + fadeIn(tween(350)))
                        .togetherWith(
                            slideOutHorizontally(tween(350)) { -it / 3 } + fadeOut(tween(350))
                        )
                },
                label = "onboarding_step"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    when (step) {
                        // ── Step 1: Welcome ───────────────────────────────────
                        0 -> WelcomeStep(userName = userName)

                        // ── Step 2: Add first skill ───────────────────────────
                        1 -> AddSkillStep(
                            skillName        = skillName,
                            onSkillNameChange = { skillName = it },
                            description      = skillDescription,
                            onDescriptionChange = { skillDescription = it },
                            selectedCategory = skillCategory,
                            onCategoryChange  = { skillCategory = it },
                            selectedLevel    = skillLevel,
                            onLevelChange    = { skillLevel = it },
                            tokenValue       = skillTokens,
                            onTokenChange    = { skillTokens = it },
                            error            = error
                        )

                        // ── Step 3: Set availability ──────────────────────────
                        2 -> SetAvailabilityStep(
                            selectedDay    = availDay,
                            onDayChange    = { availDay = it },
                            selectedHour   = availHour,
                            onHourChange   = { availHour = it },
                            selectedDuration = availDuration,
                            onDurationChange = { availDuration = it },
                            slotAdded      = availAdded
                        )

                        // ── Step 4: Find mentors ──────────────────────────────
                        3 -> FindMentorsStep(userName = userName)
                    }
                }
            }

            // Error banner
            AnimatedVisibility(visible = error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            ErrorRed.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(error ?: "", fontSize = 13.sp, color = ErrorRed)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Next / Finish button
            Button(
                onClick = {
                    error = null
                    when (currentStep) {
                        0 -> currentStep = 1

                        1 -> {
                            // Validate skill form
                            if (skillName.isBlank()) {
                                error = "Please enter a skill name"
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                skillRepository.addSkill(
                                    userId = userId,
                                    request = SkillCreateRequest(
                                        skillName        = skillName.trim(),
                                        description      = skillDescription.trim(),
                                        category         = skillCategory.name,
                                        proficiencyLevel = skillLevel,
                                        tokenValue       = skillTokens
                                    )
                                ).fold(
                                    onSuccess = {
                                        isLoading = false
                                        currentStep = 2
                                    },
                                    onFailure = { e ->
                                        isLoading = false
                                        error = e.message ?: "Failed to add skill"
                                    }
                                )
                            }
                        }

                        2 -> {
                            // Save availability slot
                            isLoading = true
                            scope.launch {
                                val slot = TimeSlot(
                                    userId          = userId,
                                    slotType        = SlotType.RECURRING,
                                    dayOfWeek       = availDay,
                                    startHour       = availHour,
                                    startMinute     = 0,
                                    durationMinutes = availDuration
                                )
                                availabilityRepository.addSlot(slot).fold(
                                    onSuccess = {
                                        isLoading = false
                                        availAdded = true
                                        kotlinx.coroutines.delay(600)
                                        currentStep = 3
                                    },
                                    onFailure = { e ->
                                        isLoading = false
                                        error = e.message ?: "Failed to save availability"
                                    }
                                )
                            }
                        }

                        3 -> {
                            // Mark onboarding complete and navigate to Feed
                            context.getSharedPreferences("KnowItAllPrefs", 0)
                                .edit()
                                .putBoolean("onboarding_complete", true)
                                .apply()

                            navController.navigate(Screen.Feed.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NearBlack,
                    contentColor   = AcidGreen,
                    disabledContainerColor = CreamDark,
                    disabledContentColor   = WarmGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AcidGreen,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = when (currentStep) {
                        0    -> "Let's Get Started →"
                        1    -> "Add Skill & Continue →"
                        2    -> "Save & Continue →"
                        else -> "Go to the App 🚀"
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// =============================================================================
// Step 1 — Welcome
// =============================================================================

@Composable
private fun WelcomeStep(userName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("👋", fontSize = 64.sp)

        Text(
            "Welcome,\n${userName.split(" ").first()}!",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = NearBlack,
            textAlign = TextAlign.Center,
            letterSpacing = (-1).sp,
            lineHeight = 42.sp
        )

        Text(
            "KnowItAll connects you with people nearby to trade skills — teach what you know, learn what you don't.",
            fontSize = 15.sp,
            color = CharcoalGray,
            textAlign = TextAlign.Center,
            lineHeight = 23.sp
        )

        Spacer(Modifier.height(8.dp))

        // Feature highlights
        listOf(
            "🗺️" to "Find mentors within 5km on the Skill Radar",
            "🤝" to "Trade skills via barter or SkillTokens",
            "🔒" to "Every session verified and recorded on the Trust Ledger",
            "📜" to "Build a verified Skill Passport for your portfolio"
        ).forEach { (emoji, text) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CreamDark, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 22.sp)
                Text(text, fontSize = 14.sp, color = NearBlack, lineHeight = 20.sp)
            }
        }
    }
}

// =============================================================================
// Step 2 — Add first skill
// =============================================================================

@Composable
private fun AddSkillStep(
    skillName: String,
    onSkillNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedCategory: SkillCategory,
    onCategoryChange: (SkillCategory) -> Unit,
    selectedLevel: String,
    onLevelChange: (String) -> Unit,
    tokenValue: Int,
    onTokenChange: (Int) -> Unit,
    error: String?
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                "Add Your First Skill",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = NearBlack,
                letterSpacing = (-0.5).sp
            )
            Text(
                "What can you teach others?",
                fontSize = 15.sp,
                color = CharcoalGray
            )
        }

        // Skill name
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OnboardingLabel("SKILL NAME")
            OutlinedTextField(
                value = skillName,
                onValueChange = onSkillNameChange,
                placeholder = { Text("e.g. Python, Carpentry, Guitar", color = WarmGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = error != null && skillName.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = NearBlack,
                    unfocusedBorderColor = WarmGray,
                    focusedContainerColor   = Cream,
                    unfocusedContainerColor = Cream
                )
            )
        }

        // Description
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OnboardingLabel("BRIEF DESCRIPTION")
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = { Text("Describe your expertise level briefly", color = WarmGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = NearBlack,
                    unfocusedBorderColor = WarmGray,
                    focusedContainerColor   = Cream,
                    unfocusedContainerColor = Cream
                )
            )
        }

        // Category
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OnboardingLabel("CATEGORY")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkillCategory.values().forEach { cat ->
                    val isSel  = selectedCategory == cat
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
                            .border(
                                1.dp,
                                if (isSel) AcidGreen else androidx.compose.ui.graphics.Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onCategoryChange(cat) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(emoji, fontSize = 20.sp)
                            Text(
                                label,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) AcidGreen else CharcoalGray
                            )
                        }
                    }
                }
            }
        }

        // Proficiency level
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OnboardingLabel("PROFICIENCY")
            val levels = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                levels.forEach { level ->
                    val isSel = selectedLevel == level
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSel) NearBlack else CreamDark,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onLevelChange(level) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            level.take(4),
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) AcidGreen else CharcoalGray
                        )
                    }
                }
            }
        }

        // Token value
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OnboardingLabel("TOKEN VALUE PER SESSION")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 20, 30, 50).forEach { t ->
                    val isSel = tokenValue == t
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSel) AcidGreen else CreamDark,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onTokenChange(t) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            "${t}T",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) NearBlack else CharcoalGray
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Step 3 — Set availability
// =============================================================================

@Composable
private fun SetAvailabilityStep(
    selectedDay: DayOfWeek,
    onDayChange: (DayOfWeek) -> Unit,
    selectedHour: Int,
    onHourChange: (Int) -> Unit,
    selectedDuration: Int,
    onDurationChange: (Int) -> Unit,
    slotAdded: Boolean
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                "When Are You\nAvailable?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = NearBlack,
                letterSpacing = (-0.5).sp,
                lineHeight = 34.sp
            )
            Text(
                "Set your first recurring teaching slot",
                fontSize = 15.sp,
                color = CharcoalGray
            )
        }

        if (slotAdded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AcidGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("✅", fontSize = 20.sp)
                    Text(
                        "Slot saved! Every ${selectedDay.label} from " +
                        "${formatHour(selectedHour)} for ${selectedDuration}min",
                        fontSize = 14.sp,
                        color = NearBlack,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Day picker
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OnboardingLabel("DAY OF WEEK")
            val days = DayOfWeek.values().toList()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(days.take(4), days.drop(4)).forEach { rowDays ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowDays.forEach { day ->
                            val isSel = selectedDay == day
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSel) NearBlack else CreamDark,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onDayChange(day) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    day.shortLabel,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) AcidGreen else CharcoalGray
                                )
                            }
                        }
                        // Pad last row if needed
                        repeat(4 - rowDays.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Time picker
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OnboardingLabel("START TIME")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(7, 8, 9, 10, 11, 14, 16, 18, 19, 20).forEach { h ->
                    val isSel = selectedHour == h
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSel) NearBlack else CreamDark,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onHourChange(h) }
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text(
                            formatHour(h),
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) AcidGreen else CharcoalGray
                        )
                    }
                }
            }
        }

        // Duration picker
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OnboardingLabel("SESSION DURATION")
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
                            .background(
                                if (isSel) NearBlack else CreamDark,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onDurationChange(dur) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            label,
                            fontSize = 13.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) AcidGreen else CharcoalGray
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Step 4 — Find mentors
// =============================================================================

@Composable
private fun FindMentorsStep(userName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("🎉", fontSize = 64.sp)

        Text(
            "You're All Set!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = NearBlack,
            textAlign = TextAlign.Center,
            letterSpacing = (-1).sp
        )

        Text(
            "Your skill is live and your availability is set. Here's what to do next:",
            fontSize = 15.sp,
            color = CharcoalGray,
            textAlign = TextAlign.Center,
            lineHeight = 23.sp
        )

        // Next steps
        listOf(
            Triple("📡", "Check the Skill Feed",
                "Discover new skills and top mentors in your area"),
            Triple("🗺️", "Open the Radar",
                "Find mentors within 5km and send your first swap request"),
            Triple("💬", "Use Chat & Video",
                "Coordinate sessions via in-app chat or Jitsi video call"),
            Triple("🔒", "Verify & Rate",
                "QR handshake for in-person, video for remote — then rate to release tokens")
        ).forEachIndexed { index, (emoji, title, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (index == 0) NearBlack else CreamDark,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(emoji, fontSize = 24.sp)
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (index == 0) AcidGreen else NearBlack
                    )
                    Text(
                        desc,
                        fontSize = 12.sp,
                        color = if (index == 0) WarmGray else CharcoalGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// =============================================================================
// Helpers
// =============================================================================

@Composable
private fun StepIndicator(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { index ->
            val isActive = index == current
            val isPast   = index < current
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(if (isActive) 28.dp else 16.dp)
                    .background(
                        when {
                            isActive -> NearBlack
                            isPast   -> AcidGreen
                            else     -> CreamDeep
                        },
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun OnboardingLabel(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = WarmGray,
        letterSpacing = 1.sp
    )
}

private fun formatHour(hour: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h    = if (hour % 12 == 0) 12 else hour % 12
    return "$h$amPm"
}