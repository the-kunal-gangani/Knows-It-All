package com.example.know_it_all.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Location: util/NotificationHelper.kt
 *
 * Sends FCM push notifications to other users by:
 *  1. Reading the target user's FCM token from Firestore
 *  2. Calling the FCM HTTP v1 API with the current user's Firebase Auth token
 *
 * This is the client-side approach — no Cloud Functions needed.
 * Works as long as at least one of the two users has the app open/background.
 */
object NotificationHelper {

    private val db  = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ── Notification types ────────────────────────────────────────────────────

    suspend fun notifySwapRequest(
        toUserId: String,
        fromUserName: String,
        skillName: String,
        swapId: String
    ) = sendNotification(
        toUserId  = toUserId,
        title     = "New swap request 🤝",
        body      = "$fromUserName wants to learn $skillName from you",
        deepLink  = "trade"
    )

    suspend fun notifySwapAccepted(
        toUserId: String,
        mentorName: String,
        skillName: String,
        swapId: String
    ) = sendNotification(
        toUserId  = toUserId,
        title     = "Swap accepted! ✅",
        body      = "$mentorName accepted your $skillName swap request",
        deepLink  = "trade"
    )

    suspend fun notifyNewMessage(
        toUserId: String,
        fromUserName: String,
        message: String,
        swapId: String,
        skillName: String
    ) = sendNotification(
        toUserId  = toUserId,
        title     = "$fromUserName sent a message 💬",
        body      = message.take(80),
        deepLink  = "chat/$swapId/$skillName/$fromUserName"
    )

    suspend fun notifySwapCancelled(
        toUserId: String,
        byUserName: String,
        skillName: String
    ) = sendNotification(
        toUserId  = toUserId,
        title     = "Swap cancelled",
        body      = "$byUserName cancelled the $skillName swap",
        deepLink  = "trade"
    )

    suspend fun notifyTokensReleased(
        toUserId: String,
        tokensAmount: Long,
        skillName: String
    ) = sendNotification(
        toUserId  = toUserId,
        title     = "Tokens released 💰",
        body      = "${tokensAmount}T released for your $skillName session",
        deepLink  = "vault"
    )

    suspend fun notifySessionComplete(
        toUserId: String,
        skillName: String,
        swapId: String
    ) = sendNotification(
        toUserId  = toUserId,
        title     = "Session marked complete ✅",
        body      = "Rate your $skillName session to release tokens",
        deepLink  = "trade"
    )

    suspend fun notifyWishlistMatch(
        toUserId: String,
        wisherName: String,
        skillName: String
    ) = sendNotification(
        toUserId = toUserId,
        title    = "Someone wants to learn $skillName! 🎯",
        body     = "$wisherName is looking for a mentor in $skillName — check the Feed",
        deepLink = "feed"
    )

    // ── Core send method ──────────────────────────────────────────────────────

    private suspend fun sendNotification(
        toUserId: String,
        title: String,
        body: String,
        deepLink: String
    ) = withContext(Dispatchers.IO) {
        try {
            // 1. Get target user's FCM token from Firestore
            val userDoc  = db.collection("users").document(toUserId).get().await()
            val fcmToken = userDoc.getString("fcmToken") ?: return@withContext

            // 2. Get current user's Firebase ID token for authentication
            val currentUser = auth.currentUser ?: return@withContext
            val idToken = currentUser.getIdToken(false).await().token ?: return@withContext

            // 3. Get the project ID from Firebase
            val projectId = getProjectId()

            // 4. Build the FCM message payload
            val message = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", fcmToken)
                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", body)
                    })
                    put("data", JSONObject().apply {
                        put("title", title)
                        put("body", body)
                        put("deepLink", deepLink)
                    })
                    put("android", JSONObject().apply {
                        put("priority", "high")
                        put("notification", JSONObject().apply {
                            put("sound", "default")
                            put("channel_id", "knowitall_notifications")
                        })
                    })
                })
            }

            // 5. Send via FCM HTTP v1 API
            val url = URL("https://fcm.googleapis.com/v1/projects/$projectId/messages:send")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $idToken")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            OutputStreamWriter(connection.outputStream).use {
                it.write(message.toString())
                it.flush()
            }

            val responseCode = connection.responseCode
            connection.disconnect()

            if (responseCode != 200) {
                // Silently fail — notifications are non-critical
                println("FCM send failed: $responseCode")
            }
        } catch (e: Exception) {
            // Never crash the app because a notification failed
            println("NotificationHelper error: ${e.message}")
        }
    }

    // ── Save current device token to Firestore ────────────────────────────────

    suspend fun saveDeviceToken(userId: String) = withContext(Dispatchers.IO) {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .token.await().let { token ->
                    db.collection("users").document(userId)
                        .update("fcmToken", token).await()
                }
        } catch (e: Exception) {
            println("Token save failed: ${e.message}")
        }
    }

    suspend fun notifyGroupSessionJoined(
        toUserId: String, learnerName: String,
        skillName: String, spotsLeft: Int
    ) = sendNotification(
        toUserId = toUserId,
        title    = "$learnerName joined your $skillName session 👥",
        body     = "$spotsLeft spot${if (spotsLeft == 1) "" else "s"} remaining",
        deepLink = "group_sessions"
    )

    suspend fun notifyGroupSessionStarted(
        toUserId: String, skillName: String,
        mentorName: String, sessionId: String
    ) = sendNotification(
        toUserId = toUserId,
        title    = "$skillName group session is starting! 🎓",
        body     = "$mentorName has started — join now",
        deepLink = "group_session/$sessionId"
    )

    suspend fun notifyGroupSessionComplete(
        toUserId: String, skillName: String, sessionId: String
    ) = sendNotification(
        toUserId = toUserId,
        title    = "$skillName session completed ✅",
        body     = "Rate your session to release tokens",
        deepLink = "group_session/$sessionId"
    )

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun getProjectId(): String {
        // Extract project ID from Firebase app options
        return com.google.firebase.FirebaseApp.getInstance().options.projectId ?: ""
    }
}