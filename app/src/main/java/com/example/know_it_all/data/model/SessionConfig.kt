package com.example.know_it_all.data.model

/**
 * Tracks session count, duration, completion, and token release rules.
 * Location: data/model/SessionConfig.kt
 */
data class SessionConfig(
    val totalSessions: Int = 1,
    val durationMinutes: Int = 60,
    val completedSessions: Int = 0
) {
    val isFullyComplete: Boolean
        get() = completedSessions >= totalSessions

    val remainingSessions: Int
        get() = (totalSessions - completedSessions).coerceAtLeast(0)

    val progressPercent: Float
        get() = if (totalSessions == 0) 0f
                else (completedSessions.toFloat() / totalSessions).coerceIn(0f, 1f)

    companion object {
        /**
         * 5★ (≥4.5) = 100% to mentor,  0% held
         * 4★ (≥3.5) =  75% to mentor, 25% held in escrow
         * 3★ (≥2.5) =  50% to mentor, 50% held in escrow
         * <3★        =  25% to mentor, 75% held in escrow
         *
         * Held tokens auto-release after 7 days if no dispute raised.
         */
        fun releasePercent(rating: Float): Double = when {
            rating >= 4.5f -> 1.0
            rating >= 3.5f -> 0.75
            rating >= 2.5f -> 0.5
            else           -> 0.25
        }

        fun tokensToMentor(totalTokens: Long, rating: Float): Long =
            (totalTokens * releasePercent(rating)).toLong()

        fun tokensToEscrow(totalTokens: Long, rating: Float): Long =
            totalTokens - tokensToMentor(totalTokens, rating)

        fun ratingLabel(rating: Float): String = when {
            rating >= 4.5f -> "Excellent — 100% tokens released to mentor"
            rating >= 3.5f -> "Good — 75% released, 25% held in escrow"
            rating >= 2.5f -> "Average — 50% released, 50% held in escrow"
            else           -> "Poor — 25% released, 75% held in escrow"
        }
    }
}