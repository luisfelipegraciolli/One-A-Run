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

sealed interface MqttConnectionState {
    object Disconnected : MqttConnectionState
    object Connecting : MqttConnectionState
    object Connected : MqttConnectionState
    data class Error(val message: String) : MqttConnectionState
}

class MqttDataSource(
    private val host: String = "test.mosquitto.org",
    private val port: Int = 1883,
    private val clientIdPrefix: String = "corrida-android-"
) {
    private val tag = "MqttDataSource"
    private var client: Mqtt5AsyncClient? = null

    private val _connectionState = MutableStateFlow<MqttConnectionState>(MqttConnectionState.Disconnected)
    val connectionState: StateFlow<MqttConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    suspend fun connect() = withContext(Dispatchers.IO) {
        if (_connectionState.value == MqttConnectionState.Connected && client != null) {
            Log.d(tag, "Already connected to $host:$port")
            return@withContext
        }

        _connectionState.value = MqttConnectionState.Connecting
        val clientId = clientIdPrefix + System.currentTimeMillis()
        Log.d(tag, "Connecting to $host:$port with clientId $clientId...")

        try {
            val mqttClient = Mqtt5Client.builder()
                .identifier(clientId)
                .serverHost(host)
                .serverPort(port)
                .buildAsync()

            client = mqttClient

            val connAck = mqttClient.toBlocking().connectWith()
                .cleanStart(true)
                .send()

            Log.d(tag, "MQTT Connected successfully to $host:$port! ConnAck: $connAck")
            _connectionState.value = MqttConnectionState.Connected

        } catch (e: Exception) {
            val errMsg = e.localizedMessage ?: "Connect exception"
            Log.e(tag, "Exception during MQTT connect to $host:$port: $errMsg", e)
            _connectionState.value = MqttConnectionState.Error(errMsg)
        }
    }

    suspend fun subscribe(topic: String) = withContext(Dispatchers.IO) {
        val activeClient = client
        if (activeClient == null) {
            Log.w(tag, "Cannot subscribe to [$topic]: MQTT client is null")
            return@withContext
        }

        Log.d(tag, "Subscribing to topic [$topic]...")
        try {
            activeClient.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback { publish: Mqtt5Publish ->
                    val message = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
                    Log.d(tag, "Received message on [$topic]: $message")
                    _incomingMessages.tryEmit(message)
                }
                .send()

            Log.d(tag, "Subscribed successfully to topic [$topic]")
        } catch (e: Exception) {
            Log.e(tag, "Exception during subscribe to [$topic]", e)
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
            activeClient.unsubscribeWith().topicFilter(topic).send()
        }
    }


    suspend fun publish(topic: String, payload: String) = withContext(Dispatchers.IO) {
        if (_connectionState.value != MqttConnectionState.Connected || client == null) {
            Log.d(tag, "Client not connected before publish. Attempting connect first...")
            connect()
        }

        val activeClient = client
        if (activeClient == null) {
            Log.w(tag, "Cannot publish to [$topic]: MQTT client is null")
            return@withContext
        }

        Log.d(tag, "Publishing to [$topic]: $payload")
        try {
            val publishResult = activeClient.toBlocking().publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(payload.toByteArray(StandardCharsets.UTF_8))
                .send()

            Log.d(tag, "Successfully published message to [$topic]: '$payload'. Result: $publishResult")
        } catch (e: Exception) {
            Log.e(tag, "Exception during publish to [$topic]", e)
        }
    }


    suspend fun disconnect() = withContext(Dispatchers.IO) {
        Log.d(tag, "Disconnecting MQTT client...")
        try {
            client?.toBlocking()?.disconnect()
        } catch (e: Exception) {
            Log.e(tag, "Exception during disconnect", e)
        }
        _connectionState.value = MqttConnectionState.Disconnected
        client = null
    }
}
