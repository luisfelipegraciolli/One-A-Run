package com.shiwa.onearun.data.repository

import com.shiwa.onearun.data.remote.MqttConnectionState
import com.shiwa.onearun.data.remote.MqttDataSource
import com.shiwa.onearun.data.remote.api.CreateRaceRequest
import com.shiwa.onearun.data.remote.api.CreateRaceResponse
import com.shiwa.onearun.data.remote.api.JoinRaceRequest
import com.shiwa.onearun.data.remote.api.JoinRaceResponse
import com.shiwa.onearun.data.remote.api.RaceApiService
import com.shiwa.onearun.data.remote.api.RaceDetailsResponse
import com.shiwa.onearun.data.remote.api.RaceDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RaceSessionState(
    val sessionId: String? = null,
    val joinCode: String? = null,
    val hostRunnerId: String? = null,
    val guestRunnerId: String? = null,
    val runnerId: String? = null,
    val distanceMeters: Int? = null,
    val status: String? = null,
    val details: RaceDetailsResponse? = null
)

interface RaceRepository {
    val connectionState: StateFlow<MqttConnectionState>
    val incomingMessages: Flow<String>
    val currentSession: StateFlow<RaceSessionState?>

    suspend fun connectToBroker()
    suspend fun subscribeToRaceTopic(topic: String = DEFAULT_RACE_TOPIC)
    suspend fun publishRaceMessage(message: String, topic: String = DEFAULT_RACE_TOPIC)
    suspend fun disconnect()

    // Retrofit API Methods (Existentes)
    suspend fun createRaceApi(race: RaceDto): Result<RaceDto>

    // Retrofit API Methods (Novos Endpoints)
    suspend fun createRaceSession(hostRunnerName: String, distanceInMeters: Int): Result<CreateRaceResponse>
    suspend fun createRaceSession(request: CreateRaceRequest): Result<CreateRaceResponse>
    suspend fun getRaceSession(sessionId: String): Result<RaceDetailsResponse>
    suspend fun joinRaceSession(guestRunnerName: String, joinCode: String): Result<JoinRaceResponse>
    suspend fun joinRaceSession(request: JoinRaceRequest): Result<JoinRaceResponse>

    companion object {
        const val DEFAULT_RACE_TOPIC = "corrida-app/test/hello"
    }
}

class RaceRepositoryImpl(
    private val mqttDataSource: MqttDataSource = MqttDataSource(),
    private val raceApiService: RaceApiService? = null,
) : RaceRepository {

    override val connectionState: StateFlow<MqttConnectionState> = mqttDataSource.connectionState
    override val incomingMessages: Flow<String> = mqttDataSource.incomingMessages

    private val _currentSession = MutableStateFlow<RaceSessionState?>(null)
    override val currentSession: StateFlow<RaceSessionState?> = _currentSession.asStateFlow()

    override suspend fun connectToBroker() {
        mqttDataSource.connect()
    }

    override suspend fun subscribeToRaceTopic(topic: String) {
        mqttDataSource.subscribe(topic)
    }

    override suspend fun publishRaceMessage(message: String, topic: String) {
        mqttDataSource.publish(topic, message)
    }

    override suspend fun disconnect() {
        mqttDataSource.disconnect()
    }

    override suspend fun createRaceApi(race: RaceDto): Result<RaceDto> {
        return runCatching {
            raceApiService?.createRace(race) ?: race
        }
    }

    override suspend fun createRaceSession(hostRunnerName: String, distanceInMeters: Int): Result<CreateRaceResponse> {
        return createRaceSession(CreateRaceRequest(
            distanceInMeters = distanceInMeters,
            hostRunnerName = hostRunnerName
        ))
    }

    override suspend fun createRaceSession(request: CreateRaceRequest): Result<CreateRaceResponse> {
        return runCatching {
            val service = raceApiService ?: throw IllegalStateException("RaceApiService não inicializado")
            val response = service.createRaceSession(request)
            _currentSession.update { current ->
                (current ?: RaceSessionState()).copy(
                    sessionId = response.sessionId,
                    joinCode = response.joinCode,
                    hostRunnerId = response.runnerId,
                    runnerId = response.runnerId,
                    distanceMeters = request.distanceInMeters
                )
            }
            response
        }
    }

    override suspend fun getRaceSession(sessionId: String): Result<RaceDetailsResponse> {
        return runCatching {
            val service = raceApiService ?: throw IllegalStateException("RaceApiService não inicializado")
            val response = service.getRaceSession(sessionId)
            _currentSession.update { current ->
                (current ?: RaceSessionState()).copy(
                    sessionId = response.id,
                    joinCode = response.joinCode,
                    hostRunnerId = response.hostRunnerId,
                    guestRunnerId = response.guestRunnerId,
                    distanceMeters = response.distanceMeters,
                    status = response.status,
                    details = response
                )
            }
            response
        }
    }

    override suspend fun joinRaceSession(guestRunnerName: String, joinCode: String): Result<JoinRaceResponse> {
        return joinRaceSession(JoinRaceRequest(
            joinCode = joinCode,
            guestRunnerName = guestRunnerName
        ))
    }

    override suspend fun joinRaceSession(request: JoinRaceRequest): Result<JoinRaceResponse> {
        return runCatching {
            val service = raceApiService ?: throw IllegalStateException("RaceApiService não inicializado")
            val response = service.joinRaceSession(request)
            _currentSession.update { current ->
                (current ?: RaceSessionState()).copy(
                    sessionId = response.sessionId,
                    joinCode = request.joinCode,
                    guestRunnerId = response.guestRunnerId,
                    runnerId = response.guestRunnerId,
                    distanceMeters = response.distanceMeters
                )
            }
            response
        }
    }
}
