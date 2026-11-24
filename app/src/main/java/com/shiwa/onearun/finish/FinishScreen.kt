package com.shiwa.onearun.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FinishScreen(
    modifier: Modifier = Modifier,
    isWinner: Boolean = true,
    topSpeed: String = "5:12min/km",
    distance: String = "500m",
    elapsedTime: String = "3:21",
    onNavigateHome: () -> Unit = {}
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val resultColor = if (isWinner) Color(0xFF2E7D32) else Color(0xFFC62828)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .clickable { onNavigateHome() }
        ) {
            // Bloco Superior: Resultado + Top Speed + Distância
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isWinner) "Você\nganhou!" else "Você perdeu :(",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    color = resultColor
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Top Speed:\n$topSpeed",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = distance,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = textColor
                )
            }

            // Bloco Inferior: Tempo final da corrida
            Text(
                text = elapsedTime,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = textColor
            )
        }
    }
}

@Preview(name = "Vitória", showBackground = true, showSystemUi = true)
@Composable
fun FinishScreenWinPreview() {
    MaterialTheme {
        FinishScreen(
            isWinner = true,
            topSpeed = "5:12min/km",
            distance = "500m",
            elapsedTime = "3:21"
        )
    }
}

@Preview(name = "Derrota", showBackground = true, showSystemUi = true)
@Composable
fun FinishScreenLossPreview() {
    MaterialTheme {
        FinishScreen(
            isWinner = false,
            topSpeed = "7:00min/km",
            distance = "500m",
            elapsedTime = "4:01"
        )
    }
}