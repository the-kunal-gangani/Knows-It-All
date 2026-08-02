package com.example.know_it_all.data.repository

import com.example.know_it_all.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

// ── Message model ─────────────────────────────────────────────────────────────

data class ChatMessage(
    val messageId: String = "",
    val swapId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

// ── Repository ────────────────────────────────────────────────────────────────

class FirebaseChatRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun messagesRef(swapId: String) =
        db.collection("chats").document(swapId).collection("messages")

    /**
     * Observe messages in real-time for a swap.
     * Messages are ordered by timestamp ascending.
     */
    fun observeMessages(swapId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = messagesRef(swapId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        ChatMessage(
                            messageId = doc.getString("messageId") ?: doc.id,
                            swapId    = swapId,
                            senderId  = doc.getString("senderId") ?: "",
                            text      = doc.getString("text") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Send a message to the swap chat.
     */
    suspend fun sendMessage(
        swapId: String,
        senderId: String,
        text: String
    ): Result<Unit> {
        return try {
            val messageId = UUID.randomUUID().toString()
            val message = mapOf(
                "messageId" to messageId,
                "senderId"  to senderId,
                "text"      to text,
                "timestamp" to System.currentTimeMillis()
            )
            messagesRef(swapId).document(messageId).set(message).await()

            // Record streak activity
            StreakRepository().recordActivity(senderId)

            // Notify counterpart of new message
            try {
                val swapDoc    = db.collection("swaps").document(swapId).get().await()
                val mentorId   = swapDoc.getString("mentorId") ?: ""
                val learnerId  = swapDoc.getString("learnerId") ?: ""
                val skillName  = swapDoc.getString("skillName") ?: ""
                val toUserId   = if (senderId == mentorId) learnerId else mentorId
                val senderDoc  = db.collection("users").document(senderId).get().await()
                val senderName = senderDoc.getString("name") ?: "Someone"
                NotificationHelper.notifyNewMessage(
                    toUserId     = toUserId,
                    fromUserName = senderName,
                    message      = text,
                    swapId       = swapId,
                    skillName    = skillName
                )
            } catch (e: Exception) {
                // Notification failure should not fail the message send
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}