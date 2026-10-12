package com.shiwa.onearun.navigation

import androidx.compose.runtime.Composable
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
import com.shiwa.onearun.home.HomeScreen
import com.shiwa.onearun.home.HomeViewModel
import com.shiwa.onearun.joinrace.JoinRaceScreen
import com.shiwa.onearun.lobby.LobbyScreen
import com.shiwa.onearun.navigation.OneARunDestinationsArgs.SESSION_CODE_ARG
import com.shiwa.onearun.navigation.OneARunDestinationsArgs.USER_NAME_ARG
import com.shiwa.onearun.race.RaceScreen
import com.shiwa.onearun.ui.screens.FinishScreen

@Composable
fun OneARunNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = OneARunDestinations.HOME_ROUTE,
    navActions: OneARunNavigationActions = remember(navController) {
        OneARunNavigationActions(navController)
    }
){
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
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
            CreateRaceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = OneARunDestinations.JOIN_RACE_ROUTE) {
            JoinRaceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = OneARunDestinations.LOBBY_ROUTE,
            arguments = listOf(
                navArgument(name = SESSION_CODE_ARG) { type = NavType.StringType },
                navArgument(name = USER_NAME_ARG) { type = NavType.StringType}
            )

        ) { entry ->
            val sessionCode = entry.arguments?.getString(SESSION_CODE_ARG)
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
            val sessionCode = entry.arguments?.getString(SESSION_CODE_ARG)
            RaceScreen(
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

        ) { entry ->
            val sessionCode = entry.arguments?.getString(SESSION_CODE_ARG)
            FinishScreen(
                onNavigateHome = {
                    navActions.navigateToHome()
                }
            )
        }
    }
}