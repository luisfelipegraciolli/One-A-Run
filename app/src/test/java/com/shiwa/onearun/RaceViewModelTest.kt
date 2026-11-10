package com.shiwa.onearun

import com.shiwa.onearun.race.ProgressPoint
import com.shiwa.onearun.race.RaceUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RaceViewModelTest {

    @Test
    fun testFormatTimeMinutesAndSeconds() {
        assertEquals("00:00", RaceUtils.formatTime(0L))
        assertEquals("02:23", RaceUtils.formatTime(143_000L))
        assertEquals("59:59", RaceUtils.formatTime(3_599_000L))
    }

    @Test
    fun testFormatTimeHours() {
        assertEquals("1:00:00", RaceUtils.formatTime(3_600_000L))
        assertEquals("1:05:30", RaceUtils.formatTime(3_930_000L))
    }

    @Test
    fun testFormatPace() {
        assertEquals("--:--", RaceUtils.formatPace(null))
        assertEquals("5:30", RaceUtils.formatPace(330))
        assertEquals("6:05", RaceUtils.formatPace(365))
        assertEquals("10:00", RaceUtils.formatPace(600))
    }

    @Test
    fun testCalculatePaceNormalRange() {
        // Exemplo: 50m percorridos em 10s -> 5m/s -> 200 s/km (3:20 min/km)
        val now = 10_000L
        val history = listOf(
            ProgressPoint(distanceMeters = 100.0, timestampMillis = 0L),
            ProgressPoint(distanceMeters = 150.0, timestampMillis = 10_000L)
        )

        val pace = RaceUtils.calculatePace(history, now, windowMillis = 10_000L)
        assertEquals(200, pace)
    }

    @Test
    fun testCalculatePaceInsufficientDistanceReturnsNull() {
        // Menos de 10m percorridos em 10s -> deve retornar null
        val now = 10_000L
        val history = listOf(
            ProgressPoint(distanceMeters = 100.0, timestampMillis = 0L),
            ProgressPoint(distanceMeters = 105.0, timestampMillis = 10_000L)
        )

        val pace = RaceUtils.calculatePace(history, now, windowMillis = 10_000L)
        assertNull(pace)
    }

    @Test
    fun testCalculatePaceOutOfBoundsReturnsNull() {
        // 5m000m (5km) em 10s -> 500m/s -> 2s/km (abaixo de 120s / 2:00 min/km) -> deve retornar null
        val now = 10_000L
        val historyFast = listOf(
            ProgressPoint(distanceMeters = 0.0, timestampMillis = 0L),
            ProgressPoint(distanceMeters = 5000.0, timestampMillis = 10_000L)
        )
        assertNull(RaceUtils.calculatePace(historyFast, now, windowMillis = 10_000L))

        // 11m em 10.000s -> super lento (acima de 20:00 min/km / 1200s) -> deve retornar null
        val historySlow = listOf(
            ProgressPoint(distanceMeters = 0.0, timestampMillis = 0L),
            ProgressPoint(distanceMeters = 11.0, timestampMillis = 1000_000L)
        )
        assertNull(RaceUtils.calculatePace(historySlow, 1000_000L, windowMillis = 1000_000L))
    }

    @Test
    fun testCalculateIsFirstPlaceBeforeWinner() {
        // Sem vencedor definido ainda: quem tem maior distância ganha (empate -> local player em 1º)
        assertTrue(RaceUtils.calculateIsFirstPlace(150.0, 100.0, won = null, winnerRunnerId = null))
        assertTrue(RaceUtils.calculateIsFirstPlace(100.0, 100.0, won = null, winnerRunnerId = null))
        assertFalse(RaceUtils.calculateIsFirstPlace(80.0, 100.0, won = null, winnerRunnerId = null))
    }

    @Test
    fun testCalculateIsFirstPlaceAfterWinner() {
        // Com vencedor definido: depende da flag won
        assertTrue(
            RaceUtils.calculateIsFirstPlace(
                100.0,
                150.0,
                won = true,
                winnerRunnerId = "my-runner-id"
            )
        )
        assertFalse(
            RaceUtils.calculateIsFirstPlace(
                150.0,
                100.0,
                won = false,
                winnerRunnerId = "opponent-id"
            )
        )
    }
}
