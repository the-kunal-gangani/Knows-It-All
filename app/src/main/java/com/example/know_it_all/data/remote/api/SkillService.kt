package com.example.know_it_all.data.remote.api

import com.example.know_it_all.data.model.ApiResponse
import com.example.know_it_all.data.model.dto.SkillCreateRequest
import com.example.know_it_all.data.model.dto.SkillDTO
import com.example.know_it_all.data.model.dto.SkillEndorseRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SkillService {

    @POST("skills")
    suspend fun addSkill(
        @Header("Authorization") token: String,
        @Body request: SkillCreateRequest
    ): ApiResponse<SkillDTO>

    @GET("skills/user/{userId}")
    suspend fun getUserSkills(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): ApiResponse<List<SkillDTO>>

    @GET("skills/search")
    suspend fun searchSkills(
        @Header("Authorization") token: String,
        @Query("query") query: String,
        @Query("category") category: String? = null
    ): ApiResponse<List<SkillDTO>>

    @PUT("skills/{skillId}")
    suspend fun updateSkill(
        @Header("Authorization") token: String,
        @Path("skillId") skillId: String,
        @Body request: SkillCreateRequest
    ): ApiResponse<SkillDTO>

    @DELETE("skills/{skillId}")
    suspend fun deleteSkill(
        @Header("Authorization") token: String,
        @Path("skillId") skillId: String
    ): ApiResponse<Unit>

    @GET("skills/recommendations")
    suspend fun getRecommendedSkills(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): ApiResponse<List<SkillDTO>>

    @POST("skills/{skillId}/endorse")
    suspend fun endorseSkill(
        @Header("Authorization") token: String,
        @Path("skillId") skillId: String,
        @Body request: SkillEndorseRequest
    ): ApiResponse<Unit>
}
