package com.example.know_it_all.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

/**
 * Fixes applied:
 *  1. Re-enabled — the original file had its package declaration commented out
 *     ("// DISABLED - Missing dependencies") but play.services.location is
 *     already in build.gradle.kts. The file was functional, just incorrectly
 *     disabled.
 *  2. getBestAvailableLocation() added — tries getCurrentLocation() first
 *     for highest accuracy, falls back to lastLocation if the device doesn't
 *     return a fresh fix within the timeout. RadarViewModel should call this
 *     method to wire in real GPS coordinates before loadNearbyUsers().
 *
 * Usage in RadarViewModel:
 *
 *   private val locationService = LocationService(context)
 *
 *   fun refreshLocation(context: Context) {
 *       viewModelScope.launch {
 *           val location = LocationService(context).getBestAvailableLocation()
 *           location?.let {
 *               updateLocation(it.latitude, it.longitude)
 *               loadNearbyUsers()
 *           }
 *       }
 *   }
 *
 * Note: The screen is responsible for requesting ACCESS_FINE_LOCATION
 * permission before calling this. RadarScreenEnhanced already does this
 * via rememberPermissionState.
 */
class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Requests a fresh high-accuracy GPS fix.
     * Returns null if permission is denied or the request times out.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns the most recently known location.
     * Faster than getCurrentLocation() but may be stale (minutes old).
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Best-effort location — tries a fresh fix first, falls back to the
     * last known location if a fresh fix is unavailable.
     * This is what RadarViewModel should call.
     */
    @SuppressLint("MissingPermission")
    suspend fun getBestAvailableLocation(): Location? {
        return getCurrentLocation() ?: getLastLocation()
    }
}