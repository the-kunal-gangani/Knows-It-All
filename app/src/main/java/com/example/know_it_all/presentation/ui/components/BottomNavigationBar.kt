package com.example.know_it_all.presentation.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.WarmGray

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val items = listOf(
        BottomNavItem("Feed",    "feed",          Icons.Default.Explore),
        BottomNavItem("Radar",   "radar",         Icons.Default.LocationOn),
        BottomNavItem("Trade",   "trade",         Icons.Default.SwapHoriz),
        BottomNavItem("Vault",   "vault",         Icons.Default.Wallet),
        BottomNavItem("Profile", "skill_profile", Icons.Default.Person),
        BottomNavItem("Ranks", "leaderboard", Icons.Outlined.Leaderboard)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cream)
    ) {
        // Top border line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CreamDark)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                BottomNavTabItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                // Pop back to start so back stack doesn't grow
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavTabItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (isSelected) NearBlack else androidx.compose.ui.graphics.Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isSelected) AcidGreen else WarmGray,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) NearBlack else WarmGray
        )
    }
}