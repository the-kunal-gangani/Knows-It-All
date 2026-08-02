package com.example.know_it_all.presentation.ui.screen.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.know_it_all.data.model.SkillCategory
import com.example.know_it_all.data.repository.FeedItem
import com.example.know_it_all.presentation.viewmodel.FeedViewModel
import com.example.know_it_all.presentation.viewmodel.WishlistViewModel
import com.example.know_it_all.presentation.ui.components.BottomNavigationBar
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.ErrorContainerColor
import com.example.know_it_all.ui.theme.ErrorRed
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.Ochre
import com.example.know_it_all.ui.theme.WarmGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun categoryColor(cat: SkillCategory) = when (cat) {
    SkillCategory.DIGITAL  -> AcidGreen
    SkillCategory.PHYSICAL -> Ochre
    SkillCategory.HYBRID   -> CharcoalGray
}

private fun categoryEmoji(cat: SkillCategory) = when (cat) {
    SkillCategory.DIGITAL  -> "💻"
    SkillCategory.PHYSICAL -> "🔨"
    SkillCategory.HYBRID   -> "⚡"
}

private fun timeAgoString(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    return when {
        diff < 60_000L      -> "just now"
        diff < 3_600_000L   -> "${diff / 60_000}m ago"
        diff < 86_400_000L  -> "${diff / 3_600_000}h ago"
        diff < 604_800_000L -> "${diff / 86_400_000}d ago"
        else                -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(ts))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavHostController,
    feedViewModel: FeedViewModel,
    wishlistViewModel: WishlistViewModel? = null,
    onMentorConnect: (userId: String) -> Unit = {}
) {
    val feedState by feedViewModel.uiState.collectAsState()
    var showPostWishSheet by remember { mutableStateOf(false) }

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
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "DISCOVER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = WarmGray
                        )
                        Text(
                            text = "Skill Feed",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = NearBlack,
                            letterSpacing = (-1.5).sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream),
                actions = {
                    if (wishlistViewModel != null) {
                        IconButton(onClick = { showPostWishSheet = true }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NearBlack, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✦",
                                    fontSize = 14.sp,
                                    color = AcidGreen,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    IconButton(onClick = { feedViewModel.refresh() }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CreamDark, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = if (feedState.isRefreshing) AcidGreen else NearBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            feedState.isLoading -> FeedLoadingState(modifier = Modifier.padding(innerPadding))
            feedState.error != null -> FeedErrorState(
                message = feedState.error ?: "",
                onRetry = { feedViewModel.loadFeed() },
                modifier = Modifier.padding(innerPadding)
            )
            feedState.items.isEmpty() -> FeedEmptyState(modifier = Modifier.padding(innerPadding))
            else -> {
                val trending = feedState.items.filterIsInstance<FeedItem.TrendingCategory>()
                val mainItems = feedState.items.filterNot { it is FeedItem.TrendingCategory }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (trending.isNotEmpty()) {
                        item { TrendingCategoriesStrip(categories = trending) }
                    }

                    itemsIndexed(mainItems, key = { _, item -> item.id }) { index, item ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300, index * 40)) +
                                    slideInVertically(tween(300, index * 40)) { it / 6 }
                        ) {
                            when (item) {
                                is FeedItem.NewSkill       -> NewSkillCard(item = item)
                                is FeedItem.CompletedSwap  -> CompletedSwapCard(item = item)
                                is FeedItem.TopMentor      -> TopMentorCard(
                                    item = item,
                                    onConnect = { onMentorConnect(item.user.uid) }
                                )
                                is FeedItem.WishlistRequest -> WishlistCard(item = item)
                                is FeedItem.GroupSessionItem -> GroupSessionCard(
                                    session = item.session,
                                    currentUserId = feedState.items.hashCode().toString(),
                                    onClick = { navController.navigate("group_session/${item.session.sessionId}") }
                                )
                                else -> {}
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }

        if (showPostWishSheet && wishlistViewModel != null) {
            ModalBottomSheet(
                onDismissRequest = { showPostWishSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
                containerColor = Cream
            ) {
                PostWishSheet(
                    wishlistViewModel = wishlistViewModel,
                    onDismiss = { showPostWishSheet = false }
                )
            }
        }
    }
}

// =============================================================================
// State screens
// =============================================================================

@Composable
private fun FeedLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                color = NearBlack,
                strokeWidth = 2.dp,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Loading feed...",
                fontSize = 13.sp,
                color = CharcoalGray
            )
        }
    }
}

@Composable
private fun FeedErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "😕", fontSize = 40.sp)
            Text(
                text = "Couldn't load feed",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = NearBlack,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = message,
                fontSize = 13.sp,
                color = CharcoalGray
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(NearBlack, RoundedCornerShape(12.dp))
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 28.dp, vertical = 13.dp)
            ) {
                Text(
                    text = "Try again",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cream
                )
            }
        }
    }
}

@Composable
private fun FeedEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "🌱", fontSize = 40.sp)
            Text(
                text = "Feed is empty",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = NearBlack,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Be the first to add a skill\nand start trading!",
                fontSize = 13.sp,
                color = CharcoalGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// =============================================================================
// Trending strip
// =============================================================================

@Composable
private fun TrendingCategoriesStrip(categories: List<FeedItem.TrendingCategory>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "TRENDING THIS WEEK",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = WarmGray,
            letterSpacing = 1.5.sp
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(categories, key = { it.id }) { item ->
                TrendingCategoryChip(item = item)
            }
        }
    }
}

@Composable
private fun TrendingCategoryChip(item: FeedItem.TrendingCategory) {
    Column(
        modifier = Modifier
            .background(NearBlack, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = categoryEmoji(item.category), fontSize = 22.sp)
        Text(
            text = item.category.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = categoryColor(item.category),
            letterSpacing = 0.3.sp
        )
        Text(
            text = "${item.skillCount} skills",
            fontSize = 10.sp,
            color = WarmGray
        )
        Text(
            text = "${item.activeUserCount} people",
            fontSize = 10.sp,
            color = WarmGray
        )
    }
}

// =============================================================================
// New skill card
// =============================================================================

@Composable
private fun NewSkillCard(item: FeedItem.NewSkill) {
    val catColor = categoryColor(item.skill.category)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CreamDark, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(catColor)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InitialsAvatar(name = item.userName, size = 36.dp, textSize = 14.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = item.userName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NearBlack
                        )
                        Text(
                            text = "★ ${String.format("%.1f", item.userTrustScore)}",
                            fontSize = 11.sp,
                            color = Ochre
                        )
                    }
                }
                Text(
                    text = timeAgoString(item.timestamp),
                    fontSize = 11.sp,
                    color = WarmGray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(catColor, CircleShape)
                )
                Text(
                    text = "Added a new skill",
                    fontSize = 11.sp,
                    color = CharcoalGray
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NearBlack, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = item.skill.skillName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Cream,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "${item.skill.proficiencyLevel.name} · ${item.skill.category.name}",
                            fontSize = 11.sp,
                            color = WarmGray
                        )
                        if (item.skill.description.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.skill.description,
                                fontSize = 12.sp,
                                color = CharcoalGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(start = 10.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(catColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${item.skill.tokenValue}T",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NearBlack
                            )
                        }
                        if (item.skill.endorsements > 0) {
                            Text(
                                text = "★ ${item.skill.endorsements}",
                                fontSize = 10.sp,
                                color = Ochre
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// Completed swap card
// =============================================================================

@Composable
private fun CompletedSwapCard(item: FeedItem.CompletedSwap) {
    val stars = "★".repeat(item.rating.toInt()) + "☆".repeat(5 - item.rating.toInt())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CreamDark, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(text = "🤝", fontSize = 16.sp)
                    Text(
                        text = "Swap Completed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalGray
                    )
                }
                Text(
                    text = timeAgoString(item.timestamp),
                    fontSize = 11.sp,
                    color = WarmGray
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InitialsAvatar(name = item.mentorName, size = 48.dp, textSize = 18.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.skillName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = NearBlack,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "${item.mentorName} taught ${item.learnerName}",
                        fontSize = 12.sp,
                        color = CharcoalGray
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stars,
                        fontSize = 12.sp,
                        color = Ochre
                    )
                    val isToken = item.swapType == "TOKEN"
                    Box(
                        modifier = Modifier
                            .background(
                                if (isToken) NearBlack else AcidGreen.copy(alpha = 0.15f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.swapType,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isToken) AcidGreen else NearBlack,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Top mentor card
// =============================================================================

@Composable
private fun TopMentorCard(
    item: FeedItem.TopMentor,
    onConnect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NearBlack, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "⭐", fontSize = 12.sp)
                Text(
                    text = "TOP MENTOR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ochre,
                    letterSpacing = 1.5.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(AcidGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.user.name.take(1).uppercase(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = NearBlack
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            text = item.user.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Cream,
                            letterSpacing = (-0.5).sp
                        )
                        if (item.user.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(AcidGreen, CircleShape)
                            )
                        }
                    }
                    Text(
                        text = item.topSkillName,
                        fontSize = 13.sp,
                        color = WarmGray
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "★ ${String.format("%.1f", item.user.trustScore)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ochre
                        )
                        Text(
                            text = "${item.completedSwapCount} swaps",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcidGreen
                        )
                        Text(
                            text = "${item.user.skillTokenBalance}T",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WarmGray
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AcidGreen, RoundedCornerShape(12.dp))
                    .clickable(onClick = onConnect)
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Connect with ${item.user.name.split(" ").first()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NearBlack
                )
            }
        }
    }
}

// =============================================================================
// Wishlist card
// =============================================================================

@Composable
fun WishlistCard(item: FeedItem.WishlistRequest) {
    val wish = item.wish
    val catColor = categoryColor(wish.category)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CreamDark, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(text = "🎯", fontSize = 14.sp)
                    Text(
                        text = "SKILL WANTED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = catColor,
                        letterSpacing = 1.5.sp
                    )
                }
                Text(
                    text = timeAgoString(wish.createdAt),
                    fontSize = 11.sp,
                    color = WarmGray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InitialsAvatar(name = wish.userName, size = 48.dp, textSize = 18.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wish.skillName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NearBlack,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "${wish.userName} wants to learn this",
                        fontSize = 12.sp,
                        color = CharcoalGray
                    )
                }
                Box(
                    modifier = Modifier
                        .background(catColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${categoryEmoji(wish.category)} ${wish.category.name}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = catColor
                    )
                }
            }

            if (wish.description.isNotBlank()) {
                Text(
                    text = "\"${wish.description}\"",
                    fontSize = 12.sp,
                    color = CharcoalGray,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

// =============================================================================
// Post wish sheet
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostWishSheet(
    wishlistViewModel: WishlistViewModel,
    onDismiss: () -> Unit
) {
    val state by wishlistViewModel.uiState.collectAsState()
    var skillName   by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category    by remember { mutableStateOf(SkillCategory.DIGITAL) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(1200)
            wishlistViewModel.clearMessage()
            onDismiss()
        }
    }

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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Post a Wish 🎯",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NearBlack,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Tell mentors what you want to learn",
                    fontSize = 13.sp,
                    color = CharcoalGray
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = CharcoalGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        state.successMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AcidGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = msg,
                    fontSize = 13.sp,
                    color = NearBlack,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        state.error?.let { err ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ErrorContainerColor, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(text = err, fontSize = 13.sp, color = ErrorRed)
            }
        }

        OutlinedTextField(
            value = skillName,
            onValueChange = { skillName = it },
            placeholder = { Text("What skill do you want to learn?", color = WarmGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NearBlack,
                unfocusedBorderColor = WarmGray,
                focusedContainerColor = Cream,
                unfocusedContainerColor = Cream
            )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkillCategory.values().forEach { cat ->
                val selected = category == cat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (selected) NearBlack else CreamDark,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { category = cat }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat.name,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) AcidGreen else CharcoalGray
                    )
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Any details? (optional)", color = WarmGray) },
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
            onClick = {
                wishlistViewModel.postWish(skillName.trim(), category, description.trim())
            },
            enabled = !state.isPosting && skillName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AcidGreen,
                contentColor = NearBlack,
                disabledContainerColor = CreamDark,
                disabledContentColor = WarmGray
            )
        ) {
            if (state.isPosting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = NearBlack,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = "Post Wish",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

// =============================================================================
// Shared composable
// =============================================================================

@Composable
private fun InitialsAvatar(
    name: String,
    size: androidx.compose.ui.unit.Dp,
    textSize: androidx.compose.ui.unit.TextUnit
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(NearBlack, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            fontSize = textSize,
            fontWeight = FontWeight.Black,
            color = AcidGreen
        )
    }
}