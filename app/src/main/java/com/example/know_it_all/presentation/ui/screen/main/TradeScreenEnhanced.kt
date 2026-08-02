package com.example.know_it_all.presentation.ui.screen.main

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.know_it_all.data.model.SwapStatus
import com.example.know_it_all.data.model.SwapType
import com.example.know_it_all.data.model.dto.SwapDTO
import com.example.know_it_all.presentation.ui.components.BottomNavigationBar
import com.example.know_it_all.presentation.ui.navigation.Screen
import com.example.know_it_all.presentation.viewmodel.TradeViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeScreenEnhanced(
    navController: NavHostController,
    tradeViewModel: TradeViewModel,
    userId: String,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tradeState by tradeViewModel.uiState.collectAsState()
    var ratingSwap       by remember { mutableStateOf<SwapDTO?>(null) }
    var proofSwap        by remember { mutableStateOf<SwapDTO?>(null) }
    var sessionSetupSwap by remember { mutableStateOf<SwapDTO?>(null) }

    val ratingSheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val proofSheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(Unit) { tradeViewModel.loadActiveSwaps() }
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) tradeViewModel.loadSwapHistory()
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
                            "TRADE CENTER", fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp, color = WarmGray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "My Swaps", fontSize = 22.sp,
                                fontWeight = FontWeight.Black, color = NearBlack,
                                letterSpacing = (-0.5).sp
                            )
                            if (tradeState.pendingRequestCount > 0) {
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier.size(22.dp)
                                        .background(AcidGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${tradeState.pendingRequestCount}",
                                        fontSize = 11.sp, fontWeight = FontWeight.Black,
                                        color = NearBlack
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Cream).padding(innerPadding)
        ) {
            // Success / error banner
            tradeState.successMessage?.let { msg ->
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(AcidGreen.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(msg, fontSize = 13.sp, color = NearBlack,
                        fontWeight = FontWeight.Medium)
                }
            }
            tradeState.error?.let { err ->
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(ErrorContainerColor).padding(12.dp)
                ) {
                    Text(err, fontSize = 13.sp, color = ErrorRed)
                }
            }

            SegmentedTabControl(
                tabs = listOf("Active", "History"),
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            val swaps = if (selectedTab == 0) tradeState.activeSwaps
                        else tradeState.swapHistory

            if (tradeState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = NearBlack, strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else if (swaps.isEmpty()) {
                TradeEmptyState(isActive = selectedTab == 0)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    itemsIndexed(swaps, key = { _, swap -> swap.swapId }) { index, swap ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(280, index * 50)) +
                                    slideInVertically(tween(280, index * 50)) { it / 5 }
                        ) {
                            SwapCard(
                                swap = swap,
                                currentUserId = userId,
                                onAccept = { tradeViewModel.acceptSwap(swap.swapId) },
                                onCancel = { tradeViewModel.cancelSwap(swap.swapId) },
                                onChat = {
                                    // Navigate to chat screen
                                    val isMentor = swap.mentorId == userId
                                    val counterpart = if (isMentor) swap.learnerName else swap.mentorName
                                    navController.navigate(
                                        "chat/${swap.swapId}/${swap.skillName}/$counterpart"
                                    )
                                },
                                onVideoCall = {
                                    // Open Jitsi Meet link in browser
                                    val roomName = "KnowItAll_${swap.swapId.take(8)}"
                                    val url = "https://meet.jit.si/$roomName"
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    )
                                },
                                onQRHandshake = {
                                    navController.navigate(
                                        "qr_handshake/${swap.swapId}/${swap.skillName}"
                                    )
                                },
                                onMarkComplete = {
                                    if (swap.requiresOnlineVerification) {
                                        // TOKEN/HYBRID: needs proof before completing
                                        proofSwap = swap
                                    } else {
                                        // BARTER: complete directly after QR
                                        tradeViewModel.completeSwap(swap.swapId)
                                    }
                                },
                                onRate = { ratingSwap = swap }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }

    // ── Proof submission sheet ────────────────────────────────────────────────
    proofSwap?.let { swap ->
        ModalBottomSheet(
            onDismissRequest = { proofSwap = null },
            sheetState = proofSheetState,
            containerColor = Cream
        ) {
            ProofSubmissionSheet(
                skillName = swap.skillName,
                expectedDurationMinutes = swap.durationMinutes,
                onSubmit = { description, duration ->
                    tradeViewModel.submitProof(swap.swapId, description, duration)
                    proofSwap = null
                    ratingSwap = swap  // immediately show rating after proof
                },
                onDismiss = { proofSwap = null }
            )
        }
    }

    // ── Rating sheet ──────────────────────────────────────────────────────────
    ratingSwap?.let { swap ->
        if (swap.learnerId == userId) {
            ModalBottomSheet(
                onDismissRequest = { ratingSwap = null },
                sheetState = ratingSheetState,
                containerColor = Cream
            ) {
                RatingSheet(
                    swap = swap,
                    onSubmit = { rating, comment ->
                        tradeViewModel.rateSwap(swap.swapId, rating, comment)
                        ratingSwap = null
                    },
                    onSkip = { ratingSwap = null }
                )
            }
        }
    }
}

// =============================================================================
// Swap Card — full role-aware with all action buttons
// =============================================================================

@Composable
private fun SwapCard(
    swap: SwapDTO,
    currentUserId: String,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    onChat: () -> Unit,
    onVideoCall: () -> Unit,
    onQRHandshake: () -> Unit,
    onMarkComplete: () -> Unit,
    onRate: () -> Unit
) {
    val statusColor = when (swap.status) {
        SwapStatus.ACTIVE    -> AcidGreen
        SwapStatus.REQUESTED -> Ochre
        SwapStatus.COMPLETED -> CharcoalGray
        SwapStatus.CANCELLED -> ErrorRed
        SwapStatus.DISPUTED  -> ErrorRed
    }

    val isMentor        = swap.mentorId == currentUserId
    val counterpartName = if (isMentor) swap.learnerName else swap.mentorName
    val roleLabel       = if (isMentor) "Request from" else "Request to"
    val displaySkill    = swap.skillName.ifBlank { "Skill swap" }

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(CreamDark)
    ) {
        // Status band
        Box(
            modifier = Modifier.width(4.dp).height(120.dp)
                .align(Alignment.CenterStart).background(statusColor)
        )

        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        displaySkill, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        color = NearBlack, letterSpacing = (-0.3).sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("$roleLabel $counterpartName", fontSize = 13.sp, color = CharcoalGray)
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(swap.status.name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                        color = statusColor, letterSpacing = 0.5.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Swap type + session progress
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.background(NearBlack, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "${swap.swapType.name}  ·  ${swap.tokenAmount}T",
                        fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                        color = AcidGreen, letterSpacing = 0.5.sp
                    )
                }
                if (swap.totalSessions > 1) {
                    Box(
                        modifier = Modifier
                            .background(Ochre.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            swap.sessionProgress,
                            fontSize = 10.sp, fontWeight = FontWeight.Medium,
                            color = Ochre
                        )
                    }
                }
            }

            // Escrow info for active TOKEN/HYBRID swaps
            if (swap.status == SwapStatus.ACTIVE && swap.tokensInEscrow > 0) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(AcidGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "🔒 ${swap.tokensInEscrow}T in escrow — released after rating",
                        fontSize = 11.sp, color = CharcoalGray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Action buttons ─────────────────────────────────────────────────
            when {
                // REQUESTED — mentor accepts or declines
                swap.status == SwapStatus.REQUESTED && isMentor -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionChip("Accept", Icons.Default.Check,
                            AcidGreen, NearBlack, onAccept)
                        ActionChip("Decline", Icons.Default.Close,
                            CreamDeep, CharcoalGray, onCancel)
                    }
                }

                // REQUESTED — learner cancels
                swap.status == SwapStatus.REQUESTED && !isMentor -> {
                    ActionChip("Cancel Request", Icons.Default.Close,
                        CreamDeep, CharcoalGray, onCancel)
                }

                // ACTIVE — full action set based on swap type
                swap.status == SwapStatus.ACTIVE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                        // Row 1: Chat always + Video (TOKEN/HYBRID) or QR (BARTER)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionChip("Chat", Icons.Default.Chat,
                                CreamDeep, NearBlack, onChat)

                            if (swap.requiresOnlineVerification) {
                                ActionChip("Video Call", Icons.Default.VideoCall,
                                    NearBlack, AcidGreen, onVideoCall)
                            } else {
                                ActionChip("QR Verify", Icons.Default.QrCode,
                                    NearBlack, AcidGreen, onQRHandshake)
                            }
                        }

                        // Row 2: Mark complete (TOKEN/HYBRID needs proof first) + Cancel
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (swap.requiresOnlineVerification) {
                                ActionChip(
                                    label = if (swap.isSessionComplete) "Submit Proof" else "Mark Session Done",
                                    icon = Icons.Default.Check,
                                    containerColor = AcidGreen,
                                    contentColor = NearBlack,
                                    onClick = onMarkComplete
                                )
                            } else {
                                ActionChip("Mark Complete", Icons.Default.Check,
                                    AcidGreen, NearBlack, onMarkComplete)
                            }
                            ActionChip("Cancel", Icons.Default.Close,
                                CreamDeep, CharcoalGray, onCancel)
                        }
                    }
                }

                // COMPLETED — learner can rate if not yet rated
                swap.status == SwapStatus.COMPLETED && !isMentor -> {
                    if (swap.tokensInEscrow > 0) {
                        ActionChip("Rate & Release Tokens", Icons.Default.Check,
                            AcidGreen, NearBlack, onRate)
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = contentColor,
            modifier = Modifier.size(13.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
    }
}

// =============================================================================
// Rating Sheet with token release preview
// =============================================================================

@Composable
private fun RatingSheet(
    swap: SwapDTO,
    onSubmit: (Int, String) -> Unit,
    onSkip: () -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var comment        by remember { mutableStateOf("") }

    // Token release preview based on selected rating
    val tokensInEscrow = swap.tokensInEscrow
    val toMentor = if (selectedRating > 0)
        com.example.know_it_all.data.model.SessionConfig
            .tokensToMentor(tokensInEscrow, selectedRating.toFloat())
    else 0L
    val toHold = if (selectedRating > 0)
        com.example.know_it_all.data.model.SessionConfig
            .tokensToEscrow(tokensInEscrow, selectedRating.toFloat())
    else 0L

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp).padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Rate Your Session", fontSize = 22.sp, fontWeight = FontWeight.Black, color = NearBlack)
        Text("How was your swap with ${swap.mentorName}?", fontSize = 14.sp, color = CharcoalGray)

        // Star selector
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { star ->
                Text(
                    text = if (star <= selectedRating) "★" else "☆",
                    fontSize = 36.sp,
                    color = if (star <= selectedRating) Ochre else WarmGray,
                    modifier = Modifier.clickable { selectedRating = star }
                )
            }
        }

        // Token release preview
        if (selectedRating > 0 && tokensInEscrow > 0) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        if (toHold > 0) Ochre.copy(alpha = 0.1f)
                        else AcidGreen.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        com.example.know_it_all.data.model.SessionConfig
                            .ratingLabel(selectedRating.toFloat()),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NearBlack
                    )
                    Text(
                        "→ ${toMentor}T released to mentor immediately",
                        fontSize = 12.sp, color = CharcoalGray
                    )
                    if (toHold > 0) {
                        Text(
                            "→ ${toHold}T held in escrow for 7 days",
                            fontSize = 12.sp, color = Ochre
                        )
                    }
                }
            }
        }

        // Comment
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            placeholder = { Text("Leave a comment (optional)", color = WarmGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NearBlack,
                unfocusedBorderColor = WarmGray,
                focusedContainerColor = Cream,
                unfocusedContainerColor = Cream
            )
        )

        Button(
            onClick = { if (selectedRating > 0) onSubmit(selectedRating, comment) },
            enabled = selectedRating > 0,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AcidGreen, contentColor = NearBlack,
                disabledContainerColor = CreamDark, disabledContentColor = WarmGray
            )
        ) {
            Text("Submit Rating", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text("Skip for now", color = CharcoalGray)
        }
    }
}

// =============================================================================
// Segmented tab + Empty state
// =============================================================================

@Composable
private fun SegmentedTabControl(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
            .background(CreamDark, RoundedCornerShape(12.dp)).padding(4.dp)
    ) {
        Row {
            tabs.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                val bgColor by animateColorAsState(
                    if (isSelected) NearBlack else Color.Transparent,
                    animationSpec = tween(200), label = "tab_bg"
                )
                val textColor by animateColorAsState(
                    if (isSelected) Cream else CharcoalGray,
                    animationSpec = tween(200), label = "tab_text"
                )
                Box(
                    modifier = Modifier.weight(1f)
                        .background(bgColor, RoundedCornerShape(9.dp))
                        .clickable { onTabSelected(index) }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = textColor)
                }
            }
        }
    }
}

@Composable
private fun TradeEmptyState(isActive: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (isActive) "🤝" else "📋", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            if (isActive) "No active swaps" else "No swap history yet",
            fontSize = 20.sp, fontWeight = FontWeight.Black, color = NearBlack
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (isActive) "Head to the Radar to find\na mentor and request a swap"
            else "Your completed swaps\nwill appear here",
            fontSize = 13.sp, color = CharcoalGray, lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}