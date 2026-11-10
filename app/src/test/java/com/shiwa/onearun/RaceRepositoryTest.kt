package com.shiwa.onearun

import com.google.gson.Gson
import com.shiwa.onearun.data.remote.api.CreateRaceRequest
import com.shiwa.onearun.data.remote.api.CreateRaceResponse
import com.shiwa.onearun.data.remote.api.JoinRaceRequest
import com.shiwa.onearun.data.remote.api.JoinRaceResponse
import com.shiwa.onearun.data.remote.api.RaceApiService
import com.shiwa.onearun.data.remote.api.RaceDetailsResponse
import com.shiwa.onearun.data.remote.api.RaceDto
import com.shiwa.onearun.data.remote.api.RaceStatus
import com.shiwa.onearun.data.repository.RaceRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RaceRepositoryTest {

    private val gson = Gson()

    @Test
    fun testCreateRaceRequestAndResponseJsonSerialization() {
        val requestJson = gson.toJson(
            CreateRaceRequest(
                hostRunnerName = "John",
                distanceInMeters = 100
            )
        )
        assertTrue(requestJson.contains("distanceInMeters"))
        assertTrue(requestJson.contains("100"))

        val responseJson = """
            {
                "sessionId": "7110ee1d-3fdc-4f5e-8136-41a700c7b467",
                "joinCode": "BW7LYW",
                "runnerId": "987968a3-90ac-4232-9f06-5246a531f26f",
                "hostRunnerName": "John"
            }
        """.trimIndent()
        val response = gson.fromJson(responseJson, CreateRaceResponse::class.java)
        assertEquals("7110ee1d-3fdc-4f5e-8136-41a700c7b467", response.sessionId)
        assertEquals("BW7LYW", response.joinCode)
        assertEquals("987968a3-90ac-4232-9f06-5246a531f26f", response.runnerId)
    }

    @Test
    fun testRaceDetailsResponseJsonDeserialization() {
        val detailsJson = """
            {
                "id": "7110ee1d-3fdc-4f5e-8136-41a700c7b467",
                "joinCode": "BW7LYW",
                "distanceMeters": 100,
                "hostRunnerName": "John",
                "hostRunnerId": "987968a3-90ac-4232-9f06-5246a531f26f",
                "guestRunnerId": "4ae557ff-187b-4995-9d3a-bb32cca7ef46",
                "guestRunnerName": "Michael",
                "status": "READY",
                "createdAt": {
                    "epochSeconds": 1794169048,
                    "nanosecondsOfSecond": 147260524
                },
                "winnerRunnerId": null,
                "hostDistanceMeters": null,
                "guestDistanceMeters": null,
                "finishedAt": null,
                "version": 1
            }
        """.trimIndent()

        val response = gson.fromJson(detailsJson, RaceDetailsResponse::class.java)
        assertEquals("7110ee1d-3fdc-4f5e-8136-41a700c7b467", response.id)
        assertEquals("BW7LYW", response.joinCode)
        assertEquals(100, response.distanceMeters)
        assertEquals("987968a3-90ac-4232-9f06-5246a531f26f", response.hostRunnerId)
        assertEquals("4ae557ff-187b-4995-9d3a-bb32cca7ef46", response.guestRunnerId)
        assertEquals("READY", response.status)
        assertEquals(RaceStatus.READY, RaceStatus.fromString(response.status))
        assertNotNull(response.createdAt)
        assertEquals(1794169048L, response.createdAt?.epochSeconds)
        assertEquals(147260524L, response.createdAt?.nanosecondsOfSecond)
        assertNull(response.winnerRunnerId)
        assertNull(response.hostDistanceMeters)
        assertNull(response.guestDistanceMeters)
        assertNull(response.finishedAt)
        assertEquals(1, response.version)
    }

    @Test
    fun testJoinRaceRequestAndResponseJsonDeserialization() {
        val requestJson = gson.toJson(
            JoinRaceRequest(
                guestRunnerName = "Michael",
                joinCode = "BW7LYW"
            )
        )
        assertTrue(requestJson.contains("joinCode"))

        val responseJson = """
            {
                "sessionId": "7110ee1d-3fdc-4f5e-8136-41a700c7b467",
                "guestRunnerId": "4ae557ff-187b-4995-9d3a-bb32cca7ef46",
                "distanceMeters": 100,
                "guestRunnerName": "Michael"
            }
        """.trimIndent()
        val response = gson.fromJson(responseJson, JoinRaceResponse::class.java)
        assertEquals("7110ee1d-3fdc-4f5e-8136-41a700c7b467", response.sessionId)
        assertEquals("4ae557ff-187b-4995-9d3a-bb32cca7ef46", response.guestRunnerId)
        assertEquals(100, response.distanceMeters)
    }

    @Test
    fun testRepositoryCreateRaceSession() = runBlocking {
        val mockApiService = object : FakeRaceApiService() {
            override suspend fun createRaceSession(request: CreateRaceRequest): CreateRaceResponse {
                return CreateRaceResponse(
                    sessionId = "session-123",
                    joinCode = "CODE12",
                    runnerId = "host-456",
                    hostRunnerName = "John"
                )
            }
        }

        val repository = RaceRepositoryImpl(raceApiService = mockApiService)
        val request = CreateRaceRequest(hostRunnerName = "John", distanceInMeters = 100)
        val result = repository.createRaceSession(request)

        assertTrue(result.isSuccess)
        val createResponse = result.getOrNull()
        assertNotNull(createResponse)
        assertEquals("session-123", createResponse?.sessionId)
        assertEquals("CODE12", createResponse?.joinCode)
        assertEquals("host-456", createResponse?.runnerId)

        val sessionState = repository.currentSession.value
        assertNotNull(sessionState)
        assertEquals("session-123", sessionState?.sessionId)
        assertEquals("CODE12", sessionState?.joinCode)
        assertEquals("host-456", sessionState?.hostRunnerId)
        assertEquals("host-456", sessionState?.runnerId)
        assertEquals(100, sessionState?.distanceMeters)
    }

    @Test
    fun testRepositoryGetRaceSession() = runBlocking {
        val mockApiService = object : FakeRaceApiService() {
            override suspend fun getRaceSession(sessionId: String): RaceDetailsResponse {
                return RaceDetailsResponse(
                    id = sessionId,
                    joinCode = "CODE12",
                    distanceMeters = 200,
                    hostRunnerId = "host-1",
                    hostRunnerName = "John",
                    guestRunnerId = "guest-2",
                    guestRunnerName = "Michael",
                    status = "READY"
                )
            }
        }

        val repository = RaceRepositoryImpl(raceApiService = mockApiService)
        val result = repository.getRaceSession("session-999")

        assertTrue(result.isSuccess)
        val details = result.getOrNull()
        assertNotNull(details)
        assertEquals("session-999", details?.id)
        assertEquals("READY", details?.status)

        val sessionState = repository.currentSession.value
        assertEquals("session-999", sessionState?.sessionId)
        assertEquals("guest-2", sessionState?.guestRunnerId)
        assertEquals(200, sessionState?.distanceMeters)
    }

    @Test
    fun testRepositoryJoinRaceSession() = runBlocking {
        val mockApiService = object : FakeRaceApiService() {
            override suspend fun joinRaceSession(request: JoinRaceRequest): JoinRaceResponse {
                return JoinRaceResponse(
                    sessionId = "session-777",
                    guestRunnerId = "guest-888",
                    distanceMeters = 500,
                    guestRunnerName = "Michael"
                )
            }
        }

        val repository = RaceRepositoryImpl(raceApiService = mockApiService)
        val request = JoinRaceRequest(guestRunnerName = "Michael", joinCode = "BW7LYW")
        val result = repository.joinRaceSession(request)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals("session-777", response?.sessionId)
        assertEquals("guest-888", response?.guestRunnerId)
        assertEquals(500, response?.distanceMeters)

        val sessionState = repository.currentSession.value
        assertEquals("session-777", sessionState?.sessionId)
        assertEquals("BW7LYW", sessionState?.joinCode)
        assertEquals("guest-888", sessionState?.guestRunnerId)
        assertEquals(500, sessionState?.distanceMeters)
    }
}

open class FakeRaceApiService : RaceApiService {
    override suspend fun getRaceById(id: String): RaceDto = RaceDto()
    override suspend fun createRace(race: RaceDto): RaceDto = race
    override suspend fun createRaceSession(request: CreateRaceRequest): CreateRaceResponse = throw NotImplementedError()
    override suspend fun getRaceSession(sessionId: String): RaceDetailsResponse = throw NotImplementedError()
    override suspend fun joinRaceSession(request: JoinRaceRequest): JoinRaceResponse = throw NotImplementedError()
}
