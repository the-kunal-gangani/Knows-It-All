package com.example.know_it_all.data.local

import androidx.room.TypeConverter
import com.example.know_it_all.data.model.LedgerStatus
import com.example.know_it_all.data.model.ProficiencyLevel
import com.example.know_it_all.data.model.SkillCategory
import com.example.know_it_all.data.model.SwapStatus
import com.example.know_it_all.data.model.SwapType
import com.example.know_it_all.data.model.VerificationMethod

/**
 * Type converters for all custom types stored in Room.
 *
 * Why name-based (not ordinal-based):
 *   Room's default behaviour stores enum ordinals (0, 1, 2...).
 *   If you ever reorder the enum entries — e.g. insert HYBRID between
 *   DIGITAL and PHYSICAL — every existing row silently maps to the wrong
 *   value. Name-based storage (stores "DIGITAL", "PHYSICAL") is refactor-safe.
 *
 * Registration:
 *   Add @TypeConverters(RoomTypeConverters::class) to your AppDatabase class.
 *
 *   @Database(entities = [...], version = 1)
 *   @TypeConverters(RoomTypeConverters::class)
 *   abstract class KnowItAllDatabase : RoomDatabase() { ... }
 */
class RoomTypeConverters {

    // -------------------------------------------------------------------------
    // SkillCategory
    // -------------------------------------------------------------------------
    @TypeConverter
    fun skillCategoryToString(value: SkillCategory): String = value.name

    @TypeConverter
    fun stringToSkillCategory(value: String): SkillCategory =
        SkillCategory.valueOf(value)

    // -------------------------------------------------------------------------
    // ProficiencyLevel
    // -------------------------------------------------------------------------
    @TypeConverter
    fun proficiencyLevelToString(value: ProficiencyLevel): String = value.name

    @TypeConverter
    fun stringToProficiencyLevel(value: String): ProficiencyLevel =
        ProficiencyLevel.valueOf(value)

    // -------------------------------------------------------------------------
    // SwapStatus
    // -------------------------------------------------------------------------
    @TypeConverter
    fun swapStatusToString(value: SwapStatus): String = value.name

    @TypeConverter
    fun stringToSwapStatus(value: String): SwapStatus =
        SwapStatus.valueOf(value)

    // -------------------------------------------------------------------------
    // SwapType
    // -------------------------------------------------------------------------
    @TypeConverter
    fun swapTypeToString(value: SwapType): String = value.name

    @TypeConverter
    fun stringToSwapType(value: String): SwapType =
        SwapType.valueOf(value)

    // -------------------------------------------------------------------------
    // VerificationMethod
    // -------------------------------------------------------------------------
    @TypeConverter
    fun verificationMethodToString(value: VerificationMethod): String = value.name

    @TypeConverter
    fun stringToVerificationMethod(value: String): VerificationMethod =
        VerificationMethod.valueOf(value)

    // -------------------------------------------------------------------------
    // LedgerStatus
    // -------------------------------------------------------------------------
    @TypeConverter
    fun ledgerStatusToString(value: LedgerStatus): String = value.name

    @TypeConverter
    fun stringToLedgerStatus(value: String): LedgerStatus =
        LedgerStatus.valueOf(value)
}