package com.example.know_it_all.data.repository

import com.example.know_it_all.data.model.Skill
import com.example.know_it_all.data.model.SkillCategory
import com.example.know_it_all.data.repository.WishlistItem
import com.example.know_it_all.data.model.User
import com.example.know_it_all.data.model.dto.SwapDTO
import com.example.know_it_all.data.model.GroupSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

// ── Feed item types ───────────────────────────────────────────────────────────

sealed class FeedItem {
    abstract val id: String
    abstract val timestamp: Long

    data class NewSkill(
        override val id: String,
        override val timestamp: Long,
        val skill: Skill,
        val userName: String,
        val userTrustScore: Float
    ) : FeedItem()

    data class CompletedSwap(
        override val id: String,
        override val timestamp: Long,
        val skillName: String,
        val mentorName: String,
        val learnerName: String,
        val rating: Float,
        val swapType: String
    ) : FeedItem()

    data class TopMentor(
        override val id: String,
        override val timestamp: Long,
        val user: User,
        val topSkillName: String,
        val completedSwapCount: Int
    ) : FeedItem()

    data class WishlistRequest(
        override val id: String,
        override val timestamp: Long,
        val wish: WishlistItem
    ) : FeedItem()

    data class TrendingCategory(
        override val id: String,
        override val timestamp: Long,
        val category: SkillCategory,
        val skillCount: Int,
        val activeUserCount: Int
    ) : FeedItem()

    data class GroupSessionItem(
        override val id: String,
        override val timestamp: Long,
        val session: GroupSession
    ) : FeedItem()
}

// ── Repository ────────────────────────────────────────────────────────────────

class FeedRepository {

    private val db              = FirebaseFirestore.getInstance()
    private val skillsCol       = db.collection("skills")
    private val swapsCol        = db.collection("swaps")
    private val usersCol        = db.collection("users")

