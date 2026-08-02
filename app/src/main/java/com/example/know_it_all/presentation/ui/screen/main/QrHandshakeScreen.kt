package com.example.know_it_all.presentation.ui.screen.main

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.know_it_all.presentation.viewmodel.TradeViewModel
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.Ochre
import com.example.know_it_all.ui.theme.WarmGray
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.security.MessageDigest

/**
 * Location: presentation/ui/screen/main/QRHandshakeScreen.kt
 *
 * Used for BARTER swaps to verify in-person session completion.
 *
 * Flow:
 *  1. Both users open this screen for their active swap
 *  2. Each user sees their own QR code + a "Scan Partner" button
 *  3. User A scans User B's QR → B's side verified
 *  4. User B scans User A's QR → A's side verified
 *  5. When both sides verified → session auto-marked complete
 *
 * QR payload: "swapId|userId|timestamp|hash"
 * The hash prevents tampering with the payload.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRHandshakeScreen(
    navController: NavHostController,
    tradeViewModel: TradeViewModel,
    swapId: String,
    currentUserId: String,
    skillName: String
) {
    val tradeState by tradeViewModel.uiState.collectAsState()
    var showScanner  by remember { mutableStateOf(false) }
    var scanResult   by remember { mutableStateOf<String?>(null) }
    var myQRBitmap   by remember { mutableStateOf<Bitmap?>(null) }

    // Generate QR on entry
    LaunchedEffect(swapId, currentUserId) {
        myQRBitmap = generateQRBitmap(swapId, currentUserId)
    }

    // Show success and go back once both sides verified
    LaunchedEffect(tradeState.successMessage) {
        if (tradeState.successMessage?.contains("Both verified") == true) {
            kotlinx.coroutines.delay(1500)
            navController.popBackStack()
        }
    }

    if (showScanner) {
        QRScannerView(
            onScanned = { payload ->
                showScanner = false
                scanResult = payload
                tradeViewModel.verifyQRScan(swapId, payload)
            },
            onDismiss = { showScanner = false }
        )
        return
    }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "QR HANDSHAKE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = WarmGray
                        )
                        Text(
                            skillName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = NearBlack
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NearBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Instructions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CreamDark, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "How it works",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NearBlack
                    )
                    StepRow("1", "Show your QR code to your partner")
                    StepRow("2", "Tap 'Scan Partner QR' to scan theirs")
                    StepRow("3", "When both scan → session verified ✅")
                }
            }

            // My QR code
            Text(
                "YOUR QR CODE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmGray,
                letterSpacing = 1.sp
            )

            myQRBitmap?.let { bmp ->
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(NearBlack, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Your QR Code",
                        modifier = Modifier.size(188.dp)
                    )
                }
            } ?: Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(CreamDark, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Generating QR...", color = WarmGray, fontSize = 13.sp)
            }

            // Status message
            tradeState.successMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (msg.contains("Both verified"))
                                AcidGreen.copy(alpha = 0.15f)
                            else
                                Ochre.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        msg,
                        fontSize = 13.sp,
                        color = if (msg.contains("Both verified")) NearBlack else Ochre,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            tradeState.error?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            com.example.know_it_all.ui.theme.ErrorContainerColor,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(err, fontSize = 13.sp,
                        color = com.example.know_it_all.ui.theme.ErrorRed)
                }
            }

            Spacer(Modifier.weight(1f))

            // Scan button
            Button(
                onClick = { showScanner = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(NearBlack, AcidGreen)
            ) {
                Text(
                    "📷  Scan Partner's QR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back", color = CharcoalGray)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── QR scanner placeholder ────────────────────────────────────────────────────
// Uses CameraX + ML Kit — Phase 2 wiring. For now shows manual entry fallback.

@Composable
private fun QRScannerView(
    onScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var manualInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📷", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Point camera at partner's QR code",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Cream,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Camera scanner coming in next update.\nAsk your partner to share their QR code text.",
            fontSize = 13.sp,
            color = WarmGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(32.dp))

        // Manual entry fallback
        androidx.compose.material3.OutlinedTextField(
            value = manualInput,
            onValueChange = { manualInput = it },
            placeholder = { Text("Paste QR payload here", color = WarmGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AcidGreen,
                unfocusedBorderColor = WarmGray,
                focusedTextColor = Cream,
                unfocusedTextColor = Cream,
                focusedContainerColor = NearBlack,
                unfocusedContainerColor = NearBlack
            )
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { if (manualInput.isNotBlank()) onScanned(manualInput.trim()) },
            enabled = manualInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(AcidGreen, NearBlack)
        ) {
            Text("Verify", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Cancel", color = WarmGray)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun StepRow(number: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(NearBlack, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(number, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AcidGreen)
        }
        Text(text, fontSize = 13.sp, color = CharcoalGray, lineHeight = 19.sp)
    }
}

/**
 * Generates a signed QR bitmap.
 * Payload: "swapId|userId|timestamp|hash"
 */
fun generateQRBitmap(swapId: String, userId: String, size: Int = 512): Bitmap? {
    return try {
        val timestamp = System.currentTimeMillis()
        val raw       = "$swapId|$userId|$timestamp"
        val hash      = sha256(raw).take(16)
        val payload   = "$raw|$hash"

        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN          to 1,
            EncodeHintType.CHARACTER_SET   to "UTF-8"
        )
        val writer    = QRCodeWriter()
        val bitMatrix = writer.encode(payload, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap    = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) { null }
}

private fun sha256(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(input.toByteArray())
        .fold("") { str, byte -> str + "%02x".format(byte) }
}