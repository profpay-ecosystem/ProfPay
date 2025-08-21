package com.example.telegramWallet.ui.app.navigation.graphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.telegramWallet.ui.app.navigation.bottom_bar.BottomBarScreen
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.coRAddressFromWalletSystemNG
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.profileNavGraph
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.settingsNavGraph
import com.example.telegramWallet.ui.screens.SmartInDevelopment

@Composable
fun HomeNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        route = Graph.Home.route,
        startDestination = Graph.Profile.route
    ) {

        composable(route = BottomBarScreen.SmartContractList.route) {
//           SmartListScreen(
//               goToSystemTRX = { navController.navigate(route = WalletInfo.WalletSystemTRX.route) }
//           )
            SmartInDevelopment()
        }

        settingsNavGraph(navController)
        profileNavGraph(navController)
        coRAddressFromWalletSystemNG(navController)
    }
}

