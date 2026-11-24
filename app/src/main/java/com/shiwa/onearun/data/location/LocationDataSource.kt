package com.shiwa.onearun.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.annotation.Keep
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Keep
data class LocationUpdate(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("accuracy") val accuracy: Float,
    @SerializedName("speed") val speed: Float? = null

)

private const val MAX_ACCURACY_M = 20f
private const val MAX_AGE_MS = 5_000L
private const val MIN_DELTA_M = 3f
private const val STATIONARY_SPEED_MS = 0.8f

class LocationDataSource(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
        context
    )
) {
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<LocationUpdate> = callbackFlow {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1500L)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(2.0f)
            .setWaitForAccurateLocation(true)
            .build()

        val locationCallback = object : LocationCallback() {
            private var lastEmittedLat: Double? = null
            private var lastEmittedLon: Double? = null

            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    // 1) descarta fix velho (cache do Fused)
                    val ageMs =
                        (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000
                    if (ageMs > MAX_AGE_MS) continue

                    // 2) descarta fix impreciso (inclusive sem accuracy)
                    if (!location.hasAccuracy() || location.accuracy > MAX_ACCURACY_M) {
                        Log.d(
                            "LocationDataSource",
                            "Ignorando fix impreciso: acc=${location.accuracy}m"
                        )
                        continue
                    }

                    val speed = if (location.hasSpeed()) location.speed else null
                    val lastLat = lastEmittedLat
                    val lastLon = lastEmittedLon

                    if (lastLat != null && lastLon != null) {
                        val r = FloatArray(1)
                        android.location.Location.distanceBetween(
                            lastLat, lastLon, location.latitude, location.longitude, r
                        )
                        // 3) o deslocamento precisa sair do "círculo de erro" do fix
                        val gate = maxOf(MIN_DELTA_M, location.accuracy)
                        if (r[0] < gate) continue

                        // 4) parado segundo o Doppler: é ruído
                        if (speed != null && speed < STATIONARY_SPEED_MS) continue
                    }

                    lastEmittedLat = location.latitude
                    lastEmittedLon = location.longitude

                    trySend(
                        LocationUpdate(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestamp = location.time,
                            accuracy = location.accuracy,
                            speed = speed
                        )
                    )
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            ).addOnFailureListener { e ->
                Log.e("LocationDataSource", "Erro ao solicitar atualizações de localização GPS", e)
                close(e)
            }
        } catch (e: Exception) {
            Log.e("LocationDataSource", "Exceção ao iniciar atualizações de localização GPS", e)
            close(e)
        }

        awaitClose {
            runCatching {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }
}
