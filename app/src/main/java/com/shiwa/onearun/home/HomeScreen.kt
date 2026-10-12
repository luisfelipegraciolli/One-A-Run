package com.shiwa.onearun.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shiwa.onearun.data.remote.MqttConnectionState

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = HomeViewModel(),
    onStartRaceNavigate: () -> Unit = {},
    onEnterRaceNavigate: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onStartRaceClick = {
            viewModel.onStartRaceClick()
            onStartRaceNavigate()
        },
        onEnterRaceClick = {
            viewModel.onEnterRaceClick()
            onEnterRaceNavigate()
        },
        onPublishClick = {
            viewModel.onPublishHelloClick()
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(text = "One A Run")
        },
        modifier = Modifier.clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onStartRaceClick: () -> Unit,
    onEnterRaceClick: () -> Unit,
    onPublishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { HomeScreenTopBar() }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = uiState.statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = when (uiState.connectionState) {
                    MqttConnectionState.Connected -> MaterialTheme.colorScheme.primary
                    is MqttConnectionState.Error -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            uiState.lastReceivedMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last msg: $msg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            StartRaceButton(onClick = onStartRaceClick)

            Spacer(modifier = Modifier.height(12.dp))

            EnterRaceButton(onClick = onEnterRaceClick)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onPublishClick,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(text = "Publish Hello Message")
            }
        }
    }
}

@Composable
fun EnterRaceButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(text = "Enter Race")
    }
}

@Composable
fun StartRaceButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(text = "Start Race")
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        uiState = HomeUiState(statusMessage = "Connected to HiveMQ Broker"),
        onStartRaceClick = {},
        onEnterRaceClick = {},
        onPublishClick = {}
    )
}
