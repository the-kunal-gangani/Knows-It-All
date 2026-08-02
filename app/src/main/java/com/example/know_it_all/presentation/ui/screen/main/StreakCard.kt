package com.example.know_it_all.presentation.ui.screen.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.know_it_all.data.repository.StreakData
import com.example.know_it_all.presentation.viewmodel.StreakViewModel
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.CreamDeep
import com.example.know_it_all.ui.theme.ErrorRed
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.Ochre
import com.example.know_it_all.ui.theme.WarmGray

/**
 * Location: presentation/ui/screen/main/StreakCard.kt
 *
 * Shown inside SkillProfileScreenEnhanced below the profile header.
 * Displays:
 *  - Current streak count (large flame display)
 *  - Progress bar to next milestone
 *  - Milestone badges (claim button if reached)
 *  - Days until streak resets warning
 */
@Composable
fun StreakCard(streakViewModel: StreakViewModel) {
    val state by streakViewModel.uiState.collectAsState()
    val data  = state.streakData

    // Auto-clear success message
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(2000)
            streakViewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NearBlack, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Header row ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "ACTIVITY STREAK",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmGray,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "${data.currentStreak}",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = if (data.currentStreak > 0) AcidGreen else WarmGray,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        "days",
                        fontSize = 16.sp,
                        color = WarmGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Flame emoji — animated scale based on streak
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        if (data.currentStreak > 0) AcidGreen.copy(0.1f)
                        else CreamDeep.copy(0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        data.currentStreak >= 100 -> "👑"
                        data.currentStreak >= 30  -> "🔥"
                        data.currentStreak >= 7   -> "⚡"
                        data.currentStreak > 0    -> "✨"
                        else                      -> "💤"
                    },
                    fontSize = 28.sp
                )
            }
        }

        // ── Streak stats ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StreakStatPill(
                label = "LONGEST",
                value = "${data.longestStreak}d",
                modifier = Modifier.weight(1f)
            )
            StreakStatPill(
                label = "TOTAL ACTIVITIES",
                value = "${data.totalActivities}",
                modifier = Modifier.weight(1f)
            )
            StreakStatPill(
                label = "RESETS IN",
                value = if (data.lastActivityAt == 0L) "—"
                        else "${data.daysUntilReset}d",
                valueColor = if (data.daysUntilReset <= 2 && data.lastActivityAt > 0L)
                    ErrorRed else Cream,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Progress to next milestone ────────────────────────────────────────
        data.nextMilestone?.let { next ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Next: ${StreakData.milestoneLabel(next)}",
                        fontSize = 12.sp,
                        color = Cream,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "+${StreakData.milestoneTokens(next)}T",
                        fontSize = 12.sp,
                        color = AcidGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                val progress by animateFloatAsState(
                    targetValue = data.progressToNextMilestone,
                    animationSpec = tween(800, easing = FastOutSlowInEasing),
                    label = "streak_progress"
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AcidGreen,
                    trackColor = CreamDeep.copy(alpha = 0.3f),
                    strokeCap = StrokeCap.Round
                )

                Text(
                    "${data.currentStreak} / $next days",
                    fontSize = 10.sp,
                    color = WarmGray
                )
            }
        }

        // ── Milestone badges ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StreakData.MILESTONES.forEach { milestone ->
                val isReached  = data.currentStreak >= milestone
                val isClaimed  = milestone in data.claimedMilestones
                val canClaim   = isReached && !isClaimed

                MilestoneBadge(
                    days      = milestone,
                    tokens    = StreakData.milestoneTokens(milestone),
                    isReached = isReached,
                    isClaimed = isClaimed,
                    canClaim  = canClaim,
                    isClaiming = state.isClaiming,
                    onClaim   = { streakViewModel.claimMilestone(milestone) },
                    modifier  = Modifier.weight(1f)
                )
            }
        }

        // ── Success / error messages ──────────────────────────────────────────
        state.successMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AcidGreen.copy(0.15f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(msg, fontSize = 13.sp, color = Cream, fontWeight = FontWeight.Medium)
            }
        }

        state.error?.let { err ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ErrorRed.copy(0.1f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(err, fontSize = 12.sp, color = ErrorRed)
            }
        }

        // ── How streaks work ──────────────────────────────────────────────────
        if (data.currentStreak == 0 && data.totalActivities == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CreamDeep.copy(0.15f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "Complete a swap, send a message, or add a skill to start your streak. " +
                    "Stay active every 7 days to keep it going!",
                    fontSize = 12.sp,
                    color = WarmGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// =============================================================================
// Milestone badge
// =============================================================================

@Composable
private fun MilestoneBadge(
    days: Int,
    tokens: Int,
    isReached: Boolean,
    isClaimed: Boolean,
    canClaim: Boolean,
    isClaiming: Boolean,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emoji = when (days) {
        7   -> "⚡"
        30  -> "🔥"
        100 -> "👑"
        else -> "🏆"
    }

    Column(
        modifier = modifier
            .background(
                when {
                    isClaimed -> AcidGreen.copy(0.1f)
                    canClaim  -> AcidGreen.copy(0.2f)
                    else      -> CreamDeep.copy(0.1f)
                },
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                when {
                    canClaim  -> AcidGreen.copy(0.6f)
                    isClaimed -> AcidGreen.copy(0.3f)
                    else      -> WarmGray.copy(0.2f)
                },
                RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            if (isClaimed) "✅" else if (isReached) emoji else "🔒",
            fontSize = 20.sp
        )
        Text(
            "${days}d",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = if (isReached) Cream else WarmGray
        )
        Text(
            "+${tokens}T",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isReached) AcidGreen else WarmGray
        )

        if (canClaim) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AcidGreen, RoundedCornerShape(6.dp))
                    .clickable(enabled = !isClaiming, onClick = onClaim)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isClaiming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = NearBlack,
                        strokeWidth = 1.5.dp
                    )
                } else {
                    Text(
                        "Claim",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NearBlack
                    )
                }
            }
        } else if (isClaimed) {
            Text("Claimed", fontSize = 9.sp, color = AcidGreen)
        }
    }
}

// =============================================================================
// Stat pill
// =============================================================================

@Composable
private fun StreakStatPill(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = Cream,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(CreamDeep.copy(0.15f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = valueColor)
        Text(label, fontSize = 8.sp, fontWeight = FontWeight.Medium,
            color = WarmGray, letterSpacing = 0.5.sp)
    }
}