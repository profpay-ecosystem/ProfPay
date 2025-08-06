package com.example.telegramWallet.ui.app.navigation.graphs

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.telegramWallet.R
import com.example.telegramWallet.data.services.NetworkMonitor
import com.example.telegramWallet.ui.app.navigation.HomeScreen
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.coRAddressNavGraph
import com.example.telegramWallet.ui.screens.createOrRecoveryWallet.WelcomingScreen
import com.example.telegramWallet.ui.screens.lockScreen.BlockedAppScreen
import com.example.telegramWallet.ui.screens.lockScreen.CreateLockScreen
import com.example.telegramWallet.ui.screens.lockScreen.LockScreen


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun RootNavigationGraph(navController: NavHostController, networkMonitor: NetworkMonitor) {
    val isConnected by networkMonitor.networkAvailable.collectAsState()
    NavHost(
        navController = navController,
        route = Graph.Root.route,
        startDestination = Graph.Home.route
    ) {
        composable(route = Graph.Home.route) {
            HomeScreen()
        }
        composable(route = Graph.CreateLockScreen.route) {
            CreateLockScreen(
                toNavigate = {
                    navController.navigate(route = Graph.WelcomingScreen.createRoute(firstStart = true))
                }
            )
            BackHandler {}
        }
        composable(route = Graph.LockScreen.route) {
            LockScreen(
                toNavigate = {
                    navController.navigateUp()
                }
            )
            BackHandler {}
        }
        composable(
            route = Graph.WelcomingScreen.route,
            arguments = listOf(
                navArgument("firstStart") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val firstStart = backStackEntry.arguments?.getBoolean("firstStart") ?: false

            WelcomingScreen(
                firstStart = firstStart,
                toNavigate = { route ->
                    navController.navigate(route)
                },
                toNavigateUp = {
                    navController.navigateUp()
                }
            )
            BackHandler {}
        }

        coRAddressNavGraph(navController)

        composable(route = Graph.BlockedAppScreen.route) {
            BlockedAppScreen(toNavigate = {
                navController.navigateUp()
            })
            BackHandler {}
        }
    }

    // Мы можем делать такую проверку только после создания дерева навигатора.
    val sharedPref = LocalContext.current.getSharedPreferences(
        ContextCompat.getString(LocalContext.current, R.string.preference_file_key),
        Context.MODE_PRIVATE
    )

    val pinCode = sharedPref.getString("pin_code", "startInit")
    val sessionActivity = sharedPref.getBoolean("session_activity", false)
    val isFirstStart = sharedPref.getBoolean("FIRST_STARTED", true)
    val isAcceptedRules = sharedPref.getBoolean("ACCEPTED_RULES", false)

    if (!isConnected) {
        navController.navigate(route = Graph.BlockedAppScreen.route)
    } else {
        if (pinCode.equals("startInit")) navController.navigate(route = Graph.CreateLockScreen.route)
        else if (!sessionActivity) {
            navController.navigate(route = Graph.LockScreen.route)
        }

        if (isFirstStart && !pinCode.equals("startInit")) {
            navController.navigate(route = Graph.WelcomingScreen.createRoute(true))
        }

        if (!isFirstStart && !isAcceptedRules) {
            navController.navigate(route = Graph.WelcomingScreen.createRoute(false))
        }
    }
}

sealed class Graph(val route: String) {
    object Root : Graph(route = "root_graph")
    object Home : Graph(route = "home_graph")
    object Profile : Graph(route = "profile_graph")
    object Settings : Graph(route = "settings_graph")
    object CreateLockScreen : Graph(route = "create_lock_screen")
    object LockScreen : Graph(route = "lock_screen")
    object BlockedAppScreen : Graph(route = "blocked_app_screen")
    object WelcomingScreen : Graph(route = "welcoming_screen/{firstStart}") {
        fun createRoute(firstStart: Boolean): String {
            return "welcoming_screen/$firstStart"
        }
    }
    object CreateOrRecoveryAddress : Graph(route = "create_or_recover_address")
    object FirstStart : Graph(route = "first_start")
}


