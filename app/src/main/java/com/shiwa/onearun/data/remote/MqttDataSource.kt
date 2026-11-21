package com.shiwa.onearun.data.remote

import android.util.Log
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.TimeUnit

sealed interface MqttConnectionState {
    object Disconnected : MqttConnectionState
    object Connecting : MqttConnectionState
    object Connected : MqttConnectionState
    data class Error(val message: String) : MqttConnectionState
}

class MqttDataSource(
    private val host: String = "broker.emqx.io",
    private val port: Int = 8883, // Porta TLS: evita bloqueios/drops de CGNAT em redes 4G/5G
    private val useSsl: Boolean = true,
    private val clientId: String = "onearun-android-${UUID.randomUUID()}" // ID fixo por instância
) {
    private val tag = "MqttDataSource"
    private var client: Mqtt5AsyncClient? = null

    private val _connectionState = MutableStateFlow<MqttConnectionState>(MqttConnectionState.Disconnected)
    val connectionState: StateFlow<MqttConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    suspend fun connect() = withContext(Dispatchers.IO) {
        if (_connectionState.value == MqttConnectionState.Connected && client != null) {
            return@withContext
        }

        _connectionState.value = MqttConnectionState.Connecting
        Log.d(tag, "Connecting to $host:$port with clientId $clientId...")

        try {
            val builder = Mqtt5Client.builder()
                .identifier(clientId)
                .serverHost(host)
                .serverPort(port)
                // Reconexão automática nativa do HiveMQ quando o 4G/Wi-Fi oscilar
                .automaticReconnect()
                .initialDelay(1, TimeUnit.SECONDS)
                .maxDelay(15, TimeUnit.SECONDS)
                .applyAutomaticReconnect()
                .addConnectedListener {
                    Log.d(tag, "MQTT Connected / Reconnected!")
                    _connectionState.value = MqttConnectionState.Connected
                }
                .addDisconnectedListener { context ->
                    Log.w(tag, "MQTT Disconnected: ${context.cause.message}")
                    _connectionState.value = MqttConnectionState.Disconnected
                }

            if (useSsl) {
                builder.sslWithDefaultConfig()
            }

            val mqttClient = builder.buildAsync()

            mqttClient.toBlocking().connectWith()
                .cleanStart(false)
                // Mantém a sessão e inscrições ativas no broker por 5 min durante quedas de rede
                .sessionExpiryInterval(300)
                .keepAlive(20) // Ping a cada 20s para evitar timeout de NAT em redes móveis
                .send()

            client = mqttClient
            _connectionState.value = MqttConnectionState.Connected

        } catch (e: Exception) {
            client = null
            val errMsg = e.localizedMessage ?: "Connect exception"
            Log.e(tag, "Exception during MQTT connect to $host:$port: $errMsg", e)
            _connectionState.value = MqttConnectionState.Error(errMsg)
        }
    }

    fun observeTopic(topic: String): Flow<String> = callbackFlow {
        val activeClient = client
        if (activeClient == null) {
            close(IllegalStateException("MQTT client is null"))
            return@callbackFlow
        }

        activeClient.subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { publish: Mqtt5Publish ->
                trySend(String(publish.payloadAsBytes, StandardCharsets.UTF_8))
            }
            .send()
            .whenComplete { _, error ->
                if (error != null) {
                    Log.w(tag, "Failed to subscribe to [$topic]", error)
                    close(error)
                } else {
                    Log.d(tag, "Subscribed to [$topic]")
                }
            }

        awaitClose {
            runCatching {
                activeClient.unsubscribeWith().topicFilter(topic).send()
            }
        }
    }

    suspend fun publish(topic: String, payload: String) = withContext(Dispatchers.IO) {
        if (_connectionState.value != MqttConnectionState.Connected || client == null) {
            connect()
        }

        val activeClient = client ?: return@withContext
        try {
            activeClient.toBlocking().publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(payload.toByteArray(StandardCharsets.UTF_8))
                .send()
        } catch (e: Exception) {
            Log.e(tag, "Exception during publish to [$topic]", e)
        }
    }

    suspend fun publishQos0(topic: String, payload: String) = withContext(Dispatchers.IO) {
        if (client == null) {
            connect()
        }
        val activeClient = client ?: return@withContext
        try {
            activeClient.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_MOST_ONCE) // QoS 0 real: ideal para telemetria contínua de GPS
                .payload(payload.toByteArray(StandardCharsets.UTF_8))
                .send()
        } catch (e: Exception) {
            Log.e(tag, "Exception during async publish to [$topic]", e)
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            client?.toBlocking()?.disconnectWith()
                ?.sessionExpiryInterval(0) // Encerra a sessão limpa ao sair da tela
                ?.send()
        } catch (e: Exception) {
            Log.e(tag, "Exception during disconnect", e)
        } finally {
            _connectionState.value = MqttConnectionState.Disconnected
            client = null
        }
    }
}