package com.example.know_it_all.data.repository

import com.example.know_it_all.data.model.User
import com.example.know_it_all.data.model.UserRole
import com.example.know_it_all.data.model.VerificationStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Location: data/repository/UserRepository.kt
 *
 * Firestore structure:
 *   users/{userId}
 */
class UserRepository {

    private val db        = FirebaseFirestore.getInstance()
    private val usersCol   = db.collection("users")

    // ── One-shot reads ────────────────────────────────────────────────────────

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val snapshot = usersCol.document(userId).get().await()
            val user = snapshot.toUser() ?: throw Exception("User not found")
            Result.success(user)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Writes ────────────────────────────────────────────────────────────────

    suspend fun createUser(user: User): Result<Unit> {
        return try {
            usersCol.document(user.uid).set(user.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateProfessionalBackground(
        userId: String,
        professionalBackground: String,
        yearsOfExperience: Int
    ): Result<Unit> {
        return try {
            usersCol.document(userId).update(
                mapOf(
                    "professionalBackground" to professionalBackground,
                    "yearsOfExperience"      to yearsOfExperience,
                    "updatedAt"              to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun submitVerificationDoc(
        userId: String,
        verificationDocUrl: String
    ): Result<Unit> {
        return try {
            usersCol.document(userId).update(
                mapOf(
                    "verificationDocUrl" to verificationDocUrl,
                    "verificationStatus" to VerificationStatus.PENDING.name,
                    "updatedAt"          to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}

// ── Extensions ────────────────────────────────────────────────────────────────

private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
    return try {
        User(
            uid                         = getString("uid") ?: id,
            name                        = getString("name") ?: "",
            email                       = getString("email") ?: "",
            profileImageUrl             = getString("profileImageUrl") ?: "",
            latitude                    = getDouble("latitude") ?: 0.0,
            longitude                   = getDouble("longitude") ?: 0.0,
            skillTokenBalance           = getLong("skillTokenBalance") ?: 0L,
            trustScore                  = (getDouble("trustScore") ?: 0.0).toFloat(),
            profileVerified             = getBoolean("profileVerified") ?: false,
            isOnline                    = getBoolean("isOnline") ?: false,
            role                        = runCatching {
                UserRole.valueOf(getString("role") ?: "INDIVIDUAL")
            }.getOrDefault(UserRole.INDIVIDUAL),
            organizationName            = getString("organizationName") ?: "",
            organizationRegistrationId  = getString("organizationRegistrationId") ?: "",
            professionalBackground      = getString("professionalBackground") ?: "",
            yearsOfExperience           = getLong("yearsOfExperience")?.toInt() ?: 0,
            verificationStatus          = runCatching {
                VerificationStatus.valueOf(getString("verificationStatus") ?: "NOT_SUBMITTED")
            }.getOrDefault(VerificationStatus.NOT_SUBMITTED),
            verificationDocUrl          = getString("verificationDocUrl") ?: "",
            createdAt                   = getLong("createdAt") ?: 0L,
            updatedAt                   = getLong("updatedAt") ?: 0L
        )
    } catch (e: Exception) { null }
}

private fun User.toMap(): Map<String, Any?> = mapOf(
    "uid"                        to uid,
    "name"                       to name,
    "email"                      to email,
    "profileImageUrl"            to profileImageUrl,
    "latitude"                   to latitude,
    "longitude"                  to longitude,
    "skillTokenBalance"          to skillTokenBalance,
    "trustScore"                 to trustScore,
    "profileVerified"            to profileVerified,
    "isOnline"                   to isOnline,
    "role"                       to role.name,
    "organizationName"           to organizationName,
    "organizationRegistrationId" to organizationRegistrationId,
    "professionalBackground"     to professionalBackground,
    "yearsOfExperience"          to yearsOfExperience,
    "verificationStatus"         to verificationStatus.name,
    "verificationDocUrl"         to verificationDocUrl,
    "createdAt"                  to createdAt,
    "updatedAt"                  to updatedAt
)