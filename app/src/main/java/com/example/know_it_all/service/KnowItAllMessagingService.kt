package com.example.know_it_all.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.know_it_all.MainActivity
import com.example.know_it_all.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Location: service/KnowItAllMessagingService.kt
 *
 * Handles two things:
 *  1. Displaying incoming FCM push notifications
 *  2. Saving the refreshed FCM token to Firestore whenever it changes
 */
class KnowItAllMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID   = "knowitall_notifications"
        const val CHANNEL_NAME = "KnowItAll Notifications"
    }

    // ── New token — save to Firestore so other users can reach this device ────

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
    }

    // ── Incoming message — show as system notification ────────────────────────

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "KnowItAll"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: return

        val deepLink = message.data["deepLink"] // e.g. "trade", "chat/swapId/..."

        showNotification(
            context  = this,
            title    = title,
            body     = body,
            deepLink = deepLink
        )
    }

    // ── Build and display the notification ────────────────────────────────────

    private fun showNotification(
        context: Context,
        title: String,
        body: String,
        deepLink: String?
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Create channel on Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Swap requests, messages and session alerts"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        // Intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            deepLink?.let { putExtra("deepLink", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // replace with your app icon
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}