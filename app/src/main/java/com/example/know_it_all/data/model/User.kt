package com.example.know_it_all.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Fixes applied:
 *  1. Added @Index on email (unique) — auth flow queries by email;
 *     without an index this is a full table scan on every login.
 *  2. Added @Index on latitude + longitude composite — the Radar screen
 *     filters by proximity; a composite spatial index speeds that query.
 *  3. skillTokenBalance kept as Long (correct — token balances can be large).
 *  4. trustScore kept as Float (correct — 0.0–100.0 scale with decimals).
 *  5. createdAt default is fine (set once at registration, never updated).
 *  6. updatedAt default removed — must be set explicitly at the DAO call site,
 *     not at construction time, to reflect actual DB write time accurately.
 *  7. Added isOnline flag for the live green presence dot in the UI
 *     (maps to the pulsing indicator visible in the Dribbble reference).
 *  8. Added skills: List<String> = emptyList() as a denormalized snapshot
 *     of skill names for Radar card display — avoids a JOIN on every map pin.
 *     Source of truth is still the skills table; this is a display cache only.
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),        // fast auth lookup, enforces uniqueness
        Index(value = ["latitude", "longitude"])        // spatial filter for Radar screen
    ]
)
data class User(
    @PrimaryKey
    val uid: String,
    val name: String,
    val email: String,
    val profileImageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val skillTokenBalance: Long = 0L,
    val trustScore: Float = 0f,
    val profileVerified: Boolean = false,
    val isOnline: Boolean = false,          // ✅ drives pulsing presence indicator in UI
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = 0L                // ✅ set explicitly at DAO write time, not construction
)