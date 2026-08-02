package com.example.know_it_all.presentation.ui.screen.main

import android.Manifest
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.know_it_all.data.repository.LeaderboardEntry
import com.example.know_it_all.presentation.ui.components.BottomNavigationBar
import com.example.know_it_all.presentation.viewmodel.LeaderboardMode
import com.example.know_it_all.presentation.viewmodel.LeaderboardViewModel
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.ErrorContainerColor
import com.example.know_it_all.ui.theme.ErrorRed
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.Ochre
import com.example.know_it_all.ui.theme.WarmGray
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Location: presentation/ui/screen/main/LeaderboardScreen.kt
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LeaderboardScreen(
    navController: NavHostController,
    leaderboardViewModel: LeaderboardViewModel,
    currentLat: Double = 0.0,
    currentLon: Double = 0.0
) {
    val state = leaderboardViewModel.uiState.collectAsState().value
    val context = LocalContext.current

    val locationPermission = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val entries = when (state.mode) {
        LeaderboardMode.GLOBAL -> state.globalEntries
        LeaderboardMode.NEARBY -> state.nearbyEntries
    }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute  = navController.currentBackStackEntry?.destination?.route
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "LEADERBOARD",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = WarmGray
                        )
                        Text(
                            "Top Mentors",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = NearBlack,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream),
                actions = {
                    IconButton(onClick = {
                        when (state.mode) {
                            LeaderboardMode.GLOBAL -> leaderboardViewModel.loadGlobal()
                            LeaderboardMode.NEARBY -> leaderboardViewModel.loadNearby(
                                currentLat, currentLon
                            )
                        }
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = NearBlack,
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
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Mode toggle ───────────────────────────────────────────────────
            item {
                ModeToggle(
                    selected = state.mode,
                    onSelect = { mode ->
                        if (mode == LeaderboardMode.NEARBY) {
                            if (!locationPermission.status.isGranted) {
                                locationPermission.launchPermissionRequest()
                            }
                            leaderboardViewModel.setMode(mode, currentLat, currentLon)
                        } else {
                            leaderboardViewModel.setMode(mode)
                        }
                    }
                )
            }

            // ── Current user's rank badge ──────────────────────────────────────
            if (state.currentUserRank > 0 && state.mode == LeaderboardMode.GLOBAL) {
                item {
                    YourRankBadge(rank = state.currentUserRank)
                }
            }

            // ── Error ─────────────────────────────────────────────────────────
            state.error?.let { err ->
                item {
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

            // ── Loading ───────────────────────────────────────────────────────
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = NearBlack,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp)
                            )
                            Text("Loading leaderboard...", fontSize = 13.sp, color = CharcoalGray)
                        }
                    }
                }
            }

            // ── Empty state ───────────────────────────────────────────────────
            if (!state.isLoading && entries.isEmpty() && state.error == null) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                if (state.mode == LeaderboardMode.NEARBY) "🗺️" else "🏆",
                                fontSize = 40.sp
                            )
                            Text(
                                if (state.mode == LeaderboardMode.NEARBY)
                                    "No mentors found nearby"
                                else
                                    "No mentors yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = NearBlack
                            )
                            Text(
                                if (state.mode == LeaderboardMode.NEARBY)
                                    "Try expanding your radius or\nswitching to Global"
                                else
                                    "Be the first to complete a swap\nand claim the top spot!",
                                fontSize = 13.sp,
                                color = CharcoalGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // ── Top 3 podium ──────────────────────────────────────────────────
            if (!state.isLoading && entries.size >= 3) {
                item {
                    PodiumRow(
                        first  = entries[0],
                        second = entries[1],
                        third  = entries[2]
                    )
                }
            }

            // ── Ranks 4–10 ────────────────────────────────────────────────────
            if (!state.isLoading && entries.size > 3) {
                itemsIndexed(
                    entries.drop(3),
                    key = { _, entry -> entry.user.uid }
                ) { _, entry ->
                    LeaderboardRow(
                        entry       = entry,
                        showDistance = state.mode == LeaderboardMode.NEARBY
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// =============================================================================
// Mode toggle
// =============================================================================

@Composable
private fun ModeToggle(
    selected: LeaderboardMode,
    onSelect: (LeaderboardMode) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CreamDark, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Row {
            LeaderboardMode.values().forEach { mode ->
                val isSelected = selected == mode
                val label = when (mode) {
                    LeaderboardMode.GLOBAL -> "🌍  Global"
                    LeaderboardMode.NEARBY -> "📍  Nearby (5km)"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) NearBlack else androidx.compose.ui.graphics.Color.Transparent,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { onSelect(mode) }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Cream else CharcoalGray
                    )
                }
            }
        }
    }
}

// =============================================================================
// Your rank badge
// =============================================================================

@Composable
private fun YourRankBadge(rank: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NearBlack, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🎯", fontSize = 20.sp)
        Text(
            "Your global rank",
            fontSize = 14.sp,
            color = WarmGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            "#$rank",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = AcidGreen
        )
    }
}

