package com.example.know_it_all.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["latitude", "longitude"]),
        Index(value = ["role"])
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
    val isOnline: Boolean = false,
    val role: UserRole = UserRole.INDIVIDUAL,
    val organizationName: String = "",
    val organizationRegistrationId: String = "",
    val professionalBackground: String = "",
    val yearsOfExperience: Int = 0,
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_SUBMITTED,
    val verificationDocUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = 0L
)

enum class UserRole {
    INDIVIDUAL,
    RETIREE,
    INSTITUTION
}

enum class VerificationStatus {
    NOT_SUBMITTED,
    PENDING,
    VERIFIED,
    REJECTED
}