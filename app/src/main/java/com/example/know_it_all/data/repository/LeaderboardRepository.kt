package com.example.know_it_all.data.repository

import com.example.know_it_all.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LeaderboardEntry(
    val rank: Int,
    val user: User,
    val topSkillName: String,
    val completedSwaps: Int,
    val averageRating: Float,
    val distanceKm: Double = 0.0
)

class LeaderboardRepository {

    private val db          = FirebaseFirestore.getInstance()
    private val usersCol    = db.collection("users")
    private val swapsCol    = db.collection("swaps")
    private val skillsCol   = db.collection("skills")

    // ── Global leaderboard — top 10 by trust score ────────────────────────────

    suspend fun getGlobalLeaderboard(
        currentUserId: String
    ): Result<List<LeaderboardEntry>> {
        return try {
            val snapshot = usersCol
                .orderBy("trustScore", Query.Direction.DESCENDING)
                .limit(10)
                .get().await()

            val entries = snapshot.documents.mapIndexedNotNull { index, doc ->
                val uid = doc.getString("uid") ?: doc.id
                val user = doc.toUser() ?: return@mapIndexedNotNull null

                val topSkill    = getTopSkill(uid)
                val swapCount   = getCompletedSwapCount(uid)
                val avgRating   = getAverageRating(uid)

                LeaderboardEntry(
                    rank           = index + 1,
                    user           = user,
                    topSkillName   = topSkill,
                    completedSwaps = swapCount,
                    averageRating  = avgRating
                )
            }
            Result.success(entries)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Nearby leaderboard — top 10 within 5km by trust score ────────────────

    suspend fun getNearbyLeaderboard(
        currentUserId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 5.0
    ): Result<List<LeaderboardEntry>> {
        return try {
            val latDelta = radiusKm / 111.0
            val snapshot = usersCol
                .whereGreaterThan("latitude", latitude - latDelta)
                .whereLessThan("latitude",  latitude + latDelta)
                .get().await()

            val lonDelta = radiusKm / 105.0
            val nearby = snapshot.documents.mapNotNull { doc ->
                val uid  = doc.getString("uid") ?: doc.id
                if (uid == currentUserId) return@mapNotNull null

                val user = doc.toUser() ?: return@mapNotNull null
                if (user.longitude < longitude - lonDelta ||
                    user.longitude > longitude + lonDelta) return@mapNotNull null

                val dist = distanceKm(latitude, longitude, user.latitude, user.longitude)
                if (dist > radiusKm) return@mapNotNull null

                Pair(user, dist)
            }
            .sortedByDescending { it.first.trustScore }
            .take(10)

            val entries = nearby.mapIndexed { index, (user, dist) ->
                val topSkill  = getTopSkill(user.uid)
                val swapCount = getCompletedSwapCount(user.uid)
                val avgRating = getAverageRating(user.uid)

                LeaderboardEntry(
                    rank           = index + 1,
                    user           = user,
                    topSkillName   = topSkill,
                    completedSwaps = swapCount,
                    averageRating  = avgRating,
                    distanceKm     = dist
                )
            }
            Result.success(entries)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Current user's rank ───────────────────────────────────────────────────

    suspend fun getCurrentUserRank(userId: String): Result<Int> {
        return try {
            val userDoc   = usersCol.document(userId).get().await()
            val userScore = userDoc.getDouble("trustScore") ?: 0.0
            val aboveCount = usersCol
                .whereGreaterThan("trustScore", userScore)
                .get().await().size()
            Result.success(aboveCount + 1)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private suspend fun getTopSkill(userId: String): String {
        return try {
            val snap = skillsCol
                .whereEqualTo("userId", userId)
                .orderBy("endorsements", Query.Direction.DESCENDING)
                .limit(1)
                .get().await()
            snap.documents.firstOrNull()?.getString("skillName") ?: "Various skills"
        } catch (e: Exception) { "Various skills" }
    }

    private suspend fun getCompletedSwapCount(userId: String): Int {
        return try {
            val snap = swapsCol
                .whereEqualTo("mentorId", userId)
                .whereEqualTo("status", "COMPLETED")
                .get().await()
            snap.size()
        } catch (e: Exception) { 0 }
    }

    private suspend fun getAverageRating(userId: String): Float {
        return try {
            val snap = swapsCol
                .whereEqualTo("mentorId", userId)
                .whereEqualTo("status", "COMPLETED")
                .get().await()
            val ratings = snap.documents.mapNotNull {
                it.getDouble("rating")?.toFloat()
            }
            if (ratings.isEmpty()) 0f else ratings.average().toFloat()
        } catch (e: Exception) { 0f }
    }

    private fun distanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r    = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a    = sin(dLat / 2) * sin(dLat / 2) +
                   cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                   sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
    return try {
        User(
            uid               = getString("uid") ?: id,
            name              = getString("name") ?: "",
            email             = getString("email") ?: "",
            latitude          = getDouble("latitude") ?: 0.0,
            longitude         = getDouble("longitude") ?: 0.0,
            skillTokenBalance = getLong("skillTokenBalance") ?: 0L,
            trustScore        = getDouble("trustScore")?.toFloat() ?: 0f,
            isOnline          = getBoolean("isOnline") ?: false,
            profileVerified   = getBoolean("profileVerified") ?: false
        )
    } catch (e: Exception) { null }
}