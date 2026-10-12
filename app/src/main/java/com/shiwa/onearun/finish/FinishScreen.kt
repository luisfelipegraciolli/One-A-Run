package com.shiwa.onearun.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FinishScreen(
    modifier: Modifier = Modifier,
    onNavigateHome: () -> Unit = {}
) {
    Scaffold { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Race Finished Screen",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(
                onClick = onNavigateHome,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Return to Home")
            }
        }
    }
}
