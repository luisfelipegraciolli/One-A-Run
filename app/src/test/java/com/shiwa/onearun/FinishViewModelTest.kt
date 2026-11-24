package com.shiwa.onearun

import com.shiwa.onearun.data.repository.LocationPoint
import com.shiwa.onearun.data.repository.RaceRepository
import com.shiwa.onearun.data.repository.RaceStats
import com.shiwa.onearun.finish.FinishViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FinishViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: RaceRepository = mockk(relaxed = true)
    private val statsFlow = MutableStateFlow<RaceStats?>(null)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.raceStats } returns statsFlow
        every { repository.currentSession } returns MutableStateFlow(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testFinishViewModelInitialWinState() = runTest {
        val samplePoints = listOf(
            LocationPoint(-23.550520, -46.633308),
            LocationPoint(-23.554520, -46.633308)
        )
        val stats = RaceStats(
            bestPaceSecPerKm = 312, // 5:12 min/km
            myFinishMillis = 201_000L, // 3:21 min
            myDistanceMeters = 500.0,
            won = true,
            routePoints = samplePoints
        )
        statsFlow.value = stats

        val viewModel = FinishViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.isWinner)
        assertEquals("5:12min/km", uiState.topSpeedText)
        assertEquals("500m", uiState.distanceText)
        assertEquals("03:21", uiState.elapsedTimeText)
        assertEquals(2, uiState.routePoints.size)
    }

    @Test
    fun testFinishViewModelLossState() = runTest {
        val stats = RaceStats(
            bestPaceSecPerKm = 420, // 7:00 min/km
            myFinishMillis = 241_000L, // 4:01 min
            myDistanceMeters = 500.0,
            won = false
        )
        statsFlow.value = stats

        val viewModel = FinishViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isWinner)
        assertEquals("7:00min/km", uiState.topSpeedText)
        assertEquals("500m", uiState.distanceText)
        assertEquals("04:01", uiState.elapsedTimeText)
    }
}
