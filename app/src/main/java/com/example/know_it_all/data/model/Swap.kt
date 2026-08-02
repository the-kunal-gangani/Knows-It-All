package com.example.know_it_all.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cleaned version for KnowsItAll:
 *  1. Foreign Keys removed to simplify sync with Spring Boot backend.
 *  2. Indices kept for performance on the Trade/Vault screens.
 *  3. mentorSkillId + learnerSkillId are now Strings to match Skill.skillId.
 */
@Entity(
    tableName = "swaps",
    indices = [
        Index(value = ["mentorId"]),
        Index(value = ["learnerId"]),
        Index(value = ["status"]),
        Index(value = ["mentorSkillId"])
    ]
)
data class Swap(
    @PrimaryKey
    val swapId: String,
    val mentorId: String,
    val learnerId: String,
    val mentorSkillId: String,               // Matches Skill.skillId UUID
    val learnerSkillId: String? = null,      // Null for TOKEN-only swaps
    val swapType: SwapType,
    val tokenAmount: Long = 0L,
    val status: SwapStatus = SwapStatus.REQUESTED,
    val verificationMethod: VerificationMethod = VerificationMethod.NONE,
    val sessionStartTime: Long? = null,
    val sessionEndTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis() 
)

enum class SwapStatus {
    REQUESTED,
    ACTIVE,
    COMPLETED,
    CANCELLED,
    DISPUTED
}

enum class SwapType {
    BARTER, // Skill for Skill
    TOKEN,  // Skill for Tokens
    HYBRID  // Skill + Tokens
}

enum class VerificationMethod {
    NONE,
    QR_HANDSHAKE,
    VIDEO_CALL,
    BOTH
}