package com.shiwa.onearun.finish

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.shiwa.onearun.data.repository.LocationPoint

private val WinColor = Color(0xFF2E7D32)
private val LossColor = Color(0xFFC62828)

@Composable
fun FinishScreen(
    uiState: FinishUiState,
    onNavigateHome: () -> Unit = {}
) {
    FinishScreenContent(
        isWinner = uiState.isWinner,
        topSpeed = uiState.topSpeedText,
        distance = uiState.distanceText,
        elapsedTime = uiState.elapsedTimeText,
        routePoints = uiState.routePoints,
        onNavigateHome = onNavigateHome
    )
}

@Composable
fun FinishScreen(
    modifier: Modifier = Modifier,
    isWinner: Boolean = true,
    topSpeed: String = "5:12min/km",
    distance: String = "500m",
    elapsedTime: String = "3:21",
    routePoints: List<LocationPoint> = emptyList(),
    onNavigateHome: () -> Unit = {}
) {
    FinishScreenContent(
        modifier = modifier,
        isWinner = isWinner,
        topSpeed = topSpeed,
        distance = distance,
        elapsedTime = elapsedTime,
        routePoints = routePoints,
        onNavigateHome = onNavigateHome
    )
}

@Composable
fun FinishScreenContent(
    modifier: Modifier = Modifier,
    isWinner: Boolean = true,
    topSpeed: String = "5:12min/km",
    distance: String = "500m",
    elapsedTime: String = "3:21",
    routePoints: List<LocationPoint> = emptyList(),
    onNavigateHome: () -> Unit = {}
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val resultColor = if (isWinner) WinColor else LossColor

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .clickable { onNavigateHome() }
        ) {
            // Bloco Superior: Resultado + Top Speed + Distância
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isWinner) "Você\nganhou!" else "Você perdeu :(",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = resultColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Top Speed:\n$topSpeed",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = distance,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = textColor
                )
            }

            // Bloco Central: Mapa com o trajeto do corredor
            RouteMapCard(
                routePoints = routePoints,
                lineColor = resultColor,
                isWinner = isWinner,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp)
            )

            // Bloco Inferior: Tempo final da corrida
            Text(
                text = elapsedTime,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = textColor
            )
        }
    }
}

@Composable
fun RouteMapCard(
    routePoints: List<LocationPoint>,
    lineColor: Color,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    val displayPoints = remember(routePoints, isWinner) {
        if (routePoints.isNotEmpty()) {
            routePoints.map { LatLng(it.latitude, it.longitude) }
        } else if (isWinner) {
            listOf(
                LatLng(-23.550520, -46.633308),
                LatLng(-23.554520, -46.633308),
                LatLng(-23.554520, -46.631308)
            )
        } else {
            listOf(
                LatLng(-23.554520, -46.633308),
                LatLng(-23.552520, -46.633308),
                LatLng(-23.552520, -46.634508),
                LatLng(-23.550520, -46.634508)
            )
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(displayPoints.first(), 16f)
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true,
            tiltGesturesEnabled = false
        )
    }

    val mapProperties = remember {
        MapProperties(isMyLocationEnabled = false)
    }

    LaunchedEffect(displayPoints) {
        if (displayPoints.size >= 2) {
            val builder = LatLngBounds.builder()
            displayPoints.forEach { builder.include(it) }
            val bounds = builder.build()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 80)
            )
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = mapUiSettings,
                properties = mapProperties
            ) {
                if (displayPoints.size >= 2) {
                    Polyline(
                        points = displayPoints,
                        color = lineColor,
                        width = 12f
                    )
                    Marker(
                        state = MarkerState(position = displayPoints.first()),
                        title = "Início",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                    Marker(
                        state = MarkerState(position = displayPoints.last()),
                        title = "Fim",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (isWinner) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED
                        )
                    )
                }
            }
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