    /**
     * Fetches all feed items in parallel and merges into a single
     * time-sorted list. Returns at most [limit] items total.
     */
    suspend fun getFeedItems(
        currentUserId: String,
        limit: Int = 30
    ): Result<List<FeedItem>> = coroutineScope {
        try {
            val newSkillsDeferred       = async { fetchNewSkills(currentUserId) }
            val completedSwapsDeferred  = async { fetchCompletedSwaps(currentUserId) }
            val topMentorsDeferred      = async { fetchTopMentors(currentUserId) }
            val trendingDeferred        = async { fetchTrendingCategories() }
            val wishlistDeferred        = async { fetchWishlistItems(currentUserId) }
            val groupSessionsDeferred   = async { fetchGroupSessions(currentUserId) }

            val all = buildList {
                addAll(newSkillsDeferred.await())
                addAll(completedSwapsDeferred.await())
                addAll(topMentorsDeferred.await())
                addAll(trendingDeferred.await())
                addAll(wishlistDeferred.await())
                addAll(groupSessionsDeferred.await())
            }

            // Interleave types so the feed doesn't show all skills then all swaps
            val interleaved = interleave(all)
                .sortedByDescending { it.timestamp }
                .take(limit)

            Result.success(interleaved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── New skills ────────────────────────────────────────────────────────────

    private suspend fun fetchNewSkills(currentUserId: String): List<FeedItem.NewSkill> {
        return try {
            val snapshot = skillsCol
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                val userId = doc.getString("userId") ?: return@mapNotNull null
                if (userId == currentUserId) return@mapNotNull null

                val userDoc  = usersCol.document(userId).get().await()
                val userName = userDoc.getString("name") ?: "Unknown"
                val trust    = (userDoc.getDouble("trustScore") ?: 0.0).toFloat()

                val skill = Skill(
                    skillId          = doc.getString("skillId") ?: doc.id,
                    userId           = userId,
                    skillName        = doc.getString("skillName") ?: "",
                    description      = doc.getString("description") ?: "",
                    category         = runCatching {
                        SkillCategory.valueOf(doc.getString("category") ?: "DIGITAL")
                    }.getOrDefault(SkillCategory.DIGITAL),
                    proficiencyLevel = runCatching {
                        com.example.know_it_all.data.model.ProficiencyLevel.valueOf(
                            doc.getString("proficiencyLevel") ?: "BEGINNER"
                        )
                    }.getOrDefault(com.example.know_it_all.data.model.ProficiencyLevel.BEGINNER),
                    tokenValue       = doc.getLong("tokenValue")?.toInt() ?: 10,
                    endorsements     = doc.getLong("endorsements")?.toInt() ?: 0,
                    createdAt        = doc.getLong("createdAt") ?: 0L
                )

                FeedItem.NewSkill(
                    id            = "skill_${doc.id}",
                    timestamp     = skill.createdAt,
                    skill         = skill,
                    userName      = userName,
                    userTrustScore = trust
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    // ── Completed swaps ───────────────────────────────────────────────────────

    private suspend fun fetchCompletedSwaps(currentUserId: String): List<FeedItem.CompletedSwap> {
        return try {
            val snapshot = swapsCol
                .whereEqualTo("status", "COMPLETED")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(15)
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                val mentorId  = doc.getString("mentorId") ?: return@mapNotNull null
                val learnerId = doc.getString("learnerId") ?: return@mapNotNull null
                // Don't show current user's own swaps
                if (mentorId == currentUserId || learnerId == currentUserId)
                    return@mapNotNull null

                FeedItem.CompletedSwap(
                    id          = "swap_${doc.id}",
                    timestamp   = doc.getLong("updatedAt") ?: 0L,
                    skillName   = doc.getString("skillName") ?: "Skill",
                    mentorName  = doc.getString("mentorName") ?: "",
                    learnerName = doc.getString("learnerName") ?: "",
                    rating      = (doc.getDouble("rating") ?: 0.0).toFloat(),
                    swapType    = doc.getString("swapType") ?: "TOKEN"
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    // ── Top mentors ───────────────────────────────────────────────────────────

    private suspend fun fetchTopMentors(currentUserId: String): List<FeedItem.TopMentor> {
        return try {
            val snapshot = usersCol
                .orderBy("trustScore", Query.Direction.DESCENDING)
                .limit(10)
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                val uid = doc.getString("uid") ?: doc.id
                if (uid == currentUserId) return@mapNotNull null

                val user = User(
                    uid               = uid,
                    name              = doc.getString("name") ?: "",
                    email             = doc.getString("email") ?: "",
                    latitude          = doc.getDouble("latitude") ?: 0.0,
                    longitude         = doc.getDouble("longitude") ?: 0.0,
                    skillTokenBalance = doc.getLong("skillTokenBalance") ?: 0L,
                    trustScore        = (doc.getDouble("trustScore") ?: 0.0).toFloat(),
                    isOnline          = doc.getBoolean("isOnline") ?: false,
                    profileVerified   = doc.getBoolean("profileVerified") ?: false
                )

                // Get their top skill
                val skillSnap = skillsCol
                    .whereEqualTo("userId", uid)
                    .orderBy("endorsements", Query.Direction.DESCENDING)
                    .limit(1)
                    .get().await()
                val topSkill = skillSnap.documents.firstOrNull()
                    ?.getString("skillName") ?: "Various skills"

                // Count completed swaps as mentor
                val swapCount = swapsCol
                    .whereEqualTo("mentorId", uid)
                    .whereEqualTo("status", "COMPLETED")
                    .get().await().size()

                FeedItem.TopMentor(
                    id                = "mentor_$uid",
                    timestamp         = System.currentTimeMillis() - (swapCount * 1000L),
                    user              = user,
                    topSkillName      = topSkill,
                    completedSwapCount = swapCount
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    // ── Trending categories ───────────────────────────────────────────────────

    private suspend fun fetchTrendingCategories(): List<FeedItem.TrendingCategory> {
        return try {
            val snapshot = skillsCol.get().await()
            val categoryCounts = mutableMapOf<SkillCategory, Int>()
            val categoryUsers  = mutableMapOf<SkillCategory, MutableSet<String>>()

            snapshot.documents.forEach { doc ->
                val cat = runCatching {
                    SkillCategory.valueOf(doc.getString("category") ?: "DIGITAL")
                }.getOrDefault(SkillCategory.DIGITAL)
                val uid = doc.getString("userId") ?: ""

                categoryCounts[cat] = (categoryCounts[cat] ?: 0) + 1
                categoryUsers.getOrPut(cat) { mutableSetOf() }.add(uid)
            }

            SkillCategory.values().map { cat ->
                FeedItem.TrendingCategory(
                    id              = "trend_${cat.name}",
                    timestamp       = System.currentTimeMillis(),
                    category        = cat,
                    skillCount      = categoryCounts[cat] ?: 0,
                    activeUserCount = categoryUsers[cat]?.size ?: 0
                )
            }.sortedByDescending { it.skillCount }
        } catch (e: Exception) { emptyList() }
    }

    // ── Wishlist items ────────────────────────────────────────────────────────

    private suspend fun fetchWishlistItems(currentUserId: String): List<FeedItem.WishlistRequest> {
        return try {
            val snapshot = db.collection("wishlist")
                .whereEqualTo("isOpen", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                val userId = doc.getString("userId") ?: return@mapNotNull null
                if (userId == currentUserId) return@mapNotNull null

                val wish = WishlistItem(
                    wishId      = doc.getString("wishId") ?: doc.id,
                    userId      = userId,
                    userName    = doc.getString("userName") ?: "",
                    skillName   = doc.getString("skillName") ?: "",
                    category    = runCatching {
                        SkillCategory.valueOf(doc.getString("category") ?: "DIGITAL")
                    }.getOrDefault(SkillCategory.DIGITAL),
                    description = doc.getString("description") ?: "",
                    isOpen      = doc.getBoolean("isOpen") ?: true,
                    createdAt   = doc.getLong("createdAt") ?: 0L
                )

                FeedItem.WishlistRequest(
                    id        = "wish_${doc.id}",
                    timestamp = wish.createdAt,
                    wish      = wish
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    // ── Group sessions ─────────────────────────────────────────────────────────

    private suspend fun fetchGroupSessions(currentUserId: String): List<FeedItem.GroupSessionItem> {
        return try {
            val snapshot = db.collection("group_sessions")
                .whereEqualTo("status", "OPEN")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                val mentorId = doc.getString("mentorId") ?: return@mapNotNull null
                // Don't show current user's own sessions
                if (mentorId == currentUserId) return@mapNotNull null

                val session = GroupSession(
                    sessionId            = doc.id,
                    mentorId             = mentorId,
                    mentorName           = doc.getString("mentorName") ?: "",
                    skillName            = doc.getString("skillName") ?: "",
                    skillId              = doc.getString("skillId") ?: "",
                    description          = doc.getString("description") ?: "",
                    category             = runCatching {
                        SkillCategory.valueOf(doc.getString("category") ?: "DIGITAL")
                    }.getOrDefault(SkillCategory.DIGITAL),
                    maxLearners          = doc.getLong("maxLearners")?.toInt() ?: 5,
                    currentLearners      = doc.getLong("currentLearners")?.toInt() ?: 0,
                    durationMinutes      = doc.getLong("durationMinutes")?.toInt() ?: 60,
                    tokenPricePerLearner = doc.getLong("tokenPricePerLearner") ?: 10L,
                    scheduledAt          = doc.getLong("scheduledAt") ?: 0L,
                    isScheduled          = doc.getBoolean("isScheduled") ?: false,
                    status               = runCatching {
                        com.example.know_it_all.data.model.GroupSessionStatus.valueOf(
                            doc.getString("status") ?: "OPEN"
                        )
                    }.getOrDefault(com.example.know_it_all.data.model.GroupSessionStatus.OPEN),
                    jitsiRoomName        = doc.getString("jitsiRoomName") ?: "",
                    createdAt            = doc.getLong("createdAt") ?: 0L,
                    startedAt            = doc.getLong("startedAt"),
                    completedAt          = doc.getLong("completedAt")
                )

                FeedItem.GroupSessionItem(
                    id        = "group_session_${doc.id}",
                    timestamp = session.createdAt,
                    session   = session
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    // ── Interleave different item types ──────────────────────────────────────

    private fun interleave(items: List<FeedItem>): List<FeedItem> {
        val skills         = items.filterIsInstance<FeedItem.NewSkill>()
        val swaps          = items.filterIsInstance<FeedItem.CompletedSwap>()
        val mentors        = items.filterIsInstance<FeedItem.TopMentor>()
        val categories     = items.filterIsInstance<FeedItem.TrendingCategory>()
        val wishes         = items.filterIsInstance<FeedItem.WishlistRequest>()
        val groupSessions  = items.filterIsInstance<FeedItem.GroupSessionItem>()

        val result = mutableListOf<FeedItem>()
        val maxSize = maxOf(skills.size, swaps.size, mentors.size)

        for (i in 0 until maxSize) {
            if (i < skills.size)     result.add(skills[i])
            if (i < swaps.size)      result.add(swaps[i])
            if (i % 3 == 0 && i / 3 < mentors.size) result.add(mentors[i / 3])
            if (i % 4 == 0 && i / 4 < wishes.size)  result.add(wishes[i / 4])
            if (i % 5 == 0 && i / 5 < categories.size) result.add(categories[i / 5])
            if (i % 6 == 0 && i / 6 < groupSessions.size) result.add(groupSessions[i / 6])
        }

        return result
    }
}