package com.example.know_it_all.data.repository

import com.example.know_it_all.data.model.SkillCategory
import com.example.know_it_all.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

// ── Model ─────────────────────────────────────────────────────────────────────

data class WishlistItem(
    val wishId: String = "",
    val userId: String = "",
    val userName: String = "",
    val skillName: String = "",
    val category: SkillCategory = SkillCategory.DIGITAL,
    val description: String = "",
    val isOpen: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

// ── Repository ────────────────────────────────────────────────────────────────

/**
 * Location: data/repository/WishlistRepository.kt
 *
 * Firestore collection: wishlist/{wishId}
 *
 * When a new wish is posted:
 *  1. Saved to Firestore
 *  2. All nearby users who teach that skill are notified via FCM
 */
class WishlistRepository {

    private val db           = FirebaseFirestore.getInstance()
    private val wishlistCol  = db.collection("wishlist")
    private val usersCol     = db.collection("users")
    private val skillsCol    = db.collection("skills")

    // ── Real-time stream ──────────────────────────────────────────────────────

    fun observeOpenWishes(): Flow<List<WishlistItem>> = callbackFlow {
        val listener = wishlistCol
            .whereEqualTo("isOpen", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val items = snapshot?.documents?.mapNotNull { it.toWishlistItem() }
                    ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun observeUserWishes(userId: String): Flow<List<WishlistItem>> = callbackFlow {
        val listener = wishlistCol
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val items = snapshot?.documents?.mapNotNull { it.toWishlistItem() }
                    ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    // ── Post a new wish ───────────────────────────────────────────────────────

    suspend fun postWish(
        userId: String,
        skillName: String,
        category: SkillCategory,
        description: String
    ): Result<WishlistItem> {
        return try {
            val userDoc  = usersCol.document(userId).get().await()
            val userName = userDoc.getString("name") ?: "Someone"

            val wishId = UUID.randomUUID().toString()
            val item   = WishlistItem(
                wishId      = wishId,
                userId      = userId,
                userName    = userName,
                skillName   = skillName,
                category    = category,
                description = description,
                isOpen      = true,
                createdAt   = System.currentTimeMillis()
            )

            wishlistCol.document(wishId).set(item.toMap()).await()

            // Notify mentors who teach this skill
            notifyMatchingMentors(userId, userName, skillName, category)

            Result.success(item)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Close a wish (fulfilled) ──────────────────────────────────────────────

    suspend fun closeWish(wishId: String): Result<Unit> {
        return try {
            wishlistCol.document(wishId).update("isOpen", false).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Delete a wish ─────────────────────────────────────────────────────────

    suspend fun deleteWish(wishId: String): Result<Unit> {
        return try {
            wishlistCol.document(wishId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Get open wishes for Feed ──────────────────────────────────────────────

    suspend fun getOpenWishes(excludeUserId: String): Result<List<WishlistItem>> {
        return try {
            val snapshot = wishlistCol
                .whereEqualTo("isOpen", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get().await()
            val items = snapshot.documents
                .mapNotNull { it.toWishlistItem() }
                .filter { it.userId != excludeUserId }
            Result.success(items)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Notify mentors who teach the wished skill ─────────────────────────────

    private suspend fun notifyMatchingMentors(
        wisherId: String,
        wisherName: String,
        skillName: String,
        category: SkillCategory
    ) {
        try {
            // Find skills matching the name (case-insensitive client-side)
            val snapshot = skillsCol
                .whereEqualTo("category", category.name)
                .get().await()

            val matchingUserIds = snapshot.documents
                .filter { doc ->
                    val docSkill = doc.getString("skillName") ?: ""
                    docSkill.contains(skillName, ignoreCase = true) ||
                    skillName.contains(docSkill, ignoreCase = true)
                }
                .mapNotNull { it.getString("userId") }
                .filter { it != wisherId }
                .distinct()
                .take(10) // limit to avoid too many notifications

            matchingUserIds.forEach { mentorId ->
                NotificationHelper.notifyWishlistMatch(
                    toUserId   = mentorId,
                    wisherName = wisherName,
                    skillName  = skillName
                )
            }
        } catch (e: Exception) {
            // Notification failure is non-critical
        }
    }
}

// ── Extensions ────────────────────────────────────────────────────────────────

private fun com.google.firebase.firestore.DocumentSnapshot.toWishlistItem(): WishlistItem? {
    return try {
        WishlistItem(
            wishId      = getString("wishId") ?: id,
            userId      = getString("userId") ?: "",
            userName    = getString("userName") ?: "",
            skillName   = getString("skillName") ?: "",
            category    = runCatching {
                SkillCategory.valueOf(getString("category") ?: "DIGITAL")
            }.getOrDefault(SkillCategory.DIGITAL),
            description = getString("description") ?: "",
            isOpen      = getBoolean("isOpen") ?: true,
            createdAt   = getLong("createdAt") ?: 0L
        )
    } catch (e: Exception) { null }
}

private fun WishlistItem.toMap(): Map<String, Any?> = mapOf(
    "wishId"      to wishId,
    "userId"      to userId,
    "userName"    to userName,
    "skillName"   to skillName,
    "category"    to category.name,
    "description" to description,
    "isOpen"      to isOpen,
    "createdAt"   to createdAt
)