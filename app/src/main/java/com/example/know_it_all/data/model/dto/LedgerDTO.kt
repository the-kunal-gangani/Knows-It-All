package com.example.know_it_all.data.model.dto

import com.example.know_it_all.data.model.LedgerStatus

/**
 * Fixes applied vs original:
 *  1. LedgerEntryDTO was completely missing from the original DTO set —
 *     the Vault screen had no typed API representation for ledger entries.
 *  2. ratingGiven is Int, not Float — 1–5 discrete star rating.
 *  3. mentorId + learnerId are explicit typed fields, not buried inside
 *     a transactionData JSON string (which was the original antipattern).
 *  4. skillName is a denormalized snapshot for Vault display — avoids
 *     a JOIN on every ledger list render.
 *  5. status uses LedgerStatus enum, not a raw String.
 *  6. Added LedgerDisputeRequest — needed for the dispute flow in Vault.
 */

data class LedgerEntryDTO(
    val transactionId: String,
    val swapId: String,
    val mentorId: String,                   // ✅ explicit field, was buried in JSON blob
    val learnerId: String,                  // ✅ explicit field, was buried in JSON blob
    val skillName: String,                  // ✅ denormalized for Vault display
    val previousHash: String = "",
    val currentHash: String = "",
    val ratingGiven: Int = 0,              // ✅ Int, was Float
    val ratingComment: String = "",
    val status: LedgerStatus = LedgerStatus.COMPLETED,  // ✅ enum, not String
    val createdAt: Long? = null
)

data class LedgerDisputeRequest(           // ✅ was missing
    val transactionId: String,
    val reason: String
)