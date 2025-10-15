package com.profpay.wallet.ui.app.navigation.graphs.navGraph

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.profpay.wallet.ui.app.navigation.bottombar.BottomBarScreen
import com.profpay.wallet.ui.app.navigation.graphs.Graph
import com.profpay.wallet.ui.screens.lockScreen.CreateLockScreen
import com.profpay.wallet.ui.screens.lockScreen.LockScreen
import com.profpay.wallet.ui.screens.settings.SettingsAccountScreen
import com.profpay.wallet.ui.screens.settings.SettingsAmlScreen
import com.profpay.wallet.ui.screens.settings.SettingsNotificationsScreen
import com.profpay.wallet.ui.screens.settings.SettingsScreen
import com.profpay.wallet.ui.screens.settings.SettingsSecurityScreen

fun NavGraphBuilder.settingsNavGraph(navController: NavController) {
    navigation(
        route = Graph.Settings.route,
        startDestination = "SETTINGS",
    ) {
        composable(route = "SETTINGS") {
            SettingsScreen(
                goToLockGraph = {
                },
                goToTheme = {
                },
                goToSettingsNotifications = {
                    navController.navigate(route = SettingsS.SettingsNotifications.route)
                },
                goToSettingsSecurity = {
                    navController.navigate(route = SettingsS.SettingsSecurity.route)
                },
                goToSettingsAccount = {
                    navController.navigate(route = SettingsS.SettingsAccount.route)
                },
                goToSettingsAml = {
                    navController.navigate(route = SettingsS.SettingsAml.route)
                },
            )
        }
        composable(route = SettingsS.SettingsAccount.route) {
            SettingsAccountScreen(
                goToBack = { navController.navigateUp() },
            )
        }
        composable(route = SettingsS.SettingsNotifications.route) {
            SettingsNotificationsScreen(
                goToBack = { navController.navigateUp() },
            )
        }

        composable(route = SettingsS.SettingsSecurity.route) {
            SettingsSecurityScreen(
                goToBack = { navController.navigateUp() },
                goToLock = { navController.navigate(route = SettingsSecurity.LockScreen.route) },
            )
        }

        composable(route = SettingsS.SettingsAml.route) {
            SettingsAmlScreen(
                goToBack = { navController.navigateUp() },
            )
        }

        composable(route = SettingsSecurity.LockScreen.route) {
            LockScreen(
                toNavigate = {
                    navController.navigate(route = LockScreen.CreateLockScreen.route)
                },
                goingBack = true,
                goToBack = {
                    navController.navigate(route = BottomBarScreen.Settings.route)
                },
            )
        }

        composable(route = LockScreen.CreateLockScreen.route) {
            CreateLockScreen(
                toNavigate = {
                    navController.navigate(route = BottomBarScreen.Settings.route) {
                        popUpTo(route = BottomBarScreen.Profile.route)
                    }
                },
                goingBack = true,
                goToBack = {
                    navController.navigate(route = BottomBarScreen.Settings.route)
                },
            )
        }
    }
}

sealed class SettingsS(
    val route: String,
) {
    data object SettingsNotifications : SettingsS(route = "settings_notifications")

    data object SettingsSecurity : SettingsS(route = "settings_security")

    data object SettingsAccount : SettingsS(route = "settings_account")

    data object SettingsAml : SettingsS(route = "settings_aml")
}

sealed class SettingsSecurity(
    val route: String,
) {
    data object LockScreen : SettingsSecurity(route = "lock_screen_from_settings")
}

sealed class LockScreen(
    val route: String,
) {
    data object CreateLockScreen : LockScreen(route = "create_lock_screen_from_settings")
}
