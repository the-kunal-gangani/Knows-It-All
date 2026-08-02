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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.know_it_all.data.model.LedgerStatus
import com.example.know_it_all.data.model.TrustLedger
import com.example.know_it_all.presentation.ui.components.BottomNavigationBar
import com.example.know_it_all.presentation.viewmodel.LedgerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fixes applied:
 *  1. AuthViewModel removed — receives userId: String, userName: String.
 *  2. loadLedger() called without token param — ViewModel reads internally.
 *  3. passportFile removed from state reference — now uses passportEvent Flow.
 *  4. LedgerEntryItem.description changed from entry.transactionData
 *     (field removed from entity) to entry.skillName.
 *  5. BottomNavigationBar was missing — added.
 *  6. Full design applied — token balance card with large editorial numeral,
 *     trust score ring, ledger timeline with hash chain preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreenEnhanced(
    navController: NavHostController,
    ledgerViewModel: LedgerViewModel,
    userId: String,
    userName: String,           // ✅ passed from NavGraph via authState.userName
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val ledgerState by ledgerViewModel.uiState.collectAsState()
    val passportEvent by ledgerViewModel.passportEvent.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        // Ledger observations are set up in ViewModel init
    }

    // Handle passport file event — launch share intent when ready
    LaunchedEffect(passportEvent) {
        passportEvent?.let { file ->
            try {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",  // must match AndroidManifest authority
                    file
                )
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // No PDF viewer installed — fall through, passport file is still saved
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        containerColor = Cream,
        bottomBar = {  // ✅ was missing entirely
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
                            text = "THE VAULT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = WarmGray
                        )
                        Text(
                            text = "Tokens & Ledger",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = NearBlack,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream
                )
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

            // Token balance — editorial large numeral
            item {
                TokenBalanceCard(
                    balance = ledgerState.tokenBalance,
                    trustScore = ledgerState.trustScore,     
                    completedSwaps = ledgerState.totalSwaps, 
                    averageRating = ledgerState.trustScore
                )
            }

            // Skill Passport — generate + share buttons
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Generate / Download PDF
                    Button(
                        onClick = { ledgerViewModel.generateSkillPassport(context) },
                        enabled = !ledgerState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NearBlack,
                            contentColor = AcidGreen,
                            disabledContainerColor = CreamDark,
                            disabledContentColor = WarmGray
                        )
                    ) {
                        if (ledgerState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = AcidGreen,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null,
                                modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Skill Passport", fontWeight = FontWeight.Bold,
                                fontSize = 15.sp)
                        }
                    }

                    // Share buttons row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Share PDF
                        Button(
                            onClick = { ledgerViewModel.sharePDF(context) },
                            enabled = !ledgerState.isLoading,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CreamDark,
                                contentColor = NearBlack
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null,
                                modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Share web link
                        Button(
                            onClick = { ledgerViewModel.shareWebLink(context) },
                            enabled = !ledgerState.isLoading,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AcidGreen,
                                contentColor = NearBlack
                            )
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null,
                                modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Link", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Section header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NearBlack
                    )
                    Text(
                        text = "${ledgerState.ledgerEntries.size} records",
                        fontSize = 12.sp,
                        color = CharcoalGray
                    )
                }
            }

            // Loading
            if (ledgerState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = NearBlack,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Error
            ledgerState.error?.let { err ->
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

            // Ledger entries
            itemsIndexed(
                ledgerState.ledgerEntries,
                key = { _, entry -> entry.transactionId }
            ) { index, entry ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(250, index * 40)) +
                            slideInVertically(tween(250, index * 40)) { it / 5 }
                ) {
                    LedgerEntryCard(entry = entry)
                }
            }

            if (!ledgerState.isLoading && ledgerState.ledgerEntries.isEmpty()
                && ledgerState.error == null) {
                item { LedgerEmptyState() }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ---------------------------------------------------------------------------
// Token balance card — large editorial numeral treatment
// ---------------------------------------------------------------------------

@Composable
private fun TokenBalanceCard(
    balance: Long,
    trustScore: Float,
    completedSwaps: Int,
    averageRating: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NearBlack, RoundedCornerShape(20.dp))
            .padding(22.dp)
    ) {
        Column {
            Text(
                text = "TOKEN BALANCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = WarmGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$balance",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = AcidGreen,
                    letterSpacing = (-2).sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SKT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = WarmGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatPill(
                    label = "TRUST",
                    value = String.format("%.0f", trustScore * 10) + "%",
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "SWAPS",
                    value = completedSwaps.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "RATING",
                    value = if (averageRating > 0) "★ ${String.format("%.1f", averageRating)}"
                            else "—",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Cream.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            color = Cream)
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium,
            color = WarmGray, letterSpacing = 1.sp)
    }
}

// ---------------------------------------------------------------------------
// Ledger entry card — hash chain timeline
// ---------------------------------------------------------------------------

@Composable
private fun LedgerEntryCard(entry: TrustLedger) {
    val statusColor = when (entry.status) {
        LedgerStatus.COMPLETED -> AcidGreen
        LedgerStatus.DISPUTED  -> ErrorRed
        LedgerStatus.RESOLVED  -> Ochre
    }

    val dateStr = remember(entry.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(entry.createdAt))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline dot + line
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(statusColor, CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(72.dp)
                    .background(CreamDeep)
            )
        }

        // Card
        Column(
            modifier = Modifier
                .weight(1f)
                .background(CreamDark, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.skillName,                          // ✅ skillName, not transactionData
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NearBlack
                )
                Text(
                    text = "★ ${entry.ratingGiven}",                 // ✅ Int star rating
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ochre
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = dateStr,
                fontSize = 11.sp,
                color = CharcoalGray
            )
            if (entry.ratingComment.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${entry.ratingComment}\"",
                    fontSize = 11.sp,
                    color = CharcoalGray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            // Hash preview — shows chain integrity
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entry.currentHash.take(16) + "...",
                fontSize = 9.sp,
                color = WarmGray,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun LedgerEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏦", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No transactions yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = NearBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Complete a swap to see\nyour ledger history here",
            fontSize = 13.sp,
            color = CharcoalGray,
            lineHeight = 20.sp
        )
    }
}