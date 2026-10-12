package com.shiwa.onearun.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shiwa.onearun.createrace.CreateRaceScreen
import com.shiwa.onearun.home.HomeScreen
import com.shiwa.onearun.lobby.LobbyScreen
import com.shiwa.onearun.home.HomeViewModel
import com.shiwa.onearun.joinrace.JoinRaceScreen
import com.shiwa.onearun.race.RaceScreen
import com.shiwa.onearun.ui.screens.FinishScreen

enum class AppScreen {
    Home,
    Lobby,
    Race,
    CreateRace,
    JoinRace,
    End
}

@Composable
fun OneARunApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    Scaffold(modifier = modifier) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Home.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppScreen.Home.name) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onStartRaceNavigate = {
                        navController.navigate(AppScreen.CreateRace.name)
                    },
                    onEnterRaceNavigate = {
                        navController.navigate(AppScreen.JoinRace.name)
                    }
                )
            }

            composable(route = AppScreen.CreateRace.name) {
                CreateRaceScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = AppScreen.JoinRace.name) {
                JoinRaceScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = AppScreen.Lobby.name) {
                LobbyScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = AppScreen.Race.name) {
                RaceScreen(
                    onNavigateBack = {
                        navController.navigate(AppScreen.End.name)
                    }
                )
            }

            composable(route = AppScreen.End.name) {
                FinishScreen(
                    onNavigateHome = {
                        navController.popBackStack(AppScreen.Home.name, inclusive = false)
                    }
                )
            }
        }
    }
}
