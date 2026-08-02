package com.example.know_it_all.data.repository

import com.example.know_it_all.data.model.Skill
import com.example.know_it_all.data.model.SkillCategory
import com.example.know_it_all.data.model.ProficiencyLevel
import com.example.know_it_all.data.model.dto.SkillCreateRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseSkillRepository {

    private val db = FirebaseFirestore.getInstance()
    private val skillsCollection = db.collection("skills")

    // ── Observe skills (real-time) ────────────────────────────────────────────

    fun observeUserSkills(userId: String): Flow<List<Skill>> = callbackFlow {
        val listener = skillsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val skills = snapshot?.documents
                    ?.mapNotNull { it.toSkill() }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                trySend(skills)
            }
        awaitClose { listener.remove() }
    }

    // ── One-shot reads ────────────────────────────────────────────────────────

    suspend fun getUserSkills(userId: String): Result<List<Skill>> {
        return try {
            val snapshot = skillsCollection
                .whereEqualTo("userId", userId)
                .get().await()
            val skills = snapshot.documents
                .mapNotNull { it.toSkill() }
                .sortedByDescending { it.createdAt }
            Result.success(skills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSkillById(skillId: String): Result<Skill> {
        return try {
            val snapshot = skillsCollection.document(skillId).get().await()
            val skill = snapshot.toSkill() ?: throw Exception("Skill not found")
            Result.success(skill)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchSkills(query: String, category: String? = null): Result<List<Skill>> {
        return try {
            // Firestore doesn't support full-text search
            // Fetch all and filter client-side for now
            // For production use Algolia or Firebase Extensions
            var firestoreQuery = skillsCollection
                .orderBy("skillName")
            
            val snapshot = firestoreQuery.get().await()
            val skills = snapshot.documents
                .mapNotNull { it.toSkill() }
                .filter { skill ->
                    skill.skillName.contains(query, ignoreCase = true) &&
                    (category == null || skill.category.name == category)
                }
            Result.success(skills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Writes ────────────────────────────────────────────────────────────────

    suspend fun addSkill(userId: String, request: SkillCreateRequest): Result<Skill> {
        return try {
            val skill = Skill(
                skillId          = UUID.randomUUID().toString(),
                userId           = userId,
                skillName        = request.skillName,
                description      = request.description,
                category         = runCatching {
                    SkillCategory.valueOf(request.category.uppercase())
                }.getOrDefault(SkillCategory.DIGITAL),
                proficiencyLevel = runCatching {
                    ProficiencyLevel.valueOf(request.proficiencyLevel.uppercase())
                }.getOrDefault(ProficiencyLevel.BEGINNER),
                tokenValue         = request.tokenValue,
                verificationStatus = false,
                endorsements       = 0,
                createdAt          = System.currentTimeMillis()
            )
            skillsCollection.document(skill.skillId).set(skill.toMap()).await()
            
            // Record streak activity
            StreakRepository().recordActivity(userId)
            
            Result.success(skill)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSkill(
        skillId: String,
        request: SkillCreateRequest
    ): Result<Unit> {
        return try {
            skillsCollection.document(skillId).update(
                mapOf(
                    "skillName"        to request.skillName,
                    "description"      to request.description,
                    "proficiencyLevel" to request.proficiencyLevel.uppercase(),
                    "tokenValue"       to request.tokenValue
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSkill(skillId: String): Result<Unit> {
        return try {
            skillsCollection.document(skillId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun endorseSkill(skillId: String): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                val ref      = skillsCollection.document(skillId)
                val snapshot = transaction.get(ref)
                val current  = snapshot.getLong("endorsements") ?: 0
                transaction.update(ref, "endorsements", current + 1)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ── Extensions ────────────────────────────────────────────────────────────────

private fun com.google.firebase.firestore.DocumentSnapshot.toSkill(): Skill? {
    return try {
        Skill(
            skillId          = getString("skillId") ?: id,
            userId           = getString("userId") ?: "",
            skillName        = getString("skillName") ?: "",
            description      = getString("description") ?: "",
            category         = runCatching {
                SkillCategory.valueOf(getString("category") ?: "DIGITAL")
            }.getOrDefault(SkillCategory.DIGITAL),
            proficiencyLevel = runCatching {
                ProficiencyLevel.valueOf(getString("proficiencyLevel") ?: "BEGINNER")
            }.getOrDefault(ProficiencyLevel.BEGINNER),
            tokenValue         = getLong("tokenValue")?.toInt() ?: 10,
            verificationStatus = getBoolean("verificationStatus") ?: false,
            endorsements       = getLong("endorsements")?.toInt() ?: 0,
            createdAt          = getLong("createdAt") ?: 0L
        )
    } catch (e: Exception) { null }
}

private fun Skill.toMap(): Map<String, Any?> = mapOf(
    "skillId"          to skillId,
    "userId"           to userId,
    "skillName"        to skillName,
    "description"      to description,
    "category"         to category.name,
    "proficiencyLevel" to proficiencyLevel.name,
    "tokenValue"       to tokenValue,
    "verificationStatus" to verificationStatus,
    "endorsements"     to endorsements,
    "createdAt"        to createdAt
)