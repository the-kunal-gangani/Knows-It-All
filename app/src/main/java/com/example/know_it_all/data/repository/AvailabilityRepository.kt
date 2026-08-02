package com.example.know_it_all.data.repository

import com.example.know_it_all.data.model.SlotStatus
import com.example.know_it_all.data.model.TimeSlot
import com.example.know_it_all.data.model.toMap
import com.example.know_it_all.data.model.toTimeSlot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Location: data/repository/AvailabilityRepository.kt
 *
 * Firestore structure:
 *   availability/{userId}/slots/{slotId}
 */
class AvailabilityRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun slotsRef(userId: String) =
        db.collection("availability").document(userId).collection("slots")

    // ── Real-time observation ─────────────────────────────────────────────────

    fun observeUserSlots(userId: String): Flow<List<TimeSlot>> = callbackFlow {
        val listener = slotsRef(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val slots = snapshot?.documents
                    ?.mapNotNull { it.data?.toTimeSlot() }
                    ?.sortedWith(compareBy({ it.slotType.name }, { it.dayOfWeek.ordinal },
                        { it.startHour }, { it.startMinute }))
                    ?: emptyList()
                trySend(slots)
            }
        awaitClose { listener.remove() }
    }

    // ── One-shot reads ────────────────────────────────────────────────────────

    suspend fun getUserSlots(userId: String): Result<List<TimeSlot>> {
        return try {
            val snapshot = slotsRef(userId).get().await()
            val slots = snapshot.documents
                .mapNotNull { it.data?.toTimeSlot() }
                .sortedWith(compareBy({ it.slotType.name }, { it.dayOfWeek.ordinal },
                    { it.startHour }, { it.startMinute }))
            Result.success(slots)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getAvailableSlots(userId: String): Result<List<TimeSlot>> {
        return try {
            val snapshot = slotsRef(userId)
                .whereEqualTo("status", "AVAILABLE")
                .get().await()
            val slots = snapshot.documents
                .mapNotNull { it.data?.toTimeSlot() }
                .sortedWith(compareBy({ it.slotType.name }, { it.dayOfWeek.ordinal },
                    { it.startHour }, { it.startMinute }))
            Result.success(slots)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun addSlot(slot: TimeSlot): Result<TimeSlot> {
        return try {
            val slotId  = UUID.randomUUID().toString()
            val newSlot = slot.copy(slotId = slotId)
            slotsRef(slot.userId).document(slotId).set(newSlot.toMap()).await()
            Result.success(newSlot)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteSlot(userId: String, slotId: String): Result<Unit> {
        return try {
            slotsRef(userId).document(slotId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateSlot(slot: TimeSlot): Result<Unit> {
        return try {
            slotsRef(slot.userId).document(slot.slotId).set(slot.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Book a slot — called when swap is accepted ────────────────────────────

    suspend fun bookSlot(
        mentorUserId: String,
        slotId: String,
        learnerUserId: String,
        swapId: String
    ): Result<Unit> {
        return try {
            slotsRef(mentorUserId).document(slotId).update(
                mapOf(
                    "status"         to SlotStatus.BOOKED.name,
                    "bookedByUserId" to learnerUserId,
                    "bookedSwapId"   to swapId
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Release a slot — called when swap is cancelled/completed ─────────────

    suspend fun releaseSlot(mentorUserId: String, slotId: String): Result<Unit> {
        return try {
            slotsRef(mentorUserId).document(slotId).update(
                mapOf(
                    "status"         to SlotStatus.AVAILABLE.name,
                    "bookedByUserId" to null,
                    "bookedSwapId"   to null
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Delete all slots for a user ───────────────────────────────────────────

    suspend fun clearAllSlots(userId: String): Result<Unit> {
        return try {
            val snapshot = slotsRef(userId).get().await()
            val batch    = db.batch()
            snapshot.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}