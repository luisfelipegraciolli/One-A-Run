package com.shiwa.onearun.data.container

import com.shiwa.onearun.data.remote.MqttDataSource
import com.shiwa.onearun.data.remote.api.RaceApiService
import com.shiwa.onearun.data.repository.RaceRepository
import com.shiwa.onearun.data.repository.RaceRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val raceRepository: RaceRepository
    val raceApiService: RaceApiService
}

class DefaultAppContainer : AppContainer {
    // Esse é meu IP da rede local, caso queira testar o backend, use seu IP
    //private val baseUrl = "http://seu_ip:8080/"
    private val baseUrl = "http://192.168.1.20:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    override val raceApiService: RaceApiService by lazy {
        retrofit.create(RaceApiService::class.java)
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
