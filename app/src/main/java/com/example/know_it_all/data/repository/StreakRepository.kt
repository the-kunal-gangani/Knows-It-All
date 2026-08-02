package com.example.know_it_all.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Location: data/repository/StreakRepository.kt
 *
 * Streak rules:
 *  - Any activity (swap completed, message sent, skill added) counts
 *  - Activity must happen at least once per 7-day window
 *  - Missing a 7-day window resets streak to 0
 *
 * Milestones:
 *  -   7 days =  5 bonus tokens
 *  -  30 days = 20 bonus tokens
 *  - 100 days = 50 bonus tokens
 */

data class StreakData(
    val userId: String = "",
    val currentStreak: Int = 0,       // days
    val longestStreak: Int = 0,
    val lastActivityAt: Long = 0L,
    val totalActivities: Int = 0,
    val claimedMilestones: List<Int> = emptyList() // milestone days already claimed
) {
    val nextMilestone: Int?
        get() = MILESTONES.firstOrNull { it > currentStreak && it !in claimedMilestones }

    val progressToNextMilestone: Float
        get() {
            val next = nextMilestone ?: return 1f
            val prev = MILESTONES.lastOrNull { it < next } ?: 0
            return if (next == prev) 1f
            else (currentStreak - prev).toFloat() / (next - prev).toFloat()
        }

    val isActiveToday: Boolean
        get() {
            val now = System.currentTimeMillis()
            return now - lastActivityAt < TimeUnit.DAYS.toMillis(1)
        }

    val daysUntilReset: Int
        get() {
            val elapsed = System.currentTimeMillis() - lastActivityAt
            val remaining = TimeUnit.DAYS.toMillis(7) - elapsed
            return if (remaining <= 0) 0
            else TimeUnit.MILLISECONDS.toDays(remaining).toInt() + 1
        }

    companion object {
        val MILESTONES = listOf(7, 30, 100)

        fun milestoneTokens(days: Int): Int = when (days) {
            7    ->  5
            30   -> 20
            100  -> 50
            else -> 0
        }

        fun milestoneLabel(days: Int): String = when (days) {
            7    -> "7-Day Streak 🔥"
            30   -> "30-Day Streak 💪"
            100  -> "100-Day Legend 👑"
            else -> "$days-Day Streak"
        }
    }
}

class StreakRepository {

    private val db         = FirebaseFirestore.getInstance()
    private val streaksCol = db.collection("streaks")
    private val usersCol   = db.collection("users")

    // ── Get streak data ───────────────────────────────────────────────────────

    suspend fun getStreak(userId: String): Result<StreakData> {
        return try {
            val doc = streaksCol.document(userId).get().await()
            if (!doc.exists()) {
                // First time — create streak document
                val newStreak = StreakData(userId = userId)
                streaksCol.document(userId).set(newStreak.toMap()).await()
                Result.success(newStreak)
            } else {
                Result.success(doc.toStreakData() ?: StreakData(userId = userId))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Record activity — call this whenever user does something ──────────────

    suspend fun recordActivity(userId: String): Result<StreakData> {
        return try {
            val doc = streaksCol.document(userId).get().await()
            val current = doc.toStreakData() ?: StreakData(userId = userId)
            val now     = System.currentTimeMillis()

            val newStreak = calculateNewStreak(current, now)

            streaksCol.document(userId).set(newStreak.toMap()).await()
            Result.success(newStreak)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Claim milestone reward ─────────────────────────────────────────────────

    suspend fun claimMilestone(userId: String, milestoneDays: Int): Result<Int> {
        return try {
            val doc     = streaksCol.document(userId).get().await()
            val current = doc.toStreakData() ?: return Result.failure(Exception("Streak not found"))

            // Validate
            if (current.currentStreak < milestoneDays) {
                return Result.failure(Exception("Streak not reached yet"))
            }
            if (milestoneDays in current.claimedMilestones) {
                return Result.failure(Exception("Milestone already claimed"))
            }

            val bonusTokens = StreakData.milestoneTokens(milestoneDays)

            db.runTransaction { transaction ->
                val streakRef = streaksCol.document(userId)
                val userRef   = usersCol.document(userId)

                val userSnap  = transaction.get(userRef)
                val balance   = userSnap.getLong("skillTokenBalance") ?: 0L

                // Add bonus tokens to user balance
                transaction.update(userRef, "skillTokenBalance", balance + bonusTokens)

                // Mark milestone as claimed
                val updatedMilestones = current.claimedMilestones + milestoneDays
                transaction.update(streakRef, "claimedMilestones", updatedMilestones)
            }.await()

            Result.success(bonusTokens)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun calculateNewStreak(current: StreakData, now: Long): StreakData {
        val lastActivity = current.lastActivityAt
        val daysSinceLast = TimeUnit.MILLISECONDS.toDays(now - lastActivity)

        val newStreakCount = when {
            lastActivity == 0L  -> 1  // first activity ever
            daysSinceLast == 0L -> current.currentStreak  // same day, no change
            daysSinceLast <= 7  -> current.currentStreak + daysSinceLast.toInt() // within window
            else                -> 1  // missed window — reset
        }

        return current.copy(
            currentStreak  = newStreakCount,
            longestStreak  = maxOf(current.longestStreak, newStreakCount),
            lastActivityAt = now,
            totalActivities = current.totalActivities + 1
        )
    }
}

// ── Extensions ────────────────────────────────────────────────────────────────

private fun com.google.firebase.firestore.DocumentSnapshot.toStreakData(): StreakData? {
    return try {
        @Suppress("UNCHECKED_CAST")
        StreakData(
            userId           = getString("userId") ?: id,
            currentStreak    = getLong("currentStreak")?.toInt() ?: 0,
            longestStreak    = getLong("longestStreak")?.toInt() ?: 0,
            lastActivityAt   = getLong("lastActivityAt") ?: 0L,
            totalActivities  = getLong("totalActivities")?.toInt() ?: 0,
            claimedMilestones = (get("claimedMilestones") as? List<Long>)
                ?.map { it.toInt() } ?: emptyList()
        )
    } catch (e: Exception) { null }
}

private fun StreakData.toMap(): Map<String, Any?> = mapOf(
    "userId"            to userId,
    "currentStreak"     to currentStreak,
    "longestStreak"     to longestStreak,
    "lastActivityAt"    to lastActivityAt,
    "totalActivities"   to totalActivities,
    "claimedMilestones" to claimedMilestones
)