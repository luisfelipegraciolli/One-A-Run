package com.shiwa.onearun.createracewaiting

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateRaceWaiting(
    modifier: Modifier = Modifier,
    hostName: String = "João",
    distance: String = "400m",
    joinCode: String = "UX9BS",
    guestJoined: Boolean = false,
    onGuestJoined: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    LaunchedEffect(guestJoined) { if (guestJoined) onGuestJoined() }
    val context = LocalContext.current

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Título
                Text(
                    text = "Aguardando\ndesafiante...",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp
                    ),
                    modifier = Modifier.padding(bottom = 36.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Host
                Text(
                    text = "Host",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hostName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Distância
                Text(
                    text = "distância",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = distance,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Código da sessão
                Text(
                    text = joinCode,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 36.sp
                    )
                )
            }

            // Botão Compartilhar
            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Entre na minha corrida no One A Run! Código da sessão: $joinCode"
                        )
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Compartilhar código da corrida")
                    context.startActivity(shareIntent)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp)
            ) {
                Text(
                    text = "compartilhar",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