// =============================================================================
// Top 3 podium
// =============================================================================

@Composable
private fun PodiumRow(
    first: LeaderboardEntry,
    second: LeaderboardEntry,
    third: LeaderboardEntry
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd place
        PodiumCard(
            entry      = second,
            medal      = "🥈",
            height     = 120.dp,
            bgColor    = CreamDark,
            nameColor  = NearBlack,
            modifier   = Modifier.weight(1f)
        )
        // 1st place — tallest
        PodiumCard(
            entry      = first,
            medal      = "🥇",
            height     = 160.dp,
            bgColor    = NearBlack,
            nameColor  = AcidGreen,
            modifier   = Modifier.weight(1f)
        )
        // 3rd place
        PodiumCard(
            entry      = third,
            medal      = "🥉",
            height     = 100.dp,
            bgColor    = CreamDark,
            nameColor  = NearBlack,
            modifier   = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PodiumCard(
    entry: LeaderboardEntry,
    medal: String,
    height: androidx.compose.ui.unit.Dp,
    bgColor: androidx.compose.ui.graphics.Color,
    nameColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(height)
            .background(bgColor, RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(medal, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (bgColor == NearBlack) AcidGreen.copy(0.2f) else NearBlack,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                entry.user.name.take(1).uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (bgColor == NearBlack) AcidGreen else Cream
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            entry.user.name.split(" ").first(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = nameColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            "★ ${String.format("%.1f", entry.user.trustScore)}",
            fontSize = 11.sp,
            color = Ochre
        )
    }
}

// =============================================================================
// Rank 4–10 rows
// =============================================================================

@Composable
private fun LeaderboardRow(
    entry: LeaderboardEntry,
    showDistance: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CreamDark, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Rank number
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(NearBlack, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "#${entry.rank}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = WarmGray
            )
        }

        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(NearBlack, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                entry.user.name.take(1).uppercase(),
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = AcidGreen
            )
        }

        // Info
        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    entry.user.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NearBlack
                )
                if (entry.user.isOnline) {
                    Box(Modifier.size(6.dp).background(AcidGreen, CircleShape))
                }
            }
            Text(
                entry.topSkillName,
                fontSize = 12.sp,
                color = CharcoalGray
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "★ ${String.format("%.1f", entry.averageRating)}",
                    fontSize = 11.sp,
                    color = Ochre,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${entry.completedSwaps} swaps",
                    fontSize = 11.sp,
                    color = WarmGray
                )
                if (showDistance && entry.distanceKm > 0) {
                    Text(
                        "${String.format("%.1f", entry.distanceKm)}km",
                        fontSize = 11.sp,
                        color = WarmGray
                    )
                }
            }
        }

        // Trust score
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${(entry.user.trustScore * 10).toInt()}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = NearBlack
            )
            Text(
                "TRUST",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmGray,
                letterSpacing = 0.5.sp
            )
        }
    }
}