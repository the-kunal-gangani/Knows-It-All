package com.example.know_it_all.util

import java.security.MessageDigest

/**
 * No logic changes — SHA-256 implementation is correct.
 *
 * Usage note after Batch 1 entity fix:
 *   TrustLedger no longer has a transactionData: String field (that was the
 *   raw JSON blob antipattern removed in Batch 1). To generate a transaction
 *   hash, serialize the explicit typed fields instead:
 *
 *   val transactionData = buildString {
 *       append(swapId)
 *       append(mentorId)
 *       append(learnerId)
 *       append(skillName)
 *       append(ratingGiven)
 *       append(timestamp)
 *   }
 *   val currentHash = HashUtil.generateTransactionHash(transactionData, previousHash)
 *
 *   This gives deterministic, auditable hashes without serializing Room entities
 *   to JSON strings.
 */
object HashUtil {

    fun sha256(input: String): String {
        val bytes = input.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes)
            .fold("") { str, byte -> str + "%02x".format(byte) }
    }

    /**
     * Generates a transaction hash for the trust ledger chain.
     * @param transactionData Deterministic string of the transaction's fields
     * @param previousHash    Hash of the previous ledger entry (or "" for first entry)
     */
    fun generateTransactionHash(transactionData: String, previousHash: String): String {
        return sha256(transactionData + previousHash)
    }

    /**
     * Convenience overload — builds the transaction data string from
     * the explicit TrustLedger fields and hashes it.
     */
    fun generateLedgerHash(
        swapId: String,
        mentorId: String,
        learnerId: String,
        skillName: String,
        ratingGiven: Int,
        timestamp: Long,
        previousHash: String
    ): String {
        val transactionData = "$swapId$mentorId$learnerId$skillName$ratingGiven$timestamp"
        return generateTransactionHash(transactionData, previousHash)
    }
}