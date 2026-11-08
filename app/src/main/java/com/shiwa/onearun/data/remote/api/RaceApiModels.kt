package com.shiwa.onearun.data.remote.api

import com.google.gson.annotations.SerializedName

/**
 * Body enviado para criar uma corrida.
 */
data class CreateRaceRequest(
    @SerializedName("distanceInMeters")
    val distanceInMeters: Int
)

/**
 * Resposta esperada ao criar uma corrida.
 */
data class CreateRaceResponse(
    @SerializedName("sessionId")
    val sessionId: String,
    @SerializedName("joinCode")
    val joinCode: String,
    @SerializedName("runnerId")
    val runnerId: String
)

/**
 * Body enviado para entrar em uma corrida existente.
 */
data class JoinRaceRequest(
    @SerializedName("joinCode")
    val joinCode: String
)

/**
 * Resposta esperada ao entrar em uma corrida.
 */
data class JoinRaceResponse(
    @SerializedName("sessionId")
    val sessionId: String,
    @SerializedName("guestRunnerId")
    val guestRunnerId: String,
    @SerializedName("distanceMeters")
    val distanceMeters: Int
)

/**
 * Representação dos dados de data e horário retornados pelo servidor.
 */
data class RaceTimestampDto(
    @SerializedName("epochSeconds")
    val epochSeconds: Long? = null,
    @SerializedName("nanosecondsOfSecond")
    val nanosecondsOfSecond: Long? = null
)

/**
 * Resposta completa com informações de uma sessão de corrida.
 */
data class RaceDetailsResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("joinCode")
    val joinCode: String,
    @SerializedName("distanceMeters")
    val distanceMeters: Int,
    @SerializedName("hostRunnerId")
    val hostRunnerId: String,
    @SerializedName("guestRunnerId")
    val guestRunnerId: String? = null,
    @SerializedName("status")
    val status: String,
    @SerializedName("createdAt")
    val createdAt: RaceTimestampDto? = null,
    @SerializedName("winnerRunnerId")
    val winnerRunnerId: String? = null,
    @SerializedName("hostDistanceMeters")
    val hostDistanceMeters: Int? = null,
    @SerializedName("guestDistanceMeters")
    val guestDistanceMeters: Int? = null,
    @SerializedName("finishedAt")
    val finishedAt: RaceTimestampDto? = null,
    @SerializedName("version")
    val version: Int? = null
)

/**
 * Estados possíveis da corrida retornados pelo backend.
 */
enum class RaceStatus {
    CREATED,
    WAITING,
    READY,
    RUNNING,
    IN_PROGRESS,
    FINISHED,
    CANCELLED,
    UNKNOWN;

    companion object {
        fun fromString(status: String?): RaceStatus {
            if (status == null) return UNKNOWN
            return entries.firstOrNull { it.name.equals(status, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
