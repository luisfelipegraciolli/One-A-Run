package com.shiwa.onearun.navigation

import android.net.Uri
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.shiwa.onearun.navigation.OneARunDestinations.CREATE_RACE_ROUTE
import com.shiwa.onearun.navigation.OneARunDestinations.JOIN_RACE_ROUTE
import com.shiwa.onearun.navigation.OneARunDestinationsArgs.DISTANCE_ARG
import com.shiwa.onearun.navigation.OneARunDestinationsArgs.SESSION_CODE_ARG
import com.shiwa.onearun.navigation.OneARunDestinationsArgs.SESSION_ID_ARG
import com.shiwa.onearun.navigation.OneARunDestinationsArgs.USER_NAME_ARG
import com.shiwa.onearun.navigation.OneARunScreens.COUNTDOWN_SCREEN
import com.shiwa.onearun.navigation.OneARunScreens.CREATE_RACE_SCREEN
import com.shiwa.onearun.navigation.OneARunScreens.CREATE_RACE_WAITING_SCREEN
import com.shiwa.onearun.navigation.OneARunScreens.FINISH_SCREEN
import com.shiwa.onearun.navigation.OneARunScreens.HOME_SCREEN
import com.shiwa.onearun.navigation.OneARunScreens.JOIN_RACE_SCREEN
import com.shiwa.onearun.navigation.OneARunScreens.LOBBY_SCREEN
import com.shiwa.onearun.navigation.OneARunScreens.RACE_SCREEN
import java.util.Locale.getDefault

private object OneARunScreens {
    const val HOME_SCREEN = "home"
    const val CREATE_RACE_SCREEN = "createRace"
    const val CREATE_RACE_WAITING_SCREEN = "createRaceWaiting"
    const val JOIN_RACE_SCREEN = "joinRace"
    const val LOBBY_SCREEN = "lobby"
    const val COUNTDOWN_SCREEN = "countdown"
    const val RACE_SCREEN = "race"
    const val FINISH_SCREEN = "finish"
}

object OneARunDestinationsArgs {
    const val USER_NAME_ARG = "username"
    const val SESSION_CODE_ARG = "sessionCode"
    const val DISTANCE_ARG = "distance"
    const val SESSION_ID_ARG = "sessionId"
}

object OneARunDestinations {
    const val HOME_ROUTE = HOME_SCREEN
    const val CREATE_RACE_ROUTE = CREATE_RACE_SCREEN

    // sessionId no caminho (obrigatório); o resto é só para exibir e fica como parâmetro opcional de query
    const val CREATE_RACE_WAITING_ROUTE =
        "$CREATE_RACE_WAITING_SCREEN/{$SESSION_ID_ARG}?$SESSION_CODE_ARG={$SESSION_CODE_ARG}&$USER_NAME_ARG={$USER_NAME_ARG}&$DISTANCE_ARG={$DISTANCE_ARG}"
    const val JOIN_RACE_ROUTE = JOIN_RACE_SCREEN
    const val LOBBY_ROUTE = "$LOBBY_SCREEN/{$SESSION_ID_ARG}"
    const val COUNTDOWN_ROUTE = COUNTDOWN_SCREEN

    // Reescrever para usar o SESSION_ID_ARG
    const val RACE_ROUTE = "$RACE_SCREEN/{$SESSION_CODE_ARG}"
    const val FINISH_ROUTE = "$FINISH_SCREEN/{$SESSION_CODE_ARG}"
}

class OneARunNavigationActions(
    private val navController: NavHostController
){
    fun navigateToCreateRace() {
        navController.navigate(CREATE_RACE_ROUTE)
    }

    fun navigateToCreateRaceWaiting(
        sessionId: String,
        hostName: String,
        distance: String,
        sessionCode: String
    ) {
        val encodedSessionId = Uri.encode(sessionId)
        val encodedHost = Uri.encode(hostName)
        val encodedDist = Uri.encode(distance)
        val encodedCode = Uri.encode(sessionCode)
        navController.navigate("$CREATE_RACE_WAITING_SCREEN/$encodedSessionId?$SESSION_CODE_ARG=$encodedCode&$USER_NAME_ARG=$encodedHost&$DISTANCE_ARG=$encodedDist") {
            // A corrida já existe no servidor: o botão voltar não deve reabrir o formulário de criação
            popUpTo(CREATE_RACE_ROUTE) { inclusive = true }
        }
    }

    fun navigateToJoinRace(){
        navController.navigate(JOIN_RACE_ROUTE)
    }

    /**
     * popUpToRoute: tela de origem que deve sair da pilha ao avançar
     * (a espera, no caso do anfitrião; o formulário de entrada, no caso do convidado).
     */
    fun navigateToLobby(sessionId: String, popUpToRoute: String? = null) {
        val encodedSessionId = Uri.encode(sessionId)
        // O nome vai na query: sem Uri.encode, espaço, "&" ou "/" quebrariam a rota
        navController.navigate("$LOBBY_SCREEN/$encodedSessionId") {
            if (popUpToRoute != null) {
                popUpTo(popUpToRoute) { inclusive = true }
            }
        }
    }

    fun navigateToRace(sessionCode: String){
        val uppercaseSessionCode = sessionCode.uppercase(getDefault())
        navController.navigate("$RACE_SCREEN/$uppercaseSessionCode")
    }

    fun navigateToFinish(sessionCode: String){
        val uppercaseSessionCode = sessionCode.uppercase(getDefault())
        navController.navigate("$FINISH_SCREEN/$uppercaseSessionCode")
    }

    fun navigateToHome() {
        navController.navigate(HOME_SCREEN){
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = false
                saveState = false
            }
        }
    }
}