package com.shiwa.onearun.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    onCreateRaceNavigate: () -> Unit = {},
    onJoinRaceNavigate: () -> Unit = {}
) {
    HomeScreenContent(
        onCreateClick = onCreateRaceNavigate,
        onJoinClick = onJoinRaceNavigate,
        modifier = modifier
    )
}

@Composable
fun HomeScreenContent(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Alinha o conteúdo verticalmente no centro
        ) {
            // Título Principal
            Text(
                text = "ONE A RUN",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp
                ),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Botão "Criar uma corrida"
            Button(
                onClick = onCreateClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f) // Aumentado para evitar quebra de texto
                    .height(56.dp)
            ) {
                Text(
                    text = "CRIAR UMA CORRIDA",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botão "Entrar numa corrida"
            Button(
                onClick = onJoinClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f) // Aumentado para evitar quebra de texto
                    .height(56.dp)
            ) {
                Text(
                    text = "ENTRAR NUMA CORRIDA",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}