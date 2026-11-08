package com.shiwa.onearun.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class RaceDto(
    val id: String? = null,
    val name: String? = null,
    val distanceMeters: Int? = null,
    val status: String? = null
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
     * Endpoint 2.3: Entrar em uma corrida existente
     * POST /api/v1/race/join
     */
    @POST("api/v1/race/join")
    suspend fun joinRaceSession(@Body request: JoinRaceRequest): JoinRaceResponse
}
