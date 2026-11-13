package com.shiwa.onearun.data.container

import android.content.Context
import com.shiwa.onearun.BuildConfig
import com.shiwa.onearun.data.location.LocationDataSource
import com.shiwa.onearun.data.remote.MqttDataSource
import com.shiwa.onearun.data.remote.api.RaceApiService
import com.shiwa.onearun.data.repository.RaceRepository
import com.shiwa.onearun.data.repository.RaceRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val raceRepository: RaceRepository
    val raceApiService: RaceApiService
    val locationDataSource: LocationDataSource
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val baseUrl = BuildConfig.BASE_URL

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    override val raceApiService: RaceApiService by lazy {
        retrofit.create(RaceApiService::class.java)
    }

    override val locationDataSource: LocationDataSource by lazy {
        LocationDataSource(context)
    }

    override val raceRepository: RaceRepository by lazy {
        RaceRepositoryImpl(
            mqttDataSource = MqttDataSource(
                host = "broker.emqx.io",
                port = 1883,
                clientIdPrefix = "corrida-app"
            ),
            raceApiService = raceApiService
        )
    }
}
