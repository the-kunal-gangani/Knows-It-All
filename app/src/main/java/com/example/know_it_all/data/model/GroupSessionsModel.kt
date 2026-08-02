package com.example.know_it_all.data.model

/**
 * Location: data/model/GroupSessionModels.kt
 *
 * Group session flow:
 *  1. Mentor creates a GroupSession (status = OPEN)
 *  2. It appears in the Feed
 *  3. Learners tap "Join" → GroupEnrollment created, tokens locked in escrow
 *  4. Session fills up OR mentor starts it manually → status = ACTIVE
 *  5. Jitsi Meet room opened (same room for all)
 *  6. Mentor marks complete
 *  7. Each learner rates independently
 *  8. Tokens released to mentor based on each learner's rating
 */

enum class GroupSessionStatus {
    OPEN,       // accepting learners
    ACTIVE,     // in progress
    COMPLETED,  // finished
    CANCELLED   // cancelled by mentor
}

enum class EnrollmentStatus {
    ENROLLED,   // joined, tokens in escrow
    COMPLETED,  // session done, rated
    REFUNDED,   // cancelled, tokens returned
    NO_SHOW     // learner didn't attend
}

data class GroupSession(
    val sessionId: String = "",
    val mentorId: String = "",
    val mentorName: String = "",
    val skillName: String = "",
    val skillId: String = "",
    val description: String = "",
    val category: SkillCategory = SkillCategory.DIGITAL,

    // Session config
    val maxLearners: Int = 5,
    val currentLearners: Int = 0,
    val durationMinutes: Int = 60,
    val tokenPricePerLearner: Long = 10L,  // each learner pays this amount

    // Schedule
    val scheduledAt: Long = 0L,    // epoch millis, 0 = TBD
    val isScheduled: Boolean = false,

    // Status
    val status: GroupSessionStatus = GroupSessionStatus.OPEN,
    val jitsiRoomName: String = "",  // auto-generated from sessionId

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null
) {
    val isFull: Boolean get() = currentLearners >= maxLearners
    val spotsLeft: Int get() = maxLearners - currentLearners
    val totalEarnings: Long get() = tokenPricePerLearner * currentLearners

    val displaySchedule: String
        get() = if (!isScheduled) "Flexible — TBD via chat"
                else {
                    val sdf = java.text.SimpleDateFormat("dd MMM · h:mm a",
                        java.util.Locale.getDefault())
                    sdf.format(java.util.Date(scheduledAt))
                }
}

data class GroupEnrollment(
    val enrollmentId: String = "",
    val sessionId: String = "",
    val learnerId: String = "",
    val learnerName: String = "",
    val tokensLocked: Long = 0L,
    val status: EnrollmentStatus = EnrollmentStatus.ENROLLED,
    val rating: Float? = null,
    val ratingComment: String = "",
    val enrolledAt: Long = System.currentTimeMillis()
)

// ── Map helpers ───────────────────────────────────────────────────────────────

fun GroupSession.toMap(): Map<String, Any?> = mapOf(
    "sessionId"            to sessionId,
    "mentorId"             to mentorId,
    "mentorName"           to mentorName,
    "skillName"            to skillName,
    "skillId"              to skillId,
    "description"          to description,
    "category"             to category.name,
    "maxLearners"          to maxLearners,
    "currentLearners"      to currentLearners,
    "durationMinutes"      to durationMinutes,
    "tokenPricePerLearner" to tokenPricePerLearner,
    "scheduledAt"          to scheduledAt,
    "isScheduled"          to isScheduled,
    "status"               to status.name,
    "jitsiRoomName"        to jitsiRoomName,
    "createdAt"            to createdAt,
    "startedAt"            to startedAt,
    "completedAt"          to completedAt
)

fun GroupEnrollment.toMap(): Map<String, Any?> = mapOf(
    "enrollmentId"  to enrollmentId,
    "sessionId"     to sessionId,
    "learnerId"     to learnerId,
    "learnerName"   to learnerName,
    "tokensLocked"  to tokensLocked,
    "status"        to status.name,
    "rating"        to rating,
    "ratingComment" to ratingComment,
    "enrolledAt"    to enrolledAt
)