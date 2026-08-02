package com.example.know_it_all.data.model

import com.example.know_it_all.data.model.dto.UserDTO

/**
 * Fixes applied:
 *  1. AuthData now includes `name: String?` — previously AuthViewModel saved
 *     an empty string for the user's name on login because AuthData had no
 *     name field. The backend must return the name at login time.
 *  2. AuthResponse.user kept as UserDTO? — useful for pre-populating the
 *     local cache immediately after register without a second profile fetch.
 *  3. ApiResponse.message changed from non-null String to String? — the
 *     backend may not always send a message body, and forcing a non-null
 *     string would cause a NullPointerException during Gson deserialization
 *     if the field is absent from the JSON.
 */

/**
 * Standard envelope for all API responses.
 * success = true  → data is populated, error is null
 * success = false → error message is populated, data is null
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,            // ✅ nullable — not always present in JSON
    val data: T? = null,
    val error: String? = null
)

/**
 * Raw response from POST /auth/login and POST /auth/register.
 * Kept separate from AuthData so the ViewModel only works with
 * the stripped-down AuthData — it never needs the full UserDTO.
 */
data class AuthResponse(
    val token: String,
    val userId: String,
    val name: String? = null,               // ✅ name returned at auth time
    val user: UserDTO? = null               // optional full profile pre-load
)

/**
 * Minimal auth payload stored by SessionManager and passed to ViewModels.
 * Deliberately small — only what the app needs to function after login.
 * The raw JWT lives only in SessionManager, not here.
 */
data class AuthData(
    val token: String,
    val userId: String,
    val name: String? = null                // ✅ added so login can persist the user's name
)