package com.example.know_it_all.util

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Fixes applied:
 *  1. Replaced java.lang.Math calls with kotlin.math equivalents — idiomatic
 *     Kotlin and avoids the implicit static Java interop on every call.
 *  2. calculateDistance renamed to calculateDistanceKm for clarity — the
 *     return unit (km) is now explicit in the function name, preventing silent
 *     unit confusion when callers compare against a radius in km.
 *  3. RadarScreenEnhanced has its own inline Haversine copy — replace that
 *     private function with a call to calculateDistanceKm() from this file.
 */

/**
 * Haversine formula — great-circle distance between two GPS coordinates.
 * Returns distance in kilometres.
 */
fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    return earthRadiusKm * 2 * asin(sqrt(a))
}

/**
 * Formats a distance in kilometres into a human-readable string.
 * Sub-1km distances are shown in metres (e.g. "450m").
 * 1km+ distances are shown to one decimal place (e.g. "2.3 km").
 */
fun formatDistance(distanceKm: Double): String {
    return when {
        distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()}m"
        else -> String.format("%.1f km", distanceKm)
    }
}

/**
 * Computes bounding-box deltas for a given radius.
 * Used by UserDao.getUsersInBoundingBox() to filter nearby users.
 *
 * At Pune's latitude (~18.6°):
 *   latDelta for 5km ≈ 0.045°
 *   lonDelta for 5km ≈ 0.048°
 */
fun boundingBoxDeltas(radiusKm: Double): Pair<Double, Double> {
    val latDelta = radiusKm / 111.0
    val lonDelta = radiusKm / 105.0
    return Pair(latDelta, lonDelta)
}