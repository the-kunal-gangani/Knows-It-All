package com.example.know_it_all.data.repository

import com.example.know_it_all.data.model.EnrollmentStatus
import com.example.know_it_all.data.model.GroupEnrollment
import com.example.know_it_all.data.model.GroupSession
import com.example.know_it_all.data.model.GroupSessionStatus
import com.example.know_it_all.data.model.SessionConfig
import com.example.know_it_all.data.model.SkillCategory
import com.example.know_it_all.data.model.toMap
import com.example.know_it_all.util.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Location: data/repository/GroupSessionRepository.kt
 *
 * Firestore structure:
 *   group_sessions/{sessionId}
 *   group_sessions/{sessionId}/enrollments/{enrollmentId}
 */
class GroupSessionRepository {

    private val db          = FirebaseFirestore.getInstance()
    private val sessionsCol = db.collection("group_sessions")
    private val usersCol    = db.collection("users")

    private fun enrollmentsCol(sessionId: String) =
        sessionsCol.document(sessionId).collection("enrollments")

    // ── Real-time streams ─────────────────────────────────────────────────────

    fun observeOpenSessions(): Flow<List<GroupSession>> = callbackFlow {
        val listener = sessionsCol
            .whereEqualTo("status", "OPEN")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val sessions = snapshot?.documents?.mapNotNull { it.toGroupSession() }
                    ?: emptyList()
                trySend(sessions)
            }
        awaitClose { listener.remove() }
    }

    fun observeMentorSessions(mentorId: String): Flow<List<GroupSession>> = callbackFlow {
        val listener = sessionsCol
            .whereEqualTo("mentorId", mentorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val sessions = snapshot?.documents?.mapNotNull { it.toGroupSession() }
                    ?: emptyList()
                trySend(sessions)
            }
        awaitClose { listener.remove() }
    }

    fun observeEnrollments(sessionId: String): Flow<List<GroupEnrollment>> = callbackFlow {
        val listener = enrollmentsCol(sessionId)
            .orderBy("enrolledAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val enrollments = snapshot?.documents?.mapNotNull { it.toGroupEnrollment() }
                    ?: emptyList()
                trySend(enrollments)
            }
        awaitClose { listener.remove() }
    }

    // ── Create session (mentor) ───────────────────────────────────────────────

    suspend fun createSession(
        mentorId: String,
        skillName: String,
        skillId: String,
        description: String,
        category: SkillCategory,
        maxLearners: Int,
        durationMinutes: Int,
        tokenPricePerLearner: Long,
        scheduledAt: Long = 0L,
        isScheduled: Boolean = false
    ): Result<GroupSession> {
        return try {
            val mentorDoc  = usersCol.document(mentorId).get().await()
            val mentorName = mentorDoc.getString("name") ?: ""
            val sessionId  = UUID.randomUUID().toString()
            val roomName   = "KnowItAll_Group_${sessionId.take(8)}"

            val session = GroupSession(
                sessionId            = sessionId,
                mentorId             = mentorId,
                mentorName           = mentorName,
                skillName            = skillName,
                skillId              = skillId,
                description          = description,
                category             = category,
                maxLearners          = maxLearners,
                currentLearners      = 0,
                durationMinutes      = durationMinutes,
                tokenPricePerLearner = tokenPricePerLearner,
                scheduledAt          = scheduledAt,
                isScheduled          = isScheduled,
                status               = GroupSessionStatus.OPEN,
                jitsiRoomName        = roomName
            )

            sessionsCol.document(sessionId).set(session.toMap()).await()
            Result.success(session)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Join session (learner) — locks tokens in escrow ───────────────────────

    suspend fun joinSession(
        sessionId: String,
        learnerId: String
    ): Result<GroupEnrollment> {
        return try {
            val sessionDoc = sessionsCol.document(sessionId).get().await()
            val session    = sessionDoc.toGroupSession()
                ?: throw Exception("Session not found")

            if (session.isFull) throw Exception("Session is full")
            if (session.status != GroupSessionStatus.OPEN)
                throw Exception("Session is no longer open")

            val learnerDoc  = usersCol.document(learnerId).get().await()
            val learnerName = learnerDoc.getString("name") ?: ""
            val balance     = learnerDoc.getLong("skillTokenBalance") ?: 0L

            if (balance < session.tokenPricePerLearner)
                throw Exception("Insufficient tokens. Need ${session.tokenPricePerLearner}T")

            val enrollmentId = UUID.randomUUID().toString()
            val enrollment = GroupEnrollment(
                enrollmentId = enrollmentId,
                sessionId    = sessionId,
                learnerId    = learnerId,
                learnerName  = learnerName,
                tokensLocked = session.tokenPricePerLearner,
                status       = EnrollmentStatus.ENROLLED
            )

            db.runTransaction { transaction ->
                val learnerRef  = usersCol.document(learnerId)
                val sessionRef  = sessionsCol.document(sessionId)
                val enrollRef   = enrollmentsCol(sessionId).document(enrollmentId)

                // Deduct tokens from learner
                transaction.update(learnerRef, "skillTokenBalance",
                    balance - session.tokenPricePerLearner)

                // Increment learner count
                transaction.update(sessionRef, "currentLearners",
                    session.currentLearners + 1)

                // Create enrollment
                transaction.set(enrollRef, enrollment.toMap())
            }.await()

            // Notify mentor
            NotificationHelper.notifyGroupSessionJoined(
                toUserId     = session.mentorId,
                learnerName  = learnerName,
                skillName    = session.skillName,
                spotsLeft    = session.spotsLeft - 1
            )

            Result.success(enrollment)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Start session (mentor) ────────────────────────────────────────────────

    suspend fun startSession(sessionId: String): Result<GroupSession> {
        return try {
            val now = System.currentTimeMillis()
            sessionsCol.document(sessionId).update(mapOf(
                "status"    to GroupSessionStatus.ACTIVE.name,
                "startedAt" to now
            )).await()

            // Notify all enrolled learners
            val enrollments = enrollmentsCol(sessionId)
                .whereEqualTo("status", EnrollmentStatus.ENROLLED.name)
                .get().await()
                .mapNotNull { it.toGroupEnrollment() }

            val sessionDoc = sessionsCol.document(sessionId).get().await()
            val session    = sessionDoc.toGroupSession() ?: throw Exception("Not found")

            enrollments.forEach { enrollment ->
                NotificationHelper.notifyGroupSessionStarted(
                    toUserId  = enrollment.learnerId,
                    skillName = session.skillName,
                    mentorName = session.mentorName,
                    sessionId = sessionId
                )
            }

            Result.success(session)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Complete session (mentor) ─────────────────────────────────────────────

    suspend fun completeSession(sessionId: String): Result<GroupSession> {
        return try {
            sessionsCol.document(sessionId).update(mapOf(
                "status"      to GroupSessionStatus.COMPLETED.name,
                "completedAt" to System.currentTimeMillis()
            )).await()

            val sessionDoc = sessionsCol.document(sessionId).get().await()
            val session    = sessionDoc.toGroupSession() ?: throw Exception("Not found")

            // Notify learners to rate
            val enrollments = enrollmentsCol(sessionId)
                .whereEqualTo("status", EnrollmentStatus.ENROLLED.name)
                .get().await()
                .mapNotNull { it.toGroupEnrollment() }

            enrollments.forEach { enrollment ->
                NotificationHelper.notifyGroupSessionComplete(
                    toUserId  = enrollment.learnerId,
                    skillName = session.skillName,
                    sessionId = sessionId
                )
            }

            Result.success(session)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Rate session (learner) — releases tokens based on rating ─────────────

    suspend fun rateSession(
        sessionId: String,
        learnerId: String,
        rating: Float,
        comment: String = ""
    ): Result<Unit> {
        return try {
            val sessionDoc = sessionsCol.document(sessionId).get().await()
            val session    = sessionDoc.toGroupSession() ?: throw Exception("Not found")

            val enrollmentSnap = enrollmentsCol(sessionId)
                .whereEqualTo("learnerId", learnerId)
                .limit(1).get().await()
            val enrollment = enrollmentSnap.documents.firstOrNull()?.toGroupEnrollment()
                ?: throw Exception("Enrollment not found")

            val tokensToMentor = SessionConfig.tokensToMentor(enrollment.tokensLocked, rating)
            val tokensToReturn = SessionConfig.tokensToEscrow(enrollment.tokensLocked, rating)

            db.runTransaction { transaction ->
                val mentorRef   = usersCol.document(session.mentorId)
                val learnerRef  = usersCol.document(learnerId)
                val enrollRef   = enrollmentsCol(sessionId)
                    .document(enrollment.enrollmentId)

                val mentorSnap  = transaction.get(mentorRef)
                val learnerSnap = transaction.get(learnerRef)
                val mentorBal   = mentorSnap.getLong("skillTokenBalance") ?: 0L
                val learnerBal  = learnerSnap.getLong("skillTokenBalance") ?: 0L

                // Release tokens to mentor
                if (tokensToMentor > 0)
                    transaction.update(mentorRef, "skillTokenBalance", mentorBal + tokensToMentor)

                // Return held tokens to learner
                if (tokensToReturn > 0)
                    transaction.update(learnerRef, "skillTokenBalance", learnerBal + tokensToReturn)

                // Update enrollment
                transaction.update(enrollRef, mapOf(
                    "status"        to EnrollmentStatus.COMPLETED.name,
                    "rating"        to rating,
                    "ratingComment" to comment
                ))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Cancel session (mentor) — refund all learners ─────────────────────────

    suspend fun cancelSession(sessionId: String): Result<Unit> {
        return try {
            val enrollments = enrollmentsCol(sessionId)
                .whereEqualTo("status", EnrollmentStatus.ENROLLED.name)
                .get().await()
                .mapNotNull { it.toGroupEnrollment() }

            db.runTransaction { transaction ->
                // Refund all learners
                enrollments.forEach { enrollment ->
                    val learnerRef  = usersCol.document(enrollment.learnerId)
                    val learnerSnap = transaction.get(learnerRef)
                    val bal = learnerSnap.getLong("skillTokenBalance") ?: 0L
                    transaction.update(learnerRef, "skillTokenBalance",
                        bal + enrollment.tokensLocked)
                    transaction.update(
                        enrollmentsCol(sessionId).document(enrollment.enrollmentId),
                        "status", EnrollmentStatus.REFUNDED.name
                    )
                }
                transaction.update(sessionsCol.document(sessionId),
                    "status", GroupSessionStatus.CANCELLED.name)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Leave session (learner) — refund if still OPEN ───────────────────────

    suspend fun leaveSession(sessionId: String, learnerId: String): Result<Unit> {
        return try {
            val sessionDoc = sessionsCol.document(sessionId).get().await()
            val session    = sessionDoc.toGroupSession() ?: throw Exception("Not found")

            if (session.status != GroupSessionStatus.OPEN)
                throw Exception("Cannot leave an active or completed session")

            val enrollSnap = enrollmentsCol(sessionId)
                .whereEqualTo("learnerId", learnerId).limit(1).get().await()
            val enrollment = enrollSnap.documents.firstOrNull()?.toGroupEnrollment()
                ?: throw Exception("Enrollment not found")

            db.runTransaction { transaction ->
                val learnerRef  = usersCol.document(learnerId)
                val learnerSnap = transaction.get(learnerRef)
                val bal = learnerSnap.getLong("skillTokenBalance") ?: 0L

                // Refund tokens
                transaction.update(learnerRef, "skillTokenBalance",
                    bal + enrollment.tokensLocked)

                // Update enrollment
                transaction.update(
                    enrollmentsCol(sessionId).document(enrollment.enrollmentId),
                    "status", EnrollmentStatus.REFUNDED.name
                )

                // Decrement learner count
                transaction.update(sessionsCol.document(sessionId),
                    "currentLearners", session.currentLearners - 1)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Get open sessions for Feed ────────────────────────────────────────────

    suspend fun getOpenSessions(excludeMentorId: String): Result<List<GroupSession>> {
        return try {
            val snapshot = sessionsCol
                .whereEqualTo("status", "OPEN")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get().await()
            val sessions = snapshot.documents
                .mapNotNull { it.toGroupSession() }
                .filter { it.mentorId != excludeMentorId }
            Result.success(sessions)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ── Check if learner is enrolled ──────────────────────────────────────────

    suspend fun isEnrolled(sessionId: String, learnerId: String): Boolean {
        return try {
            val snap = enrollmentsCol(sessionId)
                .whereEqualTo("learnerId", learnerId)
                .whereIn("status", listOf("ENROLLED"))
                .limit(1).get().await()
            !snap.isEmpty
        } catch (e: Exception) { false }
    }
}

// ── Document snapshot extensions ──────────────────────────────────────────────

private fun com.google.firebase.firestore.DocumentSnapshot.toGroupSession(): GroupSession? {
    return try {
        GroupSession(
            sessionId            = getString("sessionId") ?: id,
            mentorId             = getString("mentorId") ?: "",
            mentorName           = getString("mentorName") ?: "",
            skillName            = getString("skillName") ?: "",
            skillId              = getString("skillId") ?: "",
            description          = getString("description") ?: "",
            category             = runCatching {
                SkillCategory.valueOf(getString("category") ?: "DIGITAL")
            }.getOrDefault(SkillCategory.DIGITAL),
            maxLearners          = getLong("maxLearners")?.toInt() ?: 5,
            currentLearners      = getLong("currentLearners")?.toInt() ?: 0,
            durationMinutes      = getLong("durationMinutes")?.toInt() ?: 60,
            tokenPricePerLearner = getLong("tokenPricePerLearner") ?: 10L,
            scheduledAt          = getLong("scheduledAt") ?: 0L,
            isScheduled          = getBoolean("isScheduled") ?: false,
            status               = runCatching {
                GroupSessionStatus.valueOf(getString("status") ?: "OPEN")
            }.getOrDefault(GroupSessionStatus.OPEN),
            jitsiRoomName        = getString("jitsiRoomName") ?: "",
            createdAt            = getLong("createdAt") ?: 0L,
            startedAt            = getLong("startedAt"),
            completedAt          = getLong("completedAt")
        )
    } catch (e: Exception) { null }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toGroupEnrollment(): GroupEnrollment? {
    return try {
        GroupEnrollment(
            enrollmentId  = getString("enrollmentId") ?: id,
            sessionId     = getString("sessionId") ?: "",
            learnerId     = getString("learnerId") ?: "",
            learnerName   = getString("learnerName") ?: "",
            tokensLocked  = getLong("tokensLocked") ?: 0L,
            status        = runCatching {
                EnrollmentStatus.valueOf(getString("status") ?: "ENROLLED")
            }.getOrDefault(EnrollmentStatus.ENROLLED),
            rating        = getDouble("rating")?.toFloat(),
            ratingComment = getString("ratingComment") ?: "",
            enrolledAt    = getLong("enrolledAt") ?: 0L
        )
    } catch (e: Exception) { null }
}