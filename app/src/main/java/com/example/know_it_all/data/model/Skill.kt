package com.example.know_it_all.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Fixes applied:
 *  1. skillId changed from Int (autoGenerate) → String UUID.
 *     Int PKs leak enumeration and conflict with the String-based foreign keys
 *     used across User, Swap, and TrustLedger.
 *  2. Added @Entity indices on userId (frequent JOIN target) and skillName
 *     (searched/filtered in Radar screen).
 *  3. Added @ForeignKey to User so orphaned skills are cleaned up on user delete.
 *  4. TypeConverters for SkillCategory and ProficiencyLevel are declared in
 *     RoomTypeConverters.kt (see that file) — Room stores enum NAME strings,
 *     not ordinals, so reordering enums never silently corrupts data.
 *  5. tokenValue added — each skill needs a declared token cost for the
 *     SkillToken economy; this was missing from the original model.
 */
@Entity(
    tableName = "skills",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE   // deleting a user removes their skills
        )
    ],
    indices = [
        Index(value = ["userId"]),          // fast lookup of skills by owner
        Index(value = ["skillName"])        // fast filter/search in Radar
    ]
)
data class Skill(
    @PrimaryKey
    val skillId: String = UUID.randomUUID().toString(),   // ✅ String UUID, not autoGenerate Int
    val userId: String,
    val skillName: String,
    val description: String = "",
    val category: SkillCategory,
    val proficiencyLevel: ProficiencyLevel,
    val tokenValue: Int = 10,               // ✅ token cost for this skill session
    val verificationStatus: Boolean = false,
    val endorsements: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
    // updatedAt intentionally omitted — skills are immutable once created;
    // edit = delete + re-create to preserve ledger integrity.
)

enum class SkillCategory {
    DIGITAL,    // Programming, design, digital marketing, etc.
    PHYSICAL,   // Carpentry, cooking, fitness, etc.
    HYBRID      // Multi-discipline or blended skills
}

enum class ProficiencyLevel {
    BEGINNER,       // Basic awareness
    INTERMEDIATE,   // Working proficiency
    ADVANCED,       // Strong independent ability
    EXPERT          // Can teach and certify others
}