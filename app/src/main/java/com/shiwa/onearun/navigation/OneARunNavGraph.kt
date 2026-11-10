package com.shiwa.onearun.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shiwa.onearun.countdown.CountdownScreen
import com.shiwa.onearun.createrace.CreateRaceScreen
import com.shiwa.onearun.createrace.CreateRaceViewModel
import com.shiwa.onearun.createracewaiting.CreateRaceWaiting
import com.shiwa.onearun.createracewaiting.CreateRaceWaitingViewModel
import com.shiwa.onearun.home.HomeScreen
import com.shiwa.onearun.home.HomeViewModel
import com.shiwa.onearun.joinrace.JoinRaceScreen
import com.shiwa.onearun.joinrace.JoinRaceViewModel
import com.shiwa.onearun.lobby.LobbyScreen
import com.shiwa.onearun.navigation.OneARunDestinationsArgs.DISTANCE_ARG
import com.shiwa.onearun.navigation.OneARunDestinationsArgs.SESSION_CODE_ARG
import com.shiwa.onearun.navigation.OneARunDestinationsArgs.SESSION_ID_ARG
import com.shiwa.onearun.navigation.OneARunDestinationsArgs.USER_NAME_ARG
import com.shiwa.onearun.race.RaceScreen
import com.shiwa.onearun.race.RaceViewModel
import com.shiwa.onearun.ui.screens.FinishScreen

@Composable
fun OneARunNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = OneARunDestinations.HOME_ROUTE,
    navActions: OneARunNavigationActions = remember(navController) {
        OneARunNavigationActions(navController)
    }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable(route = OneARunDestinations.HOME_ROUTE) {
            val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
            HomeScreen(
                viewModel = viewModel,
                onCreateRaceNavigate = { navActions.navigateToCreateRace() },
                onJoinRaceNavigate = { navActions.navigateToJoinRace() }
            )
        }

        composable(route = OneARunDestinations.CREATE_RACE_ROUTE) {
            val viewModel: CreateRaceViewModel = viewModel(factory = CreateRaceViewModel.Factory)
            CreateRaceScreen(
                viewModel = viewModel,
                onNavigateToWaiting = { sessionId, joinCode, hostName, distance ->
                    navActions.navigateToCreateRaceWaiting(
                        sessionId = sessionId,
                        hostName = hostName,
                        distance = distance,
                        sessionCode = joinCode
                    )
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = OneARunDestinations.CREATE_RACE_WAITING_ROUTE,
            arguments = listOf(
                navArgument(name = SESSION_ID_ARG) { type = NavType.StringType },
                navArgument(name = SESSION_CODE_ARG) { type = NavType.StringType; defaultValue = "" },
                navArgument(name = USER_NAME_ARG) { type = NavType.StringType; defaultValue = "" },
                navArgument(name = DISTANCE_ARG) { type = NavType.StringType; defaultValue = "" }
            )
        ) { entry ->
            val sessionCode = entry.arguments?.getString(SESSION_CODE_ARG).orEmpty()
            val userName = entry.arguments?.getString(USER_NAME_ARG).orEmpty()
            val distance = entry.arguments?.getString(DISTANCE_ARG).orEmpty()

            val viewModel: CreateRaceWaitingViewModel =
                viewModel(factory = CreateRaceWaitingViewModel.Factory)
            val uiState by viewModel.uiState.collectAsState()

            CreateRaceWaiting(
                guestJoined = uiState.guestJoined,
                onGuestJoined = {
                    // Mudar isso para navegar para lobby, isso só está sendo feito para testes
                    navActions.navigateToRace(
                        sessionCode = sessionCode,
                        popUpToRoute = OneARunDestinations.CREATE_RACE_WAITING_ROUTE
                    )
                },
                hostName = userName,
                distance = distance,
                joinCode = sessionCode,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = OneARunDestinations.JOIN_RACE_ROUTE,
        ) {
            val viewModel: JoinRaceViewModel = viewModel(factory = JoinRaceViewModel.Factory)
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isJoined) {
                if (uiState.isJoined) {
                    navActions.navigateToRace(
                        sessionCode = uiState.joinCode,
                        popUpToRoute = OneARunDestinations.JOIN_RACE_ROUTE
                    )
                }
            }

            JoinRaceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLobby = {
                    navActions.navigateToRace(
                        sessionCode = uiState.joinCode,
                        popUpToRoute = OneARunDestinations.JOIN_RACE_ROUTE
                    )
                },
                viewModel = viewModel
            )
        }

        composable(
            route = OneARunDestinations.LOBBY_ROUTE,
            arguments = listOf(
                navArgument(name = SESSION_ID_ARG) { type = NavType.StringType },
            )
        ) {
            LobbyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = OneARunDestinations.COUNTDOWN_ROUTE,
        ) {
            CountdownScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = OneARunDestinations.RACE_ROUTE,
            arguments = listOf(
                navArgument(name = SESSION_CODE_ARG) { type = NavType.StringType }
            )
        ) { entry ->
            val sessionCode = entry.arguments?.getString(SESSION_CODE_ARG).orEmpty()
            val viewModel: RaceViewModel = viewModel(factory = RaceViewModel.Factory)
            RaceScreen(
                viewModel = viewModel,
                onNavigateToFinish = {
                    navActions.navigateToFinish(sessionCode)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = OneARunDestinations.FINISH_ROUTE,
            arguments = listOf(
                navArgument(name = SESSION_CODE_ARG) { type = NavType.StringType },
            )
        ) {
            FinishScreen(
                onNavigateHome = {
                    navActions.navigateToHome()
                }
            )
        }
    }
}