package com.example.know_it_all.data.repository

import com.example.know_it_all.data.model.LedgerStatus
import com.example.know_it_all.data.model.TrustLedger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class FirebaseLedgerRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ledgerCollection = db.collection("trust_ledger")
    private val usersCollection  = db.collection("users")

    // ── Real-time observation ─────────────────────────────────────────────────

    fun observeUserLedger(userId: String): Flow<List<TrustLedger>> = callbackFlow {
        val listener = ledgerCollection
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents
                    ?.mapNotNull { it.toTrustLedger() }
                    ?.filter { it.mentorId == userId || it.learnerId == userId }
                    ?: emptyList()
                trySend(entries)
            }
        awaitClose { listener.remove() }
    }




    

    fun getUserLedgerLocal(userId: String): Flow<List<TrustLedger>> = observeUserLedger(userId)

    suspend fun getUserLedger(
        userId: String,
        limit: Int = 50
    ): Result<List<TrustLedger>> {
        return try {
            val snapshot = ledgerCollection
                .orderBy("createdAt",
                    com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()

            val entries = snapshot.documents
                .mapNotNull { it.toTrustLedger() }
                .filter { it.mentorId == userId || it.learnerId == userId }

            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLatestEntry(): TrustLedger? {
        return try {
            val snapshot = ledgerCollection
                .orderBy("createdAt",
                    com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get().await()
            snapshot.documents.firstOrNull()?.toTrustLedger()
        } catch (e: Exception) { null }
    }

    // ── Trust score ───────────────────────────────────────────────────────────

    suspend fun getAverageRating(userId: String): Float? {
        return try {
            val snapshot = ledgerCollection
                .whereEqualTo("mentorId", userId)
                .whereEqualTo("status", "COMPLETED")
                .get().await()

            val ratings = snapshot.documents
                .mapNotNull { it.getLong("ratingGiven")?.toInt() }

            if (ratings.isEmpty()) null
            else ratings.average().toFloat()
        } catch (e: Exception) { null }
    }

    suspend fun getCompletedCount(userId: String): Int {
        return try {
            val snapshot = ledgerCollection
                .whereEqualTo("status", "COMPLETED")
                .get().await()
            snapshot.documents
                .mapNotNull { it.toTrustLedger() }
                .count { it.mentorId == userId || it.learnerId == userId }
        } catch (e: Exception) { 0 }
    }

    // ── Write ledger entry ────────────────────────────────────────────────────

    suspend fun createLedgerEntry(
        swapId: String,
        mentorId: String,
        learnerId: String,
        skillName: String,
        ratingGiven: Int,
        ratingComment: String = ""
    ): Result<TrustLedger> {
        return try {
            // Get previous hash for chain integrity
            val previousHash = getLatestEntry()?.currentHash ?: ""

            // Build hash input
            val timestamp       = System.currentTimeMillis()
            val transactionData = "$swapId$mentorId$learnerId$skillName$ratingGiven$timestamp"
            val currentHash     = sha256(transactionData + previousHash)

            val entry = TrustLedger(
                transactionId = UUID.randomUUID().toString(),
                swapId        = swapId,
                mentorId      = mentorId,
                learnerId     = learnerId,
                skillName     = skillName,
                previousHash  = previousHash,
                currentHash   = currentHash,
                ratingGiven   = ratingGiven,
                ratingComment = ratingComment,
                status        = LedgerStatus.COMPLETED,
                createdAt     = timestamp
            )

            ledgerCollection.document(entry.transactionId)
                .set(entry.toMap()).await()

            // Update mentor trust score
            val avgRating = getAverageRating(mentorId)
            if (avgRating != null) {
                usersCollection.document(mentorId)
                    .update("trustScore", avgRating).await()
            }

            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Dispute ───────────────────────────────────────────────────────────────

    suspend fun disputeTransaction(
        transactionId: String,
        reason: String
    ): Result<Unit> {
        return try {
            ledgerCollection.document(transactionId).update(
                mapOf(
                    "status"        to "DISPUTED",
                    "disputeReason" to reason
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Verify hash chain ─────────────────────────────────────────────────────

    suspend fun verifyTransaction(transactionId: String): Result<Boolean> {
        return try {
            val snapshot = ledgerCollection.document(transactionId).get().await()
            val entry    = snapshot.toTrustLedger()
                ?: throw Exception("Transaction not found")

            // Simple validity check — hash exists and is not empty
            val isValid = entry.currentHash.isNotEmpty()
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray())
            .fold("") { str, byte -> str + "%02x".format(byte) }
    }
}

// ── Extensions ────────────────────────────────────────────────────────────────

private fun com.google.firebase.firestore.DocumentSnapshot.toTrustLedger(): TrustLedger? {
    return try {
        TrustLedger(
            transactionId = getString("transactionId") ?: id,
            swapId        = getString("swapId") ?: "",
            mentorId      = getString("mentorId") ?: "",
            learnerId     = getString("learnerId") ?: "",
            skillName     = getString("skillName") ?: "",
            previousHash  = getString("previousHash") ?: "",
            currentHash   = getString("currentHash") ?: "",
            ratingGiven   = getLong("ratingGiven")?.toInt() ?: 0,
            ratingComment = getString("ratingComment") ?: "",
            status        = runCatching {
                LedgerStatus.valueOf(getString("status") ?: "COMPLETED")
            }.getOrDefault(LedgerStatus.COMPLETED),
            createdAt     = getLong("createdAt") ?: 0L
        )
    } catch (e: Exception) { null }
}

private fun TrustLedger.toMap(): Map<String, Any?> = mapOf(
    "transactionId" to transactionId,
    "swapId"        to swapId,
    "mentorId"      to mentorId,
    "learnerId"     to learnerId,
    "skillName"     to skillName,
    "previousHash"  to previousHash,
    "currentHash"   to currentHash,
    "ratingGiven"   to ratingGiven,
    "ratingComment" to ratingComment,
    "status"        to status.name,
    "createdAt"     to createdAt
)