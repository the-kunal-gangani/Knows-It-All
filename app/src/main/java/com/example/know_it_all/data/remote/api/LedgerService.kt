package com.example.know_it_all.data.remote.api

import com.example.know_it_all.data.model.ApiResponse
import com.example.know_it_all.data.model.dto.LedgerEntryDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LedgerService {

    @GET("ledger/user/{userId}")
    suspend fun getUserLedger(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): ApiResponse<List<LedgerEntryDTO>>

    @GET("ledger/verify/{transactionId}")
    suspend fun verifyTransaction(
        @Header("Authorization") token: String,
        @Path("transactionId") transactionId: String
    ): ApiResponse<Unit>

    @GET("ledger/trust-score/{userId}")
    suspend fun getTrustScore(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): ApiResponse<Unit>

    @POST("ledger/dispute/{transactionId}")
    suspend fun disputeTransaction(
        @Header("Authorization") token: String,
        @Path("transactionId") transactionId: String,
        @Body reason: DisputeBody
    ): ApiResponse<Unit>
}

data class DisputeBody(val reason: String)
