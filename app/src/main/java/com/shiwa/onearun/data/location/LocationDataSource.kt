package com.shiwa.onearun.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
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
    @SerializedName("accuracy") val accuracy: Float
)

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
            .build()

        val locationCallback = object : LocationCallback() {
            private var lastEmittedLat: Double? = null
            private var lastEmittedLon: Double? = null

            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    if (location.hasAccuracy() && location.accuracy > 65.0f) {
                        Log.d(
                            "LocationDataSource",
                            "Ignorando localização imprecisa: acc=${location.accuracy}m"
                        )
                        continue
                    }

                    val lastLat = lastEmittedLat
                    val lastLon = lastEmittedLon

                    if (lastLat != null && lastLon != null) {
                        val distanceResults = FloatArray(1)
                        android.location.Location.distanceBetween(
                            lastLat, lastLon,
                            location.latitude, location.longitude,
                            distanceResults
                        )
                        val deltaMeters = distanceResults[0]
                        if (deltaMeters < 3.0f) {
                            Log.d(
                                "LocationDataSource",
                                "Ignorando oscilação GPS: delta=${deltaMeters}m < 3.0m"
                            )
                            continue
                        }
                    }

                    lastEmittedLat = location.latitude
                    lastEmittedLon = location.longitude

                    val update = LocationUpdate(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = location.time,
                        accuracy = location.accuracy
                    )
                    Log.d(
                        "LocationDataSource",
                        "Nova localização emitida: lat=${location.latitude}, lng=${location.longitude}, acc=${location.accuracy}m"
                    )
                    trySend(update)
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
