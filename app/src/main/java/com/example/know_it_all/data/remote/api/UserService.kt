package com.example.know_it_all.data.remote.api

import com.example.know_it_all.data.model.ApiResponse
import com.example.know_it_all.data.model.AuthData
import com.example.know_it_all.data.model.dto.UserDTO
import com.example.know_it_all.data.model.dto.UserLocationUpdate
import com.example.know_it_all.data.model.dto.UserLoginRequest
import com.example.know_it_all.data.model.dto.UserProfileUpdateRequest
import com.example.know_it_all.data.model.dto.UserRegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @POST("auth/register")
    suspend fun register(
        @Body request: UserRegisterRequest
    ): ApiResponse<AuthData>

    @POST("auth/login")
    suspend fun login(
        @Body request: UserLoginRequest
    ): ApiResponse<AuthData>

    @GET("users/profile/{userId}")
    suspend fun getUserProfile(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): ApiResponse<UserDTO>

    @PUT("users/profile/{userId}")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Body request: UserProfileUpdateRequest
    ): ApiResponse<UserDTO>

    @PUT("users/location/{userId}")
    suspend fun updateLocation(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Body location: UserLocationUpdate
    ): ApiResponse<Unit>

    @GET("users/nearby")
    suspend fun getNearbyUsers(
        @Header("Authorization") token: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusKm") radiusKm: Double = 5.0
    ): ApiResponse<List<UserDTO>>
}
