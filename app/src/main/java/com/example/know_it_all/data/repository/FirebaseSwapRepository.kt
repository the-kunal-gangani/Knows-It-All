package com.example.know_it_all.data.repository

import com.example.know_it_all.data.model.SessionConfig
import com.example.know_it_all.data.model.SwapStatus
import com.example.know_it_all.data.model.SwapType
import com.example.know_it_all.data.model.VerificationMethod
import com.example.know_it_all.data.model.dto.SwapDTO
import com.example.know_it_all.data.model.dto.SwapRequestBody
import com.example.know_it_all.util.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseSwapRepository {

    private val db              = FirebaseFirestore.getInstance()
    private val swapsCollection = db.collection("swaps")
    private val usersCollection = db.collection("users")
    private val skillsCollection = db.collection("skills")
    private val escrowCollection = db.collection("token_escrow")

    // ── Real-time streams ─────────────────────────────────────────────────────

    fun observeActiveSwaps(userId: String): Flow<List<SwapDTO>> = callbackFlow {
        val listener = swapsCollection
            .whereIn("status", listOf("REQUESTED", "ACTIVE"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val swaps = snapshot?.documents
                    ?.mapNotNull { it.toSwapDTO() }
                    ?.filter { it.mentorId == userId || it.learnerId == userId }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                trySend(swaps)
            }
        awaitClose { listener.remove() }
    }

    fun observePendingCount(userId: String): Flow<Int> = callbackFlow {
        val listener = swapsCollection
            .whereEqualTo("learnerId", userId)
            .whereEqualTo("status", "REQUESTED")
            .addSnapshotListener { snapshot, _ -> trySend(snapshot?.size() ?: 0) }
        awaitClose { listener.remove() }
    }

    // ── One-shot reads ────────────────────────────────────────────────────────

    suspend fun getActiveSwaps(userId: String): Result<List<SwapDTO>> {
        return try {
            val snapshot = swapsCollection
                .whereIn("status", listOf("REQUESTED", "ACTIVE"))
                .get().await()
            Result.success(
                snapshot.documents.mapNotNull { it.toSwapDTO() }
                    .filter { it.mentorId == userId || it.learnerId == userId }
                    .sortedByDescending { it.createdAt }
            )
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getSwapHistory(userId: String, limit: Int = 10): Result<List<SwapDTO>> {
        return try {
            val snapshot = swapsCollection
                .whereEqualTo("status", "COMPLETED")
                .limit(limit.toLong())
                .get().await()
            Result.success(
                snapshot.documents.mapNotNull { it.toSwapDTO() }
                    .filter { it.mentorId == userId || it.learnerId == userId }
                    .sortedByDescending { it.createdAt }
            )
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getSwapById(swapId: String): Result<SwapDTO> {
        return try {
            val snap = swapsCollection.document(swapId).get().await()
            Result.success(snap.toSwapDTO() ?: throw Exception("Swap not found"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Request swap ──────────────────────────────────────────────────────────

    suspend fun requestSwap(request: SwapRequestBody): Result<SwapDTO> {
        return try {
            val mentorDoc  = usersCollection.document(request.mentorId).get().await()
            val learnerDoc = usersCollection.document(request.learnerId).get().await()
            val skillDoc   = skillsCollection.document(request.mentorSkillId).get().await()

            val mentorName  = mentorDoc.getString("name") ?: ""
            val learnerName = learnerDoc.getString("name") ?: ""
            val skillName   = skillDoc.getString("skillName") ?: ""

            val swapId = UUID.randomUUID().toString()
            val now    = System.currentTimeMillis()

            // Verification method determined by swap type
            val verMethod = when (request.swapType) {
                SwapType.TOKEN, SwapType.HYBRID -> "VIDEO_CALL"
                SwapType.BARTER                -> "QR_SCAN"
            }

            swapsCollection.document(swapId).set(mapOf(
                "swapId"              to swapId,
                "mentorId"            to request.mentorId,
                "learnerId"           to request.learnerId,
                "mentorName"          to mentorName,
                "learnerName"         to learnerName,
                "mentorSkillId"       to request.mentorSkillId,
                "learnerSkillId"      to request.learnerSkillId,
                "swapType"            to request.swapType.name,
                "tokenAmount"         to request.tokenAmount,
                "status"              to "REQUESTED",
                "verificationMethod"  to verMethod,
                "skillName"           to skillName,
                "sessionStartTime"    to null,
                "sessionEndTime"      to null,
                "createdAt"           to now,
                "updatedAt"           to now,
                "totalSessions"       to request.totalSessions,
                "durationMinutes"     to request.durationMinutes,
                "completedSessions"   to 0,
                "proofDescription"    to null,
                "proofDurationMinutes" to null,
                "tokensInEscrow"      to 0L,
                "tokensToMentor"      to 0L,
                "tokensHeld"          to 0L
            )).await()

            // Notify mentor of new swap request
            NotificationHelper.notifySwapRequest(
                toUserId     = request.mentorId,
                fromUserName = learnerName,
                skillName    = skillName,
                swapId       = swapId
            )

            Result.success(SwapDTO(
                swapId = swapId,
                mentorId = request.mentorId, learnerId = request.learnerId,
                mentorName = mentorName, learnerName = learnerName,
                skillName = skillName,
                mentorSkillId = request.mentorSkillId,
                learnerSkillId = request.learnerSkillId,
                swapType = request.swapType,
                tokenAmount = request.tokenAmount,
                status = SwapStatus.REQUESTED,
                verificationMethod = VerificationMethod.NONE,
                createdAt = now,
                totalSessions = request.totalSessions,
                durationMinutes = request.durationMinutes,
                completedSessions = 0
            ))
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Accept swap — deduct tokens from learner into escrow ──────────────────

    suspend fun acceptSwap(swapId: String): Result<SwapDTO> {
        return try {
            val swapDoc     = swapsCollection.document(swapId).get().await()
            val learnerId   = swapDoc.getString("learnerId") ?: ""
            val tokenAmount = swapDoc.getLong("tokenAmount") ?: 0L

            db.runTransaction { transaction ->
                val swapRef    = swapsCollection.document(swapId)
                val learnerRef = usersCollection.document(learnerId)

                if (tokenAmount > 0) {
                    val learnerSnap = transaction.get(learnerRef)
                    val balance     = learnerSnap.getLong("skillTokenBalance") ?: 0L
                    if (balance < tokenAmount) throw Exception("Insufficient tokens")

                    // Deduct tokens from learner
                    transaction.update(learnerRef, "skillTokenBalance",
                        balance - tokenAmount)

                    // Store escrow record
                    val escrowRef = escrowCollection.document(swapId)
                    transaction.set(escrowRef, mapOf(
                        "swapId"        to swapId,
                        "learnerId"     to learnerId,
                        "totalTokens"   to tokenAmount,
                        "status"        to "LOCKED",
                        "createdAt"     to System.currentTimeMillis(),
                        // Auto-release after 7 days if no dispute
                        "autoReleaseAt" to System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
                    ))
                }

                transaction.update(swapRef, mapOf(
                    "status"         to "ACTIVE",
                    "tokensInEscrow" to tokenAmount,
                    "updatedAt"      to System.currentTimeMillis()
                ))
            }.await()

            // Notify learner that swap was accepted
            val mentorName = swapDoc.getString("mentorName") ?: ""
            val skillName  = swapDoc.getString("skillName") ?: ""
            NotificationHelper.notifySwapAccepted(
                toUserId   = learnerId,
                mentorName = mentorName,
                skillName  = skillName,
                swapId     = swapId
            )

            getSwapById(swapId)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Record one completed session ──────────────────────────────────────────

    suspend fun completeSession(swapId: String): Result<SwapDTO> {
        return try {
            db.runTransaction { transaction ->
                val ref      = swapsCollection.document(swapId)
                val snap     = transaction.get(ref)
                val done     = (snap.getLong("completedSessions") ?: 0L).toInt()
                val total    = (snap.getLong("totalSessions") ?: 1L).toInt()
                val newCount = (done + 1).coerceAtMost(total)
                transaction.update(ref, mapOf(
                    "completedSessions" to newCount,
                    "updatedAt"         to System.currentTimeMillis()
                ))
            }.await()
            getSwapById(swapId)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Submit proof (text + duration) ────────────────────────────────────────

    suspend fun submitProof(
        swapId: String,
        description: String,
        durationMinutes: Int
    ): Result<Unit> {
        return try {
            swapsCollection.document(swapId).update(mapOf(
                "proofDescription"     to description,
                "proofDurationMinutes" to durationMinutes,
                "updatedAt"            to System.currentTimeMillis()
            )).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── QR verification — both users scan each other ──────────────────────────

    /**
     * Called when user scans the counterpart's QR code.
     * Payload format: "swapId|userId|timestamp|hash"
     * Both users must scan each other — tracked via mentorQrVerified + learnerQrVerified.
     * When both are true, session is automatically marked complete.
     */
    suspend fun verifyQRScan(
        swapId: String,
        scannedPayload: String,
        scannerUserId: String
    ): Result<QRVerifyResult> {
        return try {
            val parts = scannedPayload.split("|")
            if (parts.size < 3) return Result.success(QRVerifyResult.INVALID)

            val payloadSwapId = parts[0]
            if (payloadSwapId != swapId) return Result.success(QRVerifyResult.WRONG_SWAP)

            val swapDoc  = swapsCollection.document(swapId).get().await()
            val mentorId = swapDoc.getString("mentorId") ?: ""

            // Mark the scanner's side as verified
            val field = if (scannerUserId == mentorId) "mentorQrVerified" else "learnerQrVerified"

            swapsCollection.document(swapId).update(mapOf(
                field       to true,
                "updatedAt" to System.currentTimeMillis()
            )).await()

            // Check if both sides are now verified
            val updated       = swapsCollection.document(swapId).get().await()
            val mentorDone    = updated.getBoolean("mentorQrVerified") ?: false
            val learnerDone   = updated.getBoolean("learnerQrVerified") ?: false

            if (mentorDone && learnerDone) {
                // Both scanned — complete the session
                completeSession(swapId)
                Result.success(QRVerifyResult.BOTH_VERIFIED)
            } else {
                Result.success(QRVerifyResult.ONE_SIDE_DONE)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    enum class QRVerifyResult {
        BOTH_VERIFIED,   // both scanned — session marked complete
        ONE_SIDE_DONE,   // this user scanned, waiting for counterpart
        WRONG_SWAP,      // QR is for a different swap
        INVALID          // malformed QR payload
    }

    // ── Final complete with rating-based token release ────────────────────────

    /**
     * Called after all sessions complete and rating submitted.
     *
     * Token release:
     *   5★ = 100% to mentor
     *   4★ =  75% to mentor, 25% held in escrow
     *   3★ =  50% to mentor, 50% held in escrow
     *  <3★ =  25% to mentor, 75% held in escrow
     *
     * Held tokens remain in escrow for 7 days then auto-release to mentor
     * unless a dispute is raised within that window.
     */
    suspend fun completeSwapWithRating(
        swapId: String,
        rating: Float,
        comment: String = ""
    ): Result<SwapDTO> {
        return try {
            val swapDoc     = swapsCollection.document(swapId).get().await()
            val mentorId    = swapDoc.getString("mentorId") ?: ""
            val learnerId   = swapDoc.getString("learnerId") ?: ""
            val tokensInEscrow = swapDoc.getLong("tokensInEscrow") ?: 0L

            val toMentor = SessionConfig.tokensToMentor(tokensInEscrow, rating)
            val toHold   = SessionConfig.tokensToEscrow(tokensInEscrow, rating)

            db.runTransaction { transaction ->
                val swapRef    = swapsCollection.document(swapId)
                val mentorRef  = usersCollection.document(mentorId)
                val escrowRef  = escrowCollection.document(swapId)

                // Release mentor's share immediately
                if (toMentor > 0) {
                    val mentorSnap = transaction.get(mentorRef)
                    val mentorBal  = mentorSnap.getLong("skillTokenBalance") ?: 0L
                    transaction.update(mentorRef, "skillTokenBalance", mentorBal + toMentor)
                }

                // Update escrow record with held amount
                if (toHold > 0) {
                    transaction.update(escrowRef, mapOf(
                        "status"        to "PARTIALLY_HELD",
                        "heldAmount"    to toHold,
                        "rating"        to rating,
                        // Auto-release held tokens after 7 days
                        "autoReleaseAt" to System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
                    ))
                } else {
                    // Full release — close escrow
                    transaction.update(escrowRef, "status", "RELEASED")
                }

                transaction.update(swapRef, mapOf(
                    "status"        to "COMPLETED",
                    "rating"        to rating,
                    "ratingComment" to comment,
                    "tokensToMentor" to toMentor,
                    "tokensHeld"    to toHold,
                    "updatedAt"     to System.currentTimeMillis()
                ))
            }.await()

            // Record streak activity for both mentor and learner
            StreakRepository().recordActivity(mentorId)
            StreakRepository().recordActivity(learnerId)

            getSwapById(swapId)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Cancel — return escrowed tokens to learner ────────────────────────────

    suspend fun cancelSwap(swapId: String): Result<Unit> {
        return try {
            val swapDoc  = swapsCollection.document(swapId).get().await()
            val learnerId = swapDoc.getString("learnerId") ?: ""
            val escrow   = swapDoc.getLong("tokensInEscrow") ?: 0L

            db.runTransaction { transaction ->
                val swapRef = swapsCollection.document(swapId)

                if (escrow > 0) {
                    val learnerRef  = usersCollection.document(learnerId)
                    val learnerSnap = transaction.get(learnerRef)
                    val bal = learnerSnap.getLong("skillTokenBalance") ?: 0L
                    // Return all escrowed tokens to learner on cancel
                    transaction.update(learnerRef, "skillTokenBalance", bal + escrow)
                    transaction.update(escrowCollection.document(swapId), "status", "REFUNDED")
                }

                transaction.update(swapRef, mapOf(
                    "status"         to "CANCELLED",
                    "tokensInEscrow" to 0L,
                    "updatedAt"      to System.currentTimeMillis()
                ))
            }.await()

            // Notify both users of cancellation
            val cancelDoc    = swapsCollection.document(swapId).get().await()
            val mentorId     = cancelDoc.getString("mentorId") ?: ""
            val cancelSkill  = cancelDoc.getString("skillName") ?: ""
            val currentUid   = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val currentName  = usersCollection.document(currentUid).get().await().getString("name") ?: ""
            val notifyUserId = if (currentUid == mentorId) learnerId else mentorId
            NotificationHelper.notifySwapCancelled(
                toUserId   = notifyUserId,
                byUserName = currentName,
                skillName  = cancelSkill
            )

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Convenience wrapper used by TradeViewModel.rateSwap
    suspend fun rateSwap(swapId: String, rating: Int, comment: String = ""): Result<Unit> =
        completeSwapWithRating(swapId, rating.toFloat(), comment).map { }
}

// ── Firestore document → SwapDTO ──────────────────────────────────────────────

private fun com.google.firebase.firestore.DocumentSnapshot.toSwapDTO(): SwapDTO? {
    return try {
        SwapDTO(
            swapId             = getString("swapId") ?: id,
            mentorId           = getString("mentorId") ?: "",
            learnerId          = getString("learnerId") ?: "",
            mentorName         = getString("mentorName") ?: "",
            learnerName        = getString("learnerName") ?: "",
            skillName          = getString("skillName") ?: "",
            mentorSkillId      = getString("mentorSkillId") ?: "",
            learnerSkillId     = getString("learnerSkillId"),
            swapType           = runCatching {
                SwapType.valueOf(getString("swapType") ?: "TOKEN")
            }.getOrDefault(SwapType.TOKEN),
            tokenAmount        = getLong("tokenAmount") ?: 0L,
            status             = runCatching {
                SwapStatus.valueOf(getString("status") ?: "REQUESTED")
            }.getOrDefault(SwapStatus.REQUESTED),
            verificationMethod = runCatching {
                VerificationMethod.valueOf(getString("verificationMethod") ?: "NONE")
            }.getOrDefault(VerificationMethod.NONE),
            sessionStartTime   = getLong("sessionStartTime"),
            sessionEndTime     = getLong("sessionEndTime"),
            createdAt          = getLong("createdAt"),
            totalSessions      = getLong("totalSessions")?.toInt() ?: 1,
            durationMinutes    = getLong("durationMinutes")?.toInt() ?: 60,
            completedSessions  = getLong("completedSessions")?.toInt() ?: 0,
            proofDescription   = getString("proofDescription"),
            proofDurationMinutes = getLong("proofDurationMinutes")?.toInt(),
            tokensInEscrow     = getLong("tokensInEscrow") ?: 0L,
            tokensToMentor     = getLong("tokensToMentor") ?: 0L,
            tokensHeld         = getLong("tokensHeld") ?: 0L
        )
    } catch (e: Exception) { null }
}