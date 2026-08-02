package com.example.know_it_all.data.model

import java.util.Calendar

/**
 * Location: data/model/AvailabilityModels.kt
 *
 * Supports two slot types:
 *  - RECURRING: repeats every week on the same day + time
 *  - ONE_OFF: a specific date + time, once only
 */

enum class DayOfWeek(val label: String, val shortLabel: String) {
    MONDAY("Monday", "Mon"),
    TUESDAY("Tuesday", "Tue"),
    WEDNESDAY("Wednesday", "Wed"),
    THURSDAY("Thursday", "Thu"),
    FRIDAY("Friday", "Fri"),
    SATURDAY("Saturday", "Sat"),
    SUNDAY("Sunday", "Sun")
}

enum class SlotType { RECURRING, ONE_OFF }

enum class SlotStatus { AVAILABLE, BOOKED, CANCELLED }

data class TimeSlot(
    val slotId: String = "",
    val userId: String = "",

    // Slot type
    val slotType: SlotType = SlotType.RECURRING,

    // For RECURRING — day of week + time
    val dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,

    // For ONE_OFF — specific date (epoch millis at midnight)
    val specificDate: Long = 0L,

    // Time — stored as hour + minute (24h format)
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val durationMinutes: Int = 60,

    // Booking state
    val status: SlotStatus = SlotStatus.AVAILABLE,
    val bookedByUserId: String? = null,
    val bookedSwapId: String? = null,

    val createdAt: Long = System.currentTimeMillis()
) {
    val endHour: Int
        get() = (startHour * 60 + startMinute + durationMinutes) / 60

    val endMinute: Int
        get() = (startHour * 60 + startMinute + durationMinutes) % 60

    val displayTime: String
        get() = "${formatTime(startHour, startMinute)} – ${formatTime(endHour, endMinute)}"

    val displayLabel: String
        get() = when (slotType) {
            SlotType.RECURRING -> "${dayOfWeek.label}s · $displayTime"
            SlotType.ONE_OFF   -> "${formatDate(specificDate)} · $displayTime"
        }

    val isAvailable: Boolean
        get() = status == SlotStatus.AVAILABLE

    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val h    = if (hour % 12 == 0) 12 else hour % 12
        val m    = minute.toString().padStart(2, '0')
        return "$h:$m $amPm"
    }

    private fun formatDate(epochMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun",
                            "Jul","Aug","Sep","Oct","Nov","Dec")
        return "${cal.get(Calendar.DAY_OF_MONTH)} ${months[cal.get(Calendar.MONTH)]}"
    }
}

fun TimeSlot.toMap(): Map<String, Any?> = mapOf(
    "slotId"          to slotId,
    "userId"          to userId,
    "slotType"        to slotType.name,
    "dayOfWeek"       to dayOfWeek.name,
    "specificDate"    to specificDate,
    "startHour"       to startHour,
    "startMinute"     to startMinute,
    "durationMinutes" to durationMinutes,
    "status"          to status.name,
    "bookedByUserId"  to bookedByUserId,
    "bookedSwapId"    to bookedSwapId,
    "createdAt"       to createdAt
)

fun Map<String, Any?>.toTimeSlot(): TimeSlot? {
    return try {
        TimeSlot(
            slotId          = this["slotId"] as? String ?: "",
            userId          = this["userId"] as? String ?: "",
            slotType        = SlotType.valueOf(this["slotType"] as? String ?: "RECURRING"),
            dayOfWeek       = DayOfWeek.valueOf(this["dayOfWeek"] as? String ?: "MONDAY"),
            specificDate    = (this["specificDate"] as? Long) ?: 0L,
            startHour       = (this["startHour"] as? Long)?.toInt() ?: 9,
            startMinute     = (this["startMinute"] as? Long)?.toInt() ?: 0,
            durationMinutes = (this["durationMinutes"] as? Long)?.toInt() ?: 60,
            status          = SlotStatus.valueOf(this["status"] as? String ?: "AVAILABLE"),
            bookedByUserId  = this["bookedByUserId"] as? String,
            bookedSwapId    = this["bookedSwapId"] as? String,
            createdAt       = (this["createdAt"] as? Long) ?: 0L
        )
    } catch (e: Exception) { null }
}