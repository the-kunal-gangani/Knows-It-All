package com.example.know_it_all.data.model.dto

/**
 * Fixes applied vs original:
 *  1. SkillDTO was completely missing from the original DTO set —
 *     the API had no typed representation for skill payloads.
 *  2. skillId is String UUID matching the corrected Skill entity.
 *  3. category and proficiencyLevel kept as String here (not enums)
 *     because the API may send unexpected values; enum conversion
 *     happens in the mapper layer (SkillDTO → Skill), not at
 *     deserialization time, so a bad API value doesn't crash the app.
 *  4. Added tokenValue — part of the SkillToken economy, must round-trip
 *     through the API so both parties agree on session cost.
 *  5. Added SkillCreateRequest + SkillEndorseRequest — needed for
 *     SkillProfileScreen add/endorse flows.
 */

data class SkillDTO(
    val skillId: String,                    // ✅ String UUID
    val userId: String,
    val skillName: String,
    val description: String = "",
    val category: String = "",              // String here — enum mapping in mapper layer
    val proficiencyLevel: String = "",      // String here — enum mapping in mapper layer
    val tokenValue: Int = 10,              // ✅ session token cost
    val verificationStatus: Boolean = false,
    val endorsements: Int = 0,
    val createdAt: Long? = null
)

data class SkillCreateRequest(             // ✅ was missing
    val skillName: String,
    val description: String = "",
    val category: String,
    val proficiencyLevel: String,
    val tokenValue: Int = 10
)

data class SkillEndorseRequest(            // ✅ was missing
    val skillId: String,
    val endorserId: String
)