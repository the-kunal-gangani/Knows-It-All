package com.example.know_it_all.data.remote.api

import com.example.know_it_all.data.model.ApiResponse
import com.example.know_it_all.data.model.dto.SwapDTO
import com.example.know_it_all.data.model.dto.SwapRatingRequest
import com.example.know_it_all.data.model.dto.SwapRequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SwapService {

    @POST("swap/request")
    suspend fun requestSwap(
        @Header("Authorization") token: String,
        @Body request: SwapRequestBody
    ): ApiResponse<SwapDTO>

    @GET("swap/{swapId}")
    suspend fun getSwapDetails(
        @Header("Authorization") token: String,
        @Path("swapId") swapId: String
    ): ApiResponse<SwapDTO>

    @PUT("swap/{swapId}/accept")
    suspend fun acceptSwap(
        @Header("Authorization") token: String,
        @Path("swapId") swapId: String
    ): ApiResponse<SwapDTO>

    @PUT("swap/{swapId}/complete")
    suspend fun completeSwap(
        @Header("Authorization") token: String,
        @Path("swapId") swapId: String
    ): ApiResponse<SwapDTO>

    @DELETE("swap/{swapId}")
    suspend fun cancelSwap(
        @Header("Authorization") token: String,
        @Path("swapId") swapId: String
    ): ApiResponse<Unit>

    @POST("swap/{swapId}/rating")
    suspend fun rateSwap(
        @Header("Authorization") token: String,
        @Path("swapId") swapId: String,
        @Body request: SwapRatingRequest
    ): ApiResponse<Unit>

    @GET("swap/user/active")
    suspend fun getActiveSwaps(
        @Header("Authorization") token: String
    ): ApiResponse<List<SwapDTO>>

    @GET("swap/user/history")
    suspend fun getSwapHistory(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): ApiResponse<List<SwapDTO>>
}
