package com.example.know_it_all.presentation.ui.screen.main

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.know_it_all.data.model.GroupSession
import com.example.know_it_all.data.model.GroupSessionStatus
import com.example.know_it_all.data.model.SkillCategory
import com.example.know_it_all.presentation.viewmodel.GroupSessionViewModel
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.ErrorContainerColor
import com.example.know_it_all.ui.theme.ErrorRed
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.Ochre
import com.example.know_it_all.ui.theme.WarmGray

// =============================================================================
// Group Session Card — shown in Feed
// =============================================================================

/**
 * Location: presentation/ui/screen/main/GroupSessionCard.kt
 * (put both composables in this single file)
 */
@Composable
fun GroupSessionCard(
    session: GroupSession,
    currentUserId: String,
    onClick: () -> Unit
) {
    val categoryColor = when (session.category) {
        SkillCategory.DIGITAL  -> AcidGreen
        SkillCategory.PHYSICAL -> Ochre
        SkillCategory.HYBRID   -> CharcoalGray
    }
    val spotsLeft  = session.spotsLeft
    val isMentor   = session.mentorId == currentUserId

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NearBlack)
            .clickable(onClick = onClick)
    ) {
        // Category top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(categoryColor,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("👥", fontSize = 14.sp)
                    Text(
                        "GROUP SESSION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor,
                        letterSpacing = 1.5.sp
                    )
                }
                // Spots remaining pill
                Box(
                    modifier = Modifier
                        .background(
                            if (spotsLeft <= 1) ErrorRed.copy(0.2f)
                            else AcidGreen.copy(0.15f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (session.isFull) "FULL" else "$spotsLeft spots left",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (spotsLeft <= 1) ErrorRed else AcidGreen
                    )
                }
            }

            // Skill name + mentor
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AcidGreen.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        session.mentorName.take(1).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = AcidGreen
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        session.skillName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Cream,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        "by ${session.mentorName}",
                        fontSize = 12.sp,
                        color = WarmGray
                    )
                }
            }

            // Description
            if (session.description.isNotBlank()) {
                Text(
                    session.description,
                    fontSize = 13.sp,
                    color = WarmGray,
                    maxLines = 2,
                    lineHeight = 19.sp
                )
            }

            // Stats row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SessionStatChip("${session.tokenPricePerLearner}T / learner", AcidGreen)
                SessionStatChip("${session.durationMinutes}min", WarmGray)
                SessionStatChip(
                    "${session.currentLearners}/${session.maxLearners} joined",
                    categoryColor
                )
            }

            // Schedule
            if (session.isScheduled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🗓", fontSize = 12.sp)
                    Text(
                        session.displaySchedule,
                        fontSize = 12.sp,
                        color = Ochre,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // CTA
            if (!isMentor && !session.isFull &&
                session.status == GroupSessionStatus.OPEN) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AcidGreen, RoundedCornerShape(10.dp))
                        .clickable(onClick = onClick)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Join for ${session.tokenPricePerLearner}T →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NearBlack
                    )
                }
            }

            if (isMentor) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmGray.copy(0.15f), RoundedCornerShape(10.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Your session — tap to manage",
                        fontSize = 12.sp,
                        color = WarmGray
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionStatChip(text: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .background(color.copy(0.1f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

// =============================================================================
// Group Session Detail Screen
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSessionDetailScreen(
    navController: NavHostController,
    groupSessionViewModel: GroupSessionViewModel,
    sessionId: String,
    currentUserId: String
) {
    val context = LocalContext.current
    val state   by groupSessionViewModel.uiState.collectAsState()

    // Find the session from open or my sessions
    val session = (state.openSessions + state.mySessions)
        .firstOrNull { it.sessionId == sessionId }

    var showRatingSheet by remember { mutableStateOf(false) }
    val ratingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(sessionId) {
        groupSessionViewModel.observeEnrollments(sessionId)
    }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage?.contains("cancelled") == true ||
            state.successMessage?.contains("Left") == true) {
            kotlinx.coroutines.delay(1000)
            groupSessionViewModel.clearMessage()
            navController.popBackStack()
        }
    }

    if (session == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NearBlack)
        }
        return
    }

    val isMentor    = session.mentorId == currentUserId
    val enrollments = state.currentEnrollments
    val myEnrollment = enrollments.firstOrNull { it.learnerId == currentUserId }
    val isEnrolled   = myEnrollment != null

    val categoryColor = when (session.category) {
        SkillCategory.DIGITAL  -> AcidGreen
        SkillCategory.PHYSICAL -> Ochre
        SkillCategory.HYBRID   -> CharcoalGray
    }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GROUP SESSION", fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp, color = WarmGray)
                        Text(session.skillName, fontSize = 20.sp,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Status banner
            item {
                StatusBanner(session = session, categoryColor = categoryColor)
            }

            // Success / error
            state.successMessage?.let {
                item {
                    Box(Modifier.fillMaxWidth()
                        .background(AcidGreen.copy(0.15f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                    ) {
                        Text(it, fontSize = 13.sp, color = NearBlack,
                            fontWeight = FontWeight.Medium)
                    }
                }
            }
            state.error?.let {
                item {
                    Box(Modifier.fillMaxWidth()
                        .background(ErrorContainerColor, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                    ) { Text(it, fontSize = 13.sp, color = ErrorRed) }
                }
            }

            // Session info card
            item {
                SessionInfoCard(session = session, categoryColor = categoryColor)
            }

            // Enrolled learners list
            item {
                Text("ENROLLED LEARNERS",
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = WarmGray, letterSpacing = 1.sp)
            }

            if (enrollments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(CreamDark, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No learners yet — share this session!",
                            fontSize = 13.sp, color = WarmGray,
                            textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(enrollments, key = { it.enrollmentId }) { enrollment ->
                    LearnerRow(enrollment = enrollment)
                }
            }

            // Action buttons
            item {
                Spacer(Modifier.height(8.dp))
                ActionButtons(
                    session    = session,
                    isMentor   = isMentor,
                    isEnrolled = isEnrolled,
                    isJoining  = state.isJoining,
                    onJoin     = { groupSessionViewModel.joinSession(sessionId) },
                    onLeave    = { groupSessionViewModel.leaveSession(sessionId) },
                    onStart    = { groupSessionViewModel.startSession(sessionId) },
                    onComplete = { groupSessionViewModel.completeSession(sessionId) },
                    onCancel   = { groupSessionViewModel.cancelSession(sessionId) },
                    onVideoCall = {
                        val url = "https://meet.jit.si/${session.jitsiRoomName}"
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        )
                    },
                    onRate = { showRatingSheet = true }
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // Rating sheet
    if (showRatingSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRatingSheet = false },
            sheetState = ratingSheetState,
            containerColor = Cream
        ) {
            GroupRatingSheet(
                skillName = session.skillName,
                mentorName = session.mentorName,
                onSubmit = { rating, comment ->
                    groupSessionViewModel.rateSession(sessionId, rating, comment)
                    showRatingSheet = false
                },
                onDismiss = { showRatingSheet = false }
            )
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun StatusBanner(
    session: GroupSession,
    categoryColor: androidx.compose.ui.graphics.Color
) {
    val (emoji, label, bg) = when (session.status) {
        GroupSessionStatus.OPEN      -> Triple("🟢", "Open — accepting learners",
            AcidGreen.copy(0.1f))
        GroupSessionStatus.ACTIVE    -> Triple("🔴", "Live now — session in progress",
            ErrorRed.copy(0.1f))
        GroupSessionStatus.COMPLETED -> Triple("✅", "Completed",
            WarmGray.copy(0.1f))
        GroupSessionStatus.CANCELLED -> Triple("❌", "Cancelled",
            ErrorRed.copy(0.1f))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NearBlack)
    }
}

@Composable
private fun SessionInfoCard(
    session: GroupSession,
    categoryColor: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NearBlack, RoundedCornerShape(16.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("MENTOR", fontSize = 10.sp, color = WarmGray, letterSpacing = 1.sp)
                Text(session.mentorName, fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, color = Cream)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("CAPACITY", fontSize = 10.sp, color = WarmGray, letterSpacing = 1.sp)
                Text("${session.currentLearners}/${session.maxLearners}",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = categoryColor)
            }
        }
        if (session.description.isNotBlank()) {
            Text(session.description, fontSize = 13.sp,
                color = WarmGray, lineHeight = 20.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoPill("${session.tokenPricePerLearner}T per learner", AcidGreen)
            InfoPill("${session.durationMinutes}min", WarmGray)
            if (session.isScheduled) InfoPill(session.displaySchedule, Ochre)
        }
    }
}

@Composable
private fun InfoPill(
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .background(color.copy(0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun LearnerRow(
    enrollment: com.example.know_it_all.data.model.GroupEnrollment
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CreamDark, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(NearBlack, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(enrollment.learnerName.take(1).uppercase(),
                fontSize = 14.sp, fontWeight = FontWeight.Black, color = AcidGreen)
        }
        Column(Modifier.weight(1f)) {
            Text(enrollment.learnerName, fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold, color = NearBlack)
            Text("${enrollment.tokensLocked}T locked",
                fontSize = 11.sp, color = CharcoalGray)
        }
        if (enrollment.rating != null) {
            Text("★ ${String.format("%.1f", enrollment.rating)}",
                fontSize = 13.sp, color = Ochre, fontWeight = FontWeight.Bold)
        } else {
            Text(enrollment.status.name, fontSize = 10.sp, color = WarmGray)
        }
    }
}

@Composable
private fun ActionButtons(
    session: GroupSession,
    isMentor: Boolean,
    isEnrolled: Boolean,
    isJoining: Boolean,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onVideoCall: () -> Unit,
    onRate: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when {
            // ── Mentor actions ────────────────────────────────────────────────
            isMentor && session.status == GroupSessionStatus.OPEN -> {
                Button(onClick = onStart,
                    enabled = session.currentLearners > 0,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(AcidGreen, NearBlack)
                ) { Text("Start Session", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                OutlinedButton(onClick = onCancel,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Cancel Session", color = ErrorRed) }
            }

            isMentor && session.status == GroupSessionStatus.ACTIVE -> {
                Button(onClick = onVideoCall,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(NearBlack, AcidGreen)
                ) {
                    Icon(Icons.Default.VideoCall, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Open Video Room", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Button(onClick = onComplete,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(AcidGreen, NearBlack)
                ) { Text("Mark Complete", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
            }

            // ── Learner actions ───────────────────────────────────────────────
            !isMentor && !isEnrolled &&
            session.status == GroupSessionStatus.OPEN && !session.isFull -> {
                Button(
                    onClick = onJoin,
                    enabled = !isJoining,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(AcidGreen, NearBlack)
                ) {
                    if (isJoining) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp),
                            color = NearBlack, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Join for ${session.tokenPricePerLearner}T",
                        fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            !isMentor && isEnrolled &&
            session.status == GroupSessionStatus.OPEN -> {
                Box(Modifier.fillMaxWidth()
                    .background(AcidGreen.copy(0.1f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
                ) {
                    Text("✅ You're enrolled! You'll be notified when it starts.",
                        fontSize = 13.sp, color = NearBlack, fontWeight = FontWeight.Medium)
                }
                OutlinedButton(onClick = onLeave,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Leave Session", color = CharcoalGray) }
            }

            !isMentor && isEnrolled &&
            session.status == GroupSessionStatus.ACTIVE -> {
                Button(onClick = onVideoCall,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(NearBlack, AcidGreen)
                ) {
                    Icon(Icons.Default.VideoCall, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Join Video Room", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            !isMentor && isEnrolled &&
            session.status == GroupSessionStatus.COMPLETED -> {
                Button(onClick = onRate,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(AcidGreen, NearBlack)
                ) {
                    Text("Rate & Release Tokens", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// =============================================================================
// Group Rating Sheet
// =============================================================================

@Composable
private fun GroupRatingSheet(
    skillName: String,
    mentorName: String,
    onSubmit: (Float, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var comment        by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Rate Group Session", fontSize = 22.sp,
            fontWeight = FontWeight.Black, color = NearBlack)
        Text("How was $mentorName's $skillName session?",
            fontSize = 14.sp, color = CharcoalGray)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { star ->
                Text(
                    if (star <= selectedRating) "★" else "☆",
                    fontSize = 36.sp,
                    color = if (star <= selectedRating) Ochre else WarmGray,
                    modifier = Modifier.clickable { selectedRating = star }
                )
            }
        }

        androidx.compose.material3.OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            placeholder = { Text("Leave a comment (optional)", color = WarmGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NearBlack, unfocusedBorderColor = WarmGray,
                focusedContainerColor = Cream, unfocusedContainerColor = Cream
            )
        )

        Button(
            onClick = { if (selectedRating > 0) onSubmit(selectedRating.toFloat(), comment) },
            enabled = selectedRating > 0,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AcidGreen, contentColor = NearBlack,
                disabledContainerColor = CreamDark, disabledContentColor = WarmGray
            )
        ) { Text("Submit Rating", fontWeight = FontWeight.Bold, fontSize = 15.sp) }

        androidx.compose.material3.TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Skip for now", color = CharcoalGray) }
    }
}