package com.example.know_it_all.data.model.dto

import com.example.know_it_all.data.model.SwapStatus
import com.example.know_it_all.data.model.SwapType
import com.example.know_it_all.data.model.VerificationMethod

data class SwapDTO(
    val swapId: String,
    val mentorId: String,
    val learnerId: String,
    val mentorName: String = "",
    val learnerName: String = "",
    val skillName: String = "",
    val mentorSkillId: String,
    val learnerSkillId: String? = null,
    val swapType: SwapType,
    val tokenAmount: Long = 0L,
    val status: SwapStatus = SwapStatus.REQUESTED,
    val verificationMethod: VerificationMethod = VerificationMethod.NONE,
    val sessionStartTime: Long? = null,
    val sessionEndTime: Long? = null,
    val createdAt: Long? = null,

    // ── Phase 1 additions ─────────────────────────────────────────────────────

    // Session config — set when swap is requested
    val totalSessions: Int = 1,
    val durationMinutes: Int = 60,
    val completedSessions: Int = 0,

    // Proof — submitted before final completion
    val proofDescription: String? = null,
    val proofDurationMinutes: Int? = null,

    // Escrow — tokens locked when swap is accepted
    val tokensInEscrow: Long = 0L,
    val tokensToMentor: Long = 0L,   // filled after rating
    val tokensHeld: Long = 0L        // remaining in escrow pending dispute/auto-release
) {
    // Computed helpers used by UI
    val sessionProgress: String get() = "$completedSessions / $totalSessions sessions"
    val isSessionComplete: Boolean get() = completedSessions >= totalSessions
    val hasProof: Boolean get() = !proofDescription.isNullOrBlank()

    // Whether this swap requires chat + video (TOKEN or HYBRID)
    val requiresOnlineVerification: Boolean
        get() = swapType == SwapType.TOKEN || swapType == SwapType.HYBRID

    // Whether this swap requires QR handshake (BARTER)
    val requiresQRVerification: Boolean
        get() = swapType == SwapType.BARTER
}

data class SwapRequestBody(
    val mentorId: String,
    val learnerId: String,
    val mentorSkillId: String,
    val learnerSkillId: String? = null,
    val swapType: SwapType,
    val tokenAmount: Long = 0L,
    // Session config chosen by learner when requesting
    val totalSessions: Int = 1,
    val durationMinutes: Int = 60
)

data class SwapRatingRequest(
    val swapId: String,
    val rating: Int,        // 1–5
    val comment: String = ""
)

data class SwapStatusUpdateRequest(
    val swapId: String,
    val status: SwapStatus
)

data class ProofSubmission(
    val swapId: String,
    val description: String,
    val durationMinutes: Int
)