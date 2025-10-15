package com.profpay.wallet.ui.app.navigation.graphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.profpay.wallet.ui.app.navigation.bottombar.BottomBarScreen
import com.profpay.wallet.ui.app.navigation.graphs.navGraph.coRAddressFromWalletSystemNG
import com.profpay.wallet.ui.app.navigation.graphs.navGraph.profileNavGraph
import com.profpay.wallet.ui.app.navigation.graphs.navGraph.settingsNavGraph
import com.profpay.wallet.ui.components.animation.enterTransition
import com.profpay.wallet.ui.components.animation.exitTransition
import com.profpay.wallet.ui.components.animation.popEnterTransition
import com.profpay.wallet.ui.components.animation.popExitTransition
import com.profpay.wallet.ui.screens.SmartInDevelopment

@Composable
fun HomeNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        route = Graph.Home.route,
        startDestination = Graph.Profile.route,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
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
