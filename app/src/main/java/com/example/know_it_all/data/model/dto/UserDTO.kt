package com.example.know_it_all.data.model.dto

/**
 * Fixes applied vs original:
 *  1. Added createdAt + updatedAt — were missing, causing the cache mapper
 *     to default both to construction time instead of the server's values.
 *  2. Added isOnline — maps to the live presence dot in the Radar UI.
 *  3. All optional fields are nullable with sensible defaults so Gson
 *     never throws on missing/partial API responses.
 *  4. Added UserProfileUpdateRequest — was missing from the original DTO set.
 */

data class UserDTO(
    val uid: String,
    val name: String,
    val email: String,
    val profileImageUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val skillTokenBalance: Long? = null,
    val trustScore: Float? = null,
    val profileVerified: Boolean? = null,
    val isOnline: Boolean? = null,          // ✅ drives pulsing presence dot in Radar UI
    val createdAt: Long? = null,            // ✅ server-side timestamp, was missing
    val updatedAt: Long? = null             // ✅ server-side timestamp, was missing
)

data class UserRegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class UserLoginRequest(
    val email: String,
    val password: String
)

data class UserLocationUpdate(
    val latitude: Double,
    val longitude: Double
)

data class UserProfileUpdateRequest(  // ✅ was missing from original
    val name: String,
    val email: String
)