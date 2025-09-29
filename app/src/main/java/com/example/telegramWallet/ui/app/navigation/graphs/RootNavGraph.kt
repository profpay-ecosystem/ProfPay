package com.example.telegramWallet.ui.app.navigation.graphs

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.telegramWallet.PrefKeys
import com.example.telegramWallet.bridge.view_model.pin_lock.LockState
import com.example.telegramWallet.bridge.view_model.pin_lock.PinLockViewModel
import com.example.telegramWallet.data.services.NetworkMonitor
import com.example.telegramWallet.ui.app.navigation.HomeScreen
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.coRAddressNavGraph
import com.example.telegramWallet.ui.components.animation.enterTransition
import com.example.telegramWallet.ui.components.animation.exitTransition
import com.example.telegramWallet.ui.components.animation.popEnterTransition
import com.example.telegramWallet.ui.components.animation.popExitTransition
import com.example.telegramWallet.ui.screens.SplashScreen
import com.example.telegramWallet.ui.screens.createOrRecoveryWallet.WelcomingScreen
import com.example.telegramWallet.ui.screens.lockScreen.BlockedAppScreen
import com.example.telegramWallet.ui.screens.lockScreen.CreateLockScreen
import com.example.telegramWallet.ui.screens.lockScreen.LockScreen
import com.example.telegramWallet.ui.shared.sharedPref

@Composable
fun RootNavigationGraph(
    navController: NavHostController,
    networkMonitor: NetworkMonitor,
    pinLockViewModel: PinLockViewModel = hiltViewModel(),
) {
    val isConnected by networkMonitor.networkAvailable.collectAsState()

    val sharedPref = sharedPref()

    val isFirstStart = sharedPref.getBoolean(PrefKeys.FIRST_STARTED, true)
    val isAcceptedRules = sharedPref.getBoolean(PrefKeys.ACCEPTED_RULES, false)

    var targetRoute by remember { mutableStateOf(Graph.Splash.route) }

    LaunchedEffect(Unit) {
        pinLockViewModel.navigationEvents.collect { state ->
            targetRoute =
                when (state) {
                    LockState.RequireCreation -> Graph.CreateLockScreen.route
                    LockState.RequireUnlock -> Graph.LockScreen.route
                    LockState.None ->
                        when {
                            !isConnected -> Graph.BlockedAppScreen.route
                            (isFirstStart && !isAcceptedRules) || !isAcceptedRules -> Graph.WelcomingScreen.route
                            (isFirstStart) -> Graph.FirstStart.route
                            else -> Graph.Home.route
                        }
                }
            targetRoute.let { route ->
                val current = navController.currentBackStackEntry?.destination?.route
                if (current != route) {
                    val previousBackStackEntry = navController.previousBackStackEntry?.destination?.route
                    if (state == LockState.None && previousBackStackEntry != Graph.Splash.route && targetRoute != Graph.Home.route) {
                        navController.navigateUp()
                    } else {
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
    NavHost(
        navController = navController,
        route = Graph.Root.route,
        startDestination = Graph.Splash.route,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
    ) {
        composable(route = Graph.Home.route) {
            HomeScreen()
        }
        composable(route = Graph.Splash.route) {
            SplashScreen()
        }
        composable(route = Graph.CreateLockScreen.route) {
            CreateLockScreen(
                toNavigate = {
                    navController.navigate(route = Graph.WelcomingScreen.route)
                },
            )
            BackHandler {}
        }
        composable(route = Graph.LockScreen.route) {
            LockScreen(
                viewModel = pinLockViewModel,
                toNavigate = {},
            )
            BackHandler {}
        }
        composable(route = Graph.WelcomingScreen.route) {
            WelcomingScreen(
                toNavigate = { route ->
                    navController.navigate(route)
                },
            )
            BackHandler {}
        }

        coRAddressNavGraph(navController = navController)

        composable(route = Graph.BlockedAppScreen.route) {
            BlockedAppScreen(toNavigate = {
                navController.navigateUp()
            })
            BackHandler {}
        }
    }
}

sealed class Graph(
    val route: String,
) {
    object Root : Graph(route = "root_graph")

    object Splash : Graph(route = "splash_graph")

    object Home : Graph(route = "home_graph")

    object Profile : Graph(route = "profile_graph")

    object Settings : Graph(route = "settings_graph")

    object CreateLockScreen : Graph(route = "create_lock_screen")

    object LockScreen : Graph(route = "lock_screen")

    object BlockedAppScreen : Graph(route = "blocked_app_screen")

    object WelcomingScreen : Graph(route = "welcoming_screen")

    object CreateOrRecoveryAddress : Graph(route = "create_or_recover_address")

    object FirstStart : Graph(route = "first_start")
}
