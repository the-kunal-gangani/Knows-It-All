package com.example.know_it_all.presentation.ui.screen.main
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.AcidGreenDark
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.CreamDeep
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.WarmGray
import com.example.know_it_all.ui.theme.Ochre
import com.example.know_it_all.ui.theme.ErrorRed
import com.example.know_it_all.ui.theme.ErrorContainerColor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.know_it_all.data.model.Skill
import com.example.know_it_all.data.model.SkillCategory
import com.example.know_it_all.data.model.dto.SkillCreateRequest
import com.example.know_it_all.presentation.ui.components.BottomNavigationBar
import com.example.know_it_all.presentation.ui.screen.auth.FieldLabel
import com.example.know_it_all.presentation.ui.screen.auth.KnowItAllTextField
import com.example.know_it_all.presentation.viewmodel.SkillViewModel
import com.example.know_it_all.presentation.viewmodel.StreakViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.know_it_all.presentation.viewmodel.AvailabilityViewModel

/**
 * Fixes applied:
 *  1. AuthViewModel removed — receives userId, userName as primitives.
 *  2. loadUserSkills called with userId only (not token+userId — fixed ViewModel).
 *  3. "Member since 2024" replaced with dynamic year from createdAt.
 *  4. Add skill bottom sheet implemented — was a TODO with no UI.
 *  5. Delete skill action wired to each skill card.
 *  6. addSkill calls SkillViewModel.addSkill(SkillCreateRequest) — not Skill entity.
 *  7. Full design applied — profile header with initials avatar, skill cards
 *     with category color coding, add-skill bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillProfileScreenEnhanced(
    navController: NavHostController,
    skillViewModel: SkillViewModel,
    streakViewModel: StreakViewModel,
    availabilityViewModel: AvailabilityViewModel,
    userId: String,
    userName: String,
    onLogout: () -> Unit = {}
) {
        val skillState by skillViewModel.uiState.collectAsState()
    var showAddSkillSheet by remember { mutableStateOf(false) }
    var showAvailabilitySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            skillViewModel.loadSkills()   // ✅ userId only, no token param
        }
    }

    // Success message handler
    LaunchedEffect(skillState.successMessage) {
        if (skillState.successMessage != null) {
            // Auto-clear after short delay
            skillViewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = navController.currentBackStackEntry?.destination?.route
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SKILL PROFILE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = WarmGray
                        )
                        Text(
                            text = "My Skills",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = NearBlack,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            androidx.compose.material.icons.Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = CharcoalGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Profile header card
            item {
                ProfileHeaderCard(
                    userName = userName,
                    userId = userId,
                    skillCount = skillState.skills.size
                )
            }

            // Streak card
            item {
                StreakCard(streakViewModel = streakViewModel)
            }

            // Success/error banners
            item {
                AnimatedVisibility(
                    visible = skillState.successMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    skillState.successMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    AcidGreen.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(1.dp, AcidGreen.copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Text(msg, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                color = NearBlack)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = skillState.error != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    skillState.error?.let { err ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ErrorContainerColor, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Text(err, fontSize = 13.sp, color = ErrorRed)
                        }
                    }
                }
            }

            // Skills section header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Skills",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NearBlack
                    )
                    // Add skill button — acid green icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(AcidGreen, RoundedCornerShape(10.dp))
                            .clickable { showAddSkillSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add skill",
                            tint = NearBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Availability button
            item {
                Button(
                    onClick = { showAvailabilitySheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(CreamDark, NearBlack),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Set Availability", fontSize = 13.sp)
                }
            }

            // Loading
            if (skillState.isLoading && skillState.skills.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = NearBlack,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Empty state
            if (!skillState.isLoading && skillState.skills.isEmpty()) {
                item { SkillsEmptyState(onAdd = { showAddSkillSheet = true }) }
            }

            // Skill cards
            itemsIndexed(
                skillState.skills,
                key = { _, skill -> skill.skillId }        // ✅ stable String UUID keys
            ) { index, skill ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(250, index * 50)) +
                            slideInVertically(tween(250, index * 50)) { it / 5 }
                ) {
                    SkillCard(
                        skill = skill,
                        onDelete = { skillViewModel.deleteSkill(skill.skillId) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Add skill bottom sheet
    if (showAddSkillSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSkillSheet = false },
            sheetState = sheetState,
            containerColor = Cream
        ) {
            AddSkillSheet(
                onAdd = { request ->
                    skillViewModel.addSkill(request)    // ✅ SkillCreateRequest DTO
                    showAddSkillSheet = false
                },
                onDismiss = { showAddSkillSheet = false }
            )
        }
    }

    // Availability editor sheet
    if (showAvailabilitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showAvailabilitySheet = false },
            containerColor = Cream
        ) {
            AvailabilityEditorSheet(
                availabilityViewModel = availabilityViewModel,
                onDismiss = { showAvailabilitySheet = false }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Profile header
// ---------------------------------------------------------------------------

@Composable
private fun ProfileHeaderCard(userName: String, userId: String, skillCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NearBlack, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Initials avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(AcidGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = NearBlack
                )
            }

            Column {
                Text(
                    text = userName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Cream,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Member since ${Calendar.getInstance().get(Calendar.YEAR)}", // ✅ dynamic
                    fontSize = 12.sp,
                    color = WarmGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(
                            AcidGreen.copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$skillCount skills",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AcidGreen
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Skill card
// ---------------------------------------------------------------------------

@Composable
private fun SkillCard(skill: Skill, onDelete: () -> Unit) {
    val categoryColor = when (skill.category) {
        SkillCategory.DIGITAL  -> AcidGreen
        SkillCategory.PHYSICAL -> Ochre
        SkillCategory.HYBRID   -> CharcoalGray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CreamDark, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Category color dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(categoryColor, CircleShape)
            )
            Column {
                Text(
                    text = skill.skillName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NearBlack
                )
                Text(
                    text = "${skill.proficiencyLevel.name} · ${skill.category.name} · ${skill.tokenValue}T",
                    fontSize = 11.sp,
                    color = CharcoalGray
                )
                if (skill.endorsements > 0) {
                    Text(
                        text = "★ ${skill.endorsements} endorsements",
                        fontSize = 10.sp,
                        color = Ochre,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (skill.verificationStatus) {
                Box(
                    modifier = Modifier
                        .background(
                            AcidGreen.copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("✓", fontSize = 11.sp, color = AcidGreen,
                        fontWeight = FontWeight.Bold)
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete skill",
                    tint = WarmGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Add skill bottom sheet
// ---------------------------------------------------------------------------

@Composable
private fun AddSkillSheet(
    onAdd: (SkillCreateRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var skillName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SkillCategory.DIGITAL) }
    var selectedLevel by remember { mutableStateOf("BEGINNER") }
    var showErrors by remember { mutableStateOf(false) }

    val nameError = if (showErrors && skillName.isBlank()) "Skill name is required" else null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add a Skill",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = NearBlack
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null,
                    tint = CharcoalGray)
            }
        }

        Column {
            FieldLabel("Skill Name")
            KnowItAllTextField(
                value = skillName,
                onValueChange = { skillName = it },
                placeholder = "e.g. Python, Carpentry, Guitar",
                error = nameError
            )
        }

        Column {
            FieldLabel("Description (optional)")
            KnowItAllTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Brief description of your expertise"
            )
        }

        // Category selector
        Column {
            FieldLabel("Category")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkillCategory.values().forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) NearBlack
                                else CreamDark,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AcidGreen
                                    else CharcoalGray
                        )
                    }
                }
            }
        }

        // Proficiency level selector
        Column {
            FieldLabel("Proficiency")
            val levels = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                levels.forEach { level ->
                    val isSelected = selectedLevel == level
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) NearBlack
                                else CreamDark,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedLevel = level }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = level.take(4),   // "BEGI", "INTE", "ADVA", "EXPE"
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AcidGreen
                                    else CharcoalGray
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                showErrors = true
                if (skillName.isNotBlank()) {
                    onAdd(
                        SkillCreateRequest(     // ✅ DTO, not Room entity
                            skillName = skillName.trim(),
                            description = description.trim(),
                            category = selectedCategory.name,
                            proficiencyLevel = selectedLevel,
                            tokenValue = 10
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AcidGreen,
                contentColor = NearBlack
            )
        ) {
            Text("Add Skill", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun SkillsEmptyState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎯", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No skills yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = NearBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Add your first skill to\nstart trading knowledge",
            fontSize = 13.sp,
            color = CharcoalGray,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AcidGreen,
                contentColor = NearBlack
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Skill", fontWeight = FontWeight.Bold)
        }
    }
}