package com.example.know_it_all.util

import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController

/**
 * Location: util/DeepLinkHandler.kt
 *
 * Handles incoming deep link intents and navigates to the correct screen.
 *
 * Supported schemes:
 *   knowitall://feed
 *   knowitall://vault
 *   knowitall://trade
 *   knowitall://trade/{swapId}
 *   knowitall://chat/{swapId}/{skillName}/{counterpartName}
 *   knowitall://group/{sessionId}
 *   knowitall://profile/{userId}
 *   knowitall://leaderboard
 */
object DeepLinkHandler {

    const val SCHEME = "knowitall"

    fun handle(intent: Intent?, navController: NavController): Boolean {
        val uri = intent?.data ?: return false
        if (uri.scheme != SCHEME) return false

        val route = resolveRoute(uri) ?: return false
        navController.navigate(route) {
            launchSingleTop = true
        }
        return true
    }

    private fun resolveRoute(uri: Uri): String? {
        val host = uri.host ?: return null
        val segments = uri.pathSegments // everything after the host

        return when (host) {
            "feed"        -> "feed"
            "vault"       -> "vault"
            "trade"       -> {
                val swapId = segments.getOrNull(0)
                if (swapId != null) "trade?swapId=$swapId"
                else "trade"
            }
            "chat"        -> {
                val swapId         = segments.getOrNull(0) ?: return null
                val skillName      = segments.getOrNull(1) ?: "Skill"
                val counterpart    = segments.getOrNull(2) ?: "User"
                "chat/$swapId/$skillName/$counterpart"
            }
            "group"       -> {
                val sessionId = segments.getOrNull(0) ?: return null
                "group_session/$sessionId"
            }
            "profile"     -> {
                // Navigate to radar — profile viewing from radar is handled there
                "radar"
            }
            "leaderboard" -> "leaderboard"
            "radar"       -> "radar"
            else          -> null
        }
    }

    // ── Build deep link URIs (use these when sharing) ─────────────────────────

    fun tradeLink(swapId: String) =
        Uri.parse("$SCHEME://trade/$swapId")

    fun chatLink(swapId: String, skillName: String, counterpartName: String) =
        Uri.parse("$SCHEME://chat/$swapId/$skillName/$counterpartName")

    fun groupSessionLink(sessionId: String) =
        Uri.parse("$SCHEME://group/$sessionId")

    fun profileLink(userId: String) =
        Uri.parse("$SCHEME://profile/$userId")

    fun vaultLink() = Uri.parse("$SCHEME://vault")
    fun feedLink()  = Uri.parse("$SCHEME://feed")
}