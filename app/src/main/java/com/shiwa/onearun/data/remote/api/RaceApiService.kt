package com.shiwa.onearun.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class RaceDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("distanceMeters") val distanceMeters: Int? = null,
    @SerializedName("status") val status: String? = null
)

interface RaceApiService {
    @GET("race/{id}")
    suspend fun getRaceById(@Path("id") id: String): RaceDto

    @POST("race")
    suspend fun createRace(@Body race: RaceDto): RaceDto

    /**
     * Endpoint 2.1: Criar uma corrida
     * POST /api/v1/race
     */
    @POST("api/v1/race")
    suspend fun createRaceSession(@Body request: CreateRaceRequest): CreateRaceResponse

    /**
     * Endpoint 2.2: Consultar uma corrida
     * GET /api/v1/race/{sessionId}
     */
    @GET("api/v1/race/{sessionId}")
    suspend fun getRaceSession(@Path("sessionId") sessionId: String): RaceDetailsResponse

    /**
     * Endpoint 2.3: Entrar numa corrida existente
     * POST /api/v1/race/join
     */
    @POST("api/v1/race/join")
    suspend fun joinRaceSession(@Body request: JoinRaceRequest): JoinRaceResponse
}
