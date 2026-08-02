package com.example.know_it_all.data.repository

import com.example.know_it_all.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Replaces the old UserRepository that used Retrofit + Room.
 * Now uses Firebase Auth for login/register and Firestore for profile data.
 */
class FirebaseUserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            // Create Firebase Auth account
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Registration failed")

            // Create Firestore user document
            val user = User(
                uid = uid,
                name = name,
                email = email,
                skillTokenBalance = 100L,
                trustScore = 0f,
                profileVerified = false,
                isOnline = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            usersCollection.document(uid).set(user.toMap()).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Login failed")

            // Mark user as online
            usersCollection.document(uid)
                .update("isOnline", true, "updatedAt", System.currentTimeMillis())
                .await()

            // Fetch profile from Firestore
            val snapshot = usersCollection.document(uid).get().await()
            val user = snapshot.toUser() ?: throw Exception("User profile not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                usersCollection.document(uid)
                    .update("isOnline", false, "updatedAt", System.currentTimeMillis())
                    .await()
            }
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // ── Profile ───────────────────────────────────────────────────────────────

    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            val user = snapshot.toUser() ?: throw Exception("User not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observe current user profile in real-time.
     * Screen updates automatically when Firestore data changes.
     */
    fun observeUserProfile(userId: String): Flow<User?> = callbackFlow {
        val listener = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toUser())
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateProfile(userId: String, name: String, email: String): Result<Unit> {
        return try {
            usersCollection.document(userId).update(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Location / Radar ──────────────────────────────────────────────────────

    suspend fun updateLocation(userId: String, latitude: Double, longitude: Double): Result<Unit> {
        return try {
            usersCollection.document(userId).update(
                mapOf(
                    "latitude"  to latitude,
                    "longitude" to longitude,
                    "isOnline"  to true,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get nearby users using Firestore bounding box query.
     * Firestore doesn't support true geo queries natively so we use
     * a lat/lon bounding box and filter with Haversine after.
     */
    suspend fun getNearbyUsers(
        currentUserId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 5.0
    ): Result<List<User>> {
        return try {
            val latDelta = radiusKm / 111.0

            // ✅ Only filter on latitude — avoids composite index requirement
            // Then filter uid and longitude client-side
            val snapshot = usersCollection
                .whereGreaterThan("latitude", latitude - latDelta)
                .whereLessThan("latitude", latitude + latDelta)
                .get().await()

            val lonDelta = radiusKm / 105.0
            val users = snapshot.documents
                .mapNotNull { it.toUser() }
                .filter { user ->
                    user.uid != currentUserId &&
                    user.longitude >= longitude - lonDelta &&
                    user.longitude <= longitude + lonDelta &&
                    calculateDistanceKm(latitude, longitude,
                        user.latitude, user.longitude) <= radiusKm
                }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Token balance ─────────────────────────────────────────────────────────

    fun observeTokenBalance(userId: String): Flow<Long> = callbackFlow {
        val listener = usersCollection.document(userId)
            .addSnapshotListener { snapshot, _ ->
                val balance = snapshot?.getLong("skillTokenBalance") ?: 0L
                trySend(balance)
            }
        awaitClose { listener.remove() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun calculateDistanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }
}

// ── Extension: Firestore document → User ─────────────────────────────────────

private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
    return try {
        User(
            uid              = getString("uid") ?: id,
            name             = getString("name") ?: "",
            email            = getString("email") ?: "",
            profileImageUrl  = getString("profileImageUrl") ?: "",
            latitude         = getDouble("latitude") ?: 0.0,
            longitude        = getDouble("longitude") ?: 0.0,
            skillTokenBalance = getLong("skillTokenBalance") ?: 0L,
            trustScore       = getDouble("trustScore")?.toFloat() ?: 0f,
            profileVerified  = getBoolean("profileVerified") ?: false,
            isOnline         = getBoolean("isOnline") ?: false,
            createdAt        = getLong("createdAt") ?: 0L,
            updatedAt        = getLong("updatedAt") ?: 0L
        )
    } catch (e: Exception) { null }
}

// ── Extension: User → Firestore map ──────────────────────────────────────────

private fun User.toMap(): Map<String, Any?> = mapOf(
    "uid"               to uid,
    "name"              to name,
    "email"             to email,
    "profileImageUrl"   to profileImageUrl,
    "latitude"          to latitude,
    "longitude"         to longitude,
    "skillTokenBalance" to skillTokenBalance,
    "trustScore"        to trustScore,
    "profileVerified"   to profileVerified,
    "isOnline"          to isOnline,
    "createdAt"         to createdAt,
    "updatedAt"         to updatedAt
)