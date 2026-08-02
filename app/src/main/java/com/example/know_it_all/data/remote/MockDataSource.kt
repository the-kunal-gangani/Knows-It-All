package com.example.know_it_all.data.remote

import com.example.know_it_all.data.model.AuthData
import com.example.know_it_all.data.model.SwapStatus
import com.example.know_it_all.data.model.SwapType
import com.example.know_it_all.data.model.VerificationMethod
import com.example.know_it_all.data.model.dto.SwapDTO
import com.example.know_it_all.data.model.dto.UserDTO

object MockDataSource {

    fun login(email: String, password: String): Result<AuthData> {
        return if (email.isNotEmpty() && password.length >= 6) {
            Result.success(
                AuthData(
                    token = "mock_token_${System.currentTimeMillis()}",
                    userId = "mock_user_001",
                    name = "Demo User"
                )
            )
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    fun register(name: String, email: String, password: String): Result<AuthData> {
        return if (name.isNotEmpty() && email.isNotEmpty() && password.length >= 6) {
            Result.success(
                AuthData(
                    token = "mock_token_${System.currentTimeMillis()}",
                    userId = "mock_user_${System.currentTimeMillis()}",
                    name = name
                )
            )
        } else {
            Result.failure(Exception("Registration failed: all fields required, password min 6 chars"))
        }
    }

    fun getNearbyUsers(): List<UserDTO> {
        return listOf(
            UserDTO(
                uid = "user_001",
                name = "Rahul Sharma",
                email = "rahul@example.com",
                latitude = 18.6298,
                longitude = 73.7997,
                trustScore = 4.5f,
                skillTokenBalance = 120L,
                profileVerified = true,
                isOnline = true,
                createdAt = 1_700_000_000_000L,
                updatedAt = System.currentTimeMillis()
            ),
            UserDTO(
                uid = "user_002",
                name = "Priya Patel",
                email = "priya@example.com",
                latitude = 18.6350,
                longitude = 73.8050,
                trustScore = 4.8f,
                skillTokenBalance = 340L,
                profileVerified = true,
                isOnline = true,
                createdAt = 1_700_000_000_000L,
                updatedAt = System.currentTimeMillis()
            ),
            UserDTO(
                uid = "user_003",
                name = "Amit Kumar",
                email = "amit@example.com",
                latitude = 18.6250,
                longitude = 73.7950,
                trustScore = 3.9f,
                skillTokenBalance = 55L,
                profileVerified = false,
                isOnline = false,
                createdAt = 1_700_000_000_000L,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun getActiveSwaps(): List<SwapDTO> {
        return listOf(
            SwapDTO(
                swapId = "swap_001",
                mentorId = "user_001",
                learnerId = "mock_user_001",
                mentorName = "Rahul Sharma",
                learnerName = "Demo User",
                skillName = "Python Programming",
                mentorSkillId = "skill_uuid_001",
                learnerSkillId = null,
                swapType = SwapType.TOKEN,
                tokenAmount = 10L,
                status = SwapStatus.ACTIVE,
                verificationMethod = VerificationMethod.VIDEO_CALL,
                sessionStartTime = System.currentTimeMillis() - 1_800_000L
            ),
            SwapDTO(
                swapId = "swap_002",
                mentorId = "user_002",
                learnerId = "mock_user_001",
                mentorName = "Priya Patel",
                learnerName = "Demo User",
                skillName = "Graphic Design",
                mentorSkillId = "skill_uuid_002",
                learnerSkillId = "skill_uuid_learner_001",
                swapType = SwapType.BARTER,
                tokenAmount = 0L,
                status = SwapStatus.REQUESTED,
                verificationMethod = VerificationMethod.NONE
            )
        )
    }

    fun getSwapHistory(): List<SwapDTO> {
        return listOf(
            SwapDTO(
                swapId = "swap_old_001",
                mentorId = "user_003",
                learnerId = "mock_user_001",
                mentorName = "Amit Kumar",
                learnerName = "Demo User",
                skillName = "Excel & Data Analysis",
                mentorSkillId = "skill_uuid_003",
                learnerSkillId = null,
                swapType = SwapType.TOKEN,
                tokenAmount = 5L,
                status = SwapStatus.COMPLETED,
                verificationMethod = VerificationMethod.QR_HANDSHAKE,
                sessionStartTime = System.currentTimeMillis() - 86_400_000L,
                sessionEndTime = System.currentTimeMillis() - 82_800_000L
            )
        )
    }
}
