package com.example.know_it_all.presentation.ui.screen.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.know_it_all.presentation.ui.navigation.Screen
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.WarmGray
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavHostController,
    isLoggedIn: Boolean
) {
    var contentVisible by remember { mutableStateOf(false) }
    var statsVisible by remember { mutableStateOf(false) }
    var pillsVisible by remember { mutableStateOf(false) }

    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "content_alpha"
    )
    val contentOffset by animateFloatAsState(
        targetValue = if (contentVisible) 0f else 20f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "content_offset"
    )
    val statsAlpha by animateFloatAsState(
        targetValue = if (statsVisible) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "stats_alpha"
    )
    val statsOffset by animateFloatAsState(
        targetValue = if (statsVisible) 0f else 16f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "stats_offset"
    )
    val pillsAlpha by animateFloatAsState(
        targetValue = if (pillsVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "pills_alpha"
    )

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        delay(120)
        contentVisible = true
        delay(350)
        pillsVisible = true
        delay(200)
        statsVisible = true
        delay(1800)
        if (isLoggedIn) {
            val prefs = context.getSharedPreferences("KnowItAllPrefs", 0)
            val onboardingDone = prefs.getBoolean("onboarding_complete", false)
            if (onboardingDone) {
                navController.navigate(Screen.Feed.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            } else {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        BackgroundRings()

        CornerTag(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 28.dp)
                .alpha(pillsAlpha)
        )

        FloatingPill(
            label = "5km radius",
            isDark = true,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 20.dp, y = (-120).dp)
                .alpha(pillsAlpha)
        )

        FloatingPill(
            label = "P2P Skills",
            isDark = false,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-16).dp, y = 80.dp)
                .alpha(pillsAlpha)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .alpha(contentAlpha)
                    .offset(y = contentOffset.dp)
            ) {
                LogoMark()
            }

            Spacer(modifier = Modifier.height(52.dp))

            Text(
                text = "KnowItAll",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = NearBlack.copy(alpha = contentAlpha),
                letterSpacing = (-3).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = contentOffset.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Bridging the Knowledge Gap,\nOne Trade at a Time.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = CharcoalGray.copy(alpha = contentAlpha),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.offset(y = contentOffset.dp)
            )

            Spacer(modifier = Modifier.height(52.dp))

            StatsBar(
                modifier = Modifier
                    .alpha(statsAlpha)
                    .offset(y = statsOffset.dp)
            )

            Spacer(modifier = Modifier.height(52.dp))

            SequentialDots(
                modifier = Modifier.alpha(statsAlpha)
            )
        }
    }
}

@Composable
private fun BackgroundRings() {
    val infiniteTransition = rememberInfiniteTransition(label = "rings")
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1_rotation"
    )
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2_rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(380.dp)
                .offset(x = 120.dp, y = (-80).dp)
                .rotate(rotation1)
                .border(
                    width = 1.dp,
                    color = NearBlack.copy(alpha = 0.06f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-80).dp, y = 480.dp)
                .rotate(rotation2)
                .border(
                    width = 1.dp,
                    color = NearBlack.copy(alpha = 0.06f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 200.dp, y = 440.dp)
                .rotate(rotation1)
                .border(
                    width = 1.dp,
                    color = AcidGreen.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun LogoMark() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")

    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring1_alpha"
    )
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring1_scale"
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring2_alpha",
    )
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(172.dp)
                .scale(ring1Scale)
                .border(
                    width = 1.5.dp,
                    color = AcidGreen.copy(alpha = ring2Alpha),
                    shape = RoundedCornerShape(52.dp)
                )
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(ring1Scale)
                .border(
                    width = 1.5.dp,
                    color = AcidGreen.copy(alpha = ring1Alpha),
                    shape = RoundedCornerShape(40.dp)
                )
        )

        Box(
            modifier = Modifier
                .size(108.dp)
                .background(NearBlack, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "K",
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = AcidGreen,
                letterSpacing = (-4).sp
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .scale(dotScale)
                    .size(13.dp)
                    .background(AcidGreen, CircleShape)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 14.dp)
                .background(AcidGreen, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(NearBlack.copy(alpha = 0.4f), CircleShape)
                )
                Text(
                    text = "SKILL SWAP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NearBlack,
                    letterSpacing = 0.8.sp
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(NearBlack.copy(alpha = 0.4f), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun StatsBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(NearBlack, RoundedCornerShape(20.dp)),
        horizontalArrangement = Arrangement.Center
    ) {
        StatItem(value = "3T", label = "Swap types")
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(48.dp)
                .background(Color.White.copy(alpha = 0.08f))
        )
        StatItem(value = "5km", label = "Radius")
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(48.dp)
                .background(Color.White.copy(alpha = 0.08f))
        )
        StatItem(value = "0₹", label = "Cost")
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = AcidGreen,
            letterSpacing = (-1).sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SequentialDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "d1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "d2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "d3"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(if (dot1 > 0.6f) 1f else 0.85f)
                .background(AcidGreen.copy(alpha = dot1), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(if (dot2 > 0.6f) 1f else 0.85f)
                .background(AcidGreen.copy(alpha = dot2), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(if (dot3 > 0.6f) 1f else 0.85f)
                .background(AcidGreen.copy(alpha = dot3), CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "LOADING",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = WarmGray,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun CornerTag(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(NearBlack.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "v1.0",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = NearBlack,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
private fun FloatingPill(
    label: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isDark) NearBlack else AcidGreen
    val textColor = if (isDark) Cream else NearBlack
    val dotColor = if (isDark) AcidGreen else NearBlack.copy(alpha = 0.4f)

    Row(
        modifier = modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(dotColor, CircleShape)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            letterSpacing = 0.3.sp
        )
    }
}