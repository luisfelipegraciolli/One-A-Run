package com.shiwa.onearun.race

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiwa.onearun.ui.theme.OneARunTheme

private val FirstPlaceGreen = Color(0xFF2E7D32)
private val SecondPlaceRed = Color(0xFFD32F2F)

@Composable
fun RaceScreen(
    modifier: Modifier = Modifier,
    viewModel: RaceViewModel,
    onNavigateToFinish: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(uiState.sessionFinished) {
        if (uiState.sessionFinished) {
            onNavigateToFinish()
        }
    }

    val context = LocalContext.current

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onLocationPermissionGranted()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.onLocationPermissionGranted()
        } else {
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    RaceContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun RaceContent(
    uiState: RaceUiState,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Se houver erro impeditivo (ex. sem sessão), exibe card de erro
            if (uiState.errorMessage != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Voltar")
                    }
                }
                return@Scaffold
            }

            // Se permissão de localização foi negada
            if (uiState.locationPermissionDenied) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Permissão de localização é necessária para registrar seu progresso na corrida.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Topo: Distância Atual / Distância Alvo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val currentDist = uiState.myDistanceMeters.toInt()
                val targetDist = uiState.targetDistanceMeters.toInt()

                Text(
                    text = "$currentDist / $targetDist",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                val progress = if (uiState.targetDistanceMeters > 0) {
                    (uiState.myDistanceMeters / uiState.targetDistanceMeters).coerceIn(0.0, 1.0)
                        .toFloat()
                } else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }

            // Centro: Ranking (1º e 2º colocado)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val firstRunnerName: String
                val firstRunnerDist: Int
                val firstRunnerColor: Color

                val secondRunnerName: String
                val secondRunnerDist: Int
                val secondRunnerColor: Color

                if (uiState.isFirstPlace) {
                    // Eu estou em 1º lugar: Meu nome em VERDE, oponente na cor padrão
                    firstRunnerName = uiState.myName
                    firstRunnerDist = uiState.myDistanceMeters.toInt()
                    firstRunnerColor = FirstPlaceGreen

                    secondRunnerName = uiState.opponentName
                    secondRunnerDist = uiState.opponentDistanceMeters.toInt()
                    secondRunnerColor = MaterialTheme.colorScheme.onBackground
                } else {
                    // Eu estou em 2º lugar: Oponente na cor padrão, Meu nome em VERMELHO
                    firstRunnerName = uiState.opponentName
                    firstRunnerDist = uiState.opponentDistanceMeters.toInt()
                    firstRunnerColor = MaterialTheme.colorScheme.onBackground

                    secondRunnerName = uiState.myName
                    secondRunnerDist = uiState.myDistanceMeters.toInt()
                    secondRunnerColor = SecondPlaceRed
                }

                // Linha do 1º Colocado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1º $firstRunnerName",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = firstRunnerColor
                    )
                    Text(
                        text = "${firstRunnerDist}m",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = firstRunnerColor
                    )
                }

                // Linha do 2º Colocado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2º $secondRunnerName",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondRunnerColor
                    )
                    Text(
                        text = "${secondRunnerDist}m",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondRunnerColor
                    )
                }
            }

            // Parte Inferior: Pace e Cronômetro
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (uiState.paceText == "--:--") "Pace: --:--" else "Pace: ${uiState.paceText}km",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = uiState.timeText,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (uiState.isWaitingOpponentToFinish) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aguardando o adversário terminar...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RaceContentFirstPlacePreview() {
    OneARunTheme {
        RaceContent(
            uiState = RaceUiState(
                myName = "João",
                opponentName = "Pedro",
                myDistanceMeters = 150.0,
                opponentDistanceMeters = 100.0,
                targetDistanceMeters = 400.0,
                isFirstPlace = true,
                paceText = "6:31",
                timeText = "02:23"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RaceContentSecondPlacePreview() {
    OneARunTheme {
        RaceContent(
            uiState = RaceUiState(
                myName = "Pedro",
                opponentName = "João",
                myDistanceMeters = 100.0,
                opponentDistanceMeters = 150.0,
                targetDistanceMeters = 400.0,
                isFirstPlace = false,
                paceText = "7:21",
                timeText = "02:23"
            )
        )
    }
}
