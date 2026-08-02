package com.example.know_it_all.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Blockchain-inspired immutable record of every completed skill swap.
 * Each entry hashes its own content + the previous entry's hash,
 * forming a tamper-evident chain.
 *
 * Fixes applied:
 *  1. ratingGiven changed Float → Int. Ratings are 1–5 stars (discrete).
 *     Float allowed invalid values like 3.7. If half-stars are needed later,
 *     add a separate halfStar: Boolean field rather than abusing Float precision.
 *  2. transactionData (raw JSON String) removed. The fields it serialized are
 *     now explicit typed columns: mentorId, learnerId, swapId, skillName.
 *     Storing JSON blobs in Room breaks type safety, prevents SQL queries
 *     against the content, and hides schema inside schema.
 *  3. Added @ForeignKey to Swap — a ledger entry must reference a real swap.
 *  4. Added @Index on swapId and mentorId/learnerId for Vault screen queries.
 *  5. Added ledgerStatus enum replacing the raw String — removes invalid states.
 *  6. mentorId and learnerId added as explicit columns (previously buried in
 *     transactionData JSON) so the Vault screen can filter by participant
 *     without deserializing anything.
 */
@Entity(
    tableName = "trust_ledger",
    foreignKeys = [
        ForeignKey(
            entity = Swap::class,
            parentColumns = ["swapId"],
            childColumns = ["swapId"],
            onDelete = ForeignKey.RESTRICT   // never silently delete a ledger entry
        )
    ],
    indices = [
        Index(value = ["swapId"]),
        Index(value = ["mentorId"]),
        Index(value = ["learnerId"])
    ]
)
data class TrustLedger(
    @PrimaryKey
    val transactionId: String,
    val swapId: String,

    // Participants stored explicitly — no longer buried in JSON
    val mentorId: String,
    val learnerId: String,
    val skillName: String,              // denormalized snapshot for Vault display

    // Chain integrity
    val previousHash: String = "",
    val currentHash: String = "",

    // Rating — discrete 1–5, not Float
    val ratingGiven: Int = 0,           // ✅ Int, not Float
    val ratingComment: String = "",

    val status: LedgerStatus = LedgerStatus.COMPLETED,
    val createdAt: Long = System.currentTimeMillis()
)

enum class LedgerStatus {
    COMPLETED,
    DISPUTED,
    RESOLVED
}