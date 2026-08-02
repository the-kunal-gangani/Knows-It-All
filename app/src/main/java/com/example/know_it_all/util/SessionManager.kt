package com.example.know_it_all.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Simplified SessionManager for Firebase.
 *
 * Firebase Auth manages the token internally — we no longer need to
 * store or retrieve a JWT manually. SessionManager now only stores
 * local user preferences like name, email, and location settings.
 *
 * isLoggedIn() now checks FirebaseAuth.currentUser instead of a token.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME                     = "KnowItAllPrefs"
        private const val KEY_USER_ID                   = "user_id"
        private const val KEY_USER_EMAIL                = "user_email"
        private const val KEY_USER_NAME                 = "user_name"
        private const val KEY_LOCATION_PERMISSION_ASKED   = "location_permission_asked"
        private const val KEY_LOCATION_PERMISSION_ENABLED = "location_permission_enabled"
    }

    // ── User info ─────────────────────────────────────────────────────────────

    fun saveUserInfo(userId: String, name: String, email: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID,    userId)
            putString(KEY_USER_NAME,  name)
            putString(KEY_USER_EMAIL, email)
        }.apply()
    }

    fun getUserId(): String?    = prefs.getString(KEY_USER_ID, null)
    fun getUserName(): String?  = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    // ── Auth state — delegated to Firebase ───────────────────────────────────

    /**
     * Returns true if Firebase Auth has a current signed-in user.
     * No JWT token needed — Firebase manages session persistence.
     */
    fun isLoggedIn(): Boolean {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
    }

    /**
     * Returns the current Firebase user's UID.
     * Use this instead of getUserId() when you need a guaranteed fresh value.
     */
    fun getFirebaseUserId(): String? {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    }

    /**
     * Returns the Firebase ID token for API authentication.
     * Suspends until token is retrieved from Firebase.
     */
    suspend fun getToken(): String? {
        return try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.result?.token
        } catch (e: Exception) {
            null
        }
    }

    // ── Clear session on logout ───────────────────────────────────────────────

    fun clearSession() {
        prefs.edit().clear().apply()
        // Firebase sign-out is handled in FirebaseUserRepository.logout()
    }

    // ── Location permission ───────────────────────────────────────────────────

    fun setLocationPermissionAsked() {
        prefs.edit().putBoolean(KEY_LOCATION_PERMISSION_ASKED, true).apply()
    }

    fun hasLocationPermissionBeenAsked(): Boolean =
        prefs.getBoolean(KEY_LOCATION_PERMISSION_ASKED, false)

    fun setLocationPermissionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCATION_PERMISSION_ENABLED, enabled).apply()
    }

    fun isLocationPermissionEnabled(): Boolean =
        prefs.getBoolean(KEY_LOCATION_PERMISSION_ENABLED, false)
}