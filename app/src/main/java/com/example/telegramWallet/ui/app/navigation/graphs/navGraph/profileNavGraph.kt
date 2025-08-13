package com.example.telegramWallet.ui.app.navigation.graphs.navGraph

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.telegramWallet.ui.app.navigation.BottomBarScreen
import com.example.telegramWallet.ui.app.navigation.graphs.Graph
import com.example.telegramWallet.ui.screens.wallet.CentralAddressTxHistoryScreen
import com.example.telegramWallet.ui.screens.wallet.ReceiveFromWalletSotsScreen
import com.example.telegramWallet.ui.screens.wallet.SendFromWalletInfoScreen
import com.example.telegramWallet.ui.screens.wallet.TXDetailsScreen
import com.example.telegramWallet.ui.screens.wallet.WalletAddressScreen
import com.example.telegramWallet.ui.screens.wallet.WalletArchivalSotsScreen
import com.example.telegramWallet.ui.screens.wallet.WalletInfoScreen
import com.example.telegramWallet.ui.screens.wallet.WalletSotsScreen
import com.example.telegramWallet.ui.screens.wallet.WalletSystemScreen
import com.example.telegramWallet.ui.screens.wallet.WalletSystemTRXScreen

fun NavGraphBuilder.profileNavGraph(navController: NavController) {
    navigation(
        route = Graph.Profile.route,
        startDestination = "WALLET"
    ) {
        composable("WALLET") {
            WalletInfoScreen(
                goToSendWalletInfo = { addressId: Long, tokenName: String ->
                    navController.navigate(
                        route = WalletInfo.Send.createRoute(
                            addressId = addressId,
                            tokenName = tokenName
                        )
                    )
                },
                goToWalletSystem = { navController.navigate(route = WalletInfo.WalletSystem.route) },
                goToWalletSystemTRX = { navController.navigate(route = WalletInfo.WalletSystemTRX.route) },
                goToWalletSots = { navController.navigate(route = WalletInfo.WalletSots.route) },
                goToTXDetailsScreen = { navController.navigate(route = WalletInfo.TXDetails.route) }
            )
        }

        composable(WalletInfo.WalletSots.route) {
            WalletSotsScreen(
                goToWalletAddress = { navController.navigate(route = WalletSots.WalletAddress.route) },
                goToWalletArchivalSots = { navController.navigate(route = WalletSots.WalletArchivalSots.route) },
                goToBack = { navController.navigateUp() },
                goToReceive = { navController.navigate(route = WalletAddress.Receive.route) }
            )
        }
        composable(
            route = WalletInfo.Send.route,
            arguments = listOf(
                navArgument("addressId") { type = NavType.LongType },
                navArgument("tokenName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val addressId = backStackEntry.arguments?.getLong("addressId") ?: 1L
            val tokenName = backStackEntry.arguments?.getString("tokenName") ?: "USDT"

            SendFromWalletInfoScreen(
                addressId = addressId,
                tokenName = tokenName,
                goToBack = { navController.navigateUp() },
                goToSystemTRX = { navController.navigate(route = WalletInfo.WalletSystemTRX.route) }
            )
        }
        composable(WalletInfo.WalletSystemTRX.route) {
            WalletSystemTRXScreen(
                goToBack = { navController.navigateUp() },
                goToCentralAddressTxHistory = { navController.navigate(route = WalletInfo.CentralAddressTxHistory.route) },
            )
        }
        composable(WalletInfo.CentralAddressTxHistory.route) {
            CentralAddressTxHistoryScreen(
                goToBack = { navController.navigateUp() },
            )
        }
        composable(WalletInfo.TXDetails.route) {
            TXDetailsScreen(
                goToBack = { navController.navigateUp() }
            )
        }
        composable(WalletInfo.WalletSystem.route) {
            WalletSystemScreen(
                goToBack = { navController.navigateUp() },
                goToWalletInfo = { navController.navigate(route = BottomBarScreen.Profile.route) },
                goToCoRA = { navController.navigate(route = Graph.CreateOrRecoveryAddress.route) }
            )
        }
        composable(WalletSots.WalletArchivalSots.route) {
            WalletArchivalSotsScreen(
                goToBack = { navController.navigateUp() },
                goToWalletAddress = { navController.navigate(route = WalletSots.WalletAddress.route) }
            )
        }

        composable(WalletSots.WalletAddress.route) {
            WalletAddressScreen(
                goToSendWalletAddress = { addressId: Long, tokenName: String ->
                    navController.navigate(
                        route = WalletAddress.Send.createRoute(
                            addressId = addressId,
                            tokenName = tokenName
                        )
                    )
                },
                goToBack = { navController.navigateUp() },
                goToSystemTRX = { navController.navigate(route = WalletInfo.WalletSystemTRX.route) },
                goToTXDetailsScreen = { navController.navigate(route = WalletInfo.TXDetails.route) },
                goToReceive = { navController.navigate(route = WalletAddress.Receive.route) }
            )
        }
        composable(WalletAddress.Receive.route) {
            ReceiveFromWalletSotsScreen(
                goToBack = { navController.navigateUp() }
            )
        }
        composable(
            route = WalletAddress.Send.route,
            arguments = listOf(
                navArgument("addressId") { type = NavType.LongType },
                navArgument("tokenName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val addressId = backStackEntry.arguments?.getLong("addressId") ?: 1L
            val tokenName = backStackEntry.arguments?.getString("tokenName") ?: "USDT"

            SendFromWalletInfoScreen(
                addressId = addressId,
                tokenName = tokenName,
                goToBack = { navController.navigateUp() },
                goToSystemTRX = { navController.navigate(route = WalletInfo.WalletSystemTRX.route) }
            )
        }
    }
}

sealed class WalletInfo(val route: String) {
    object WalletSots : WalletInfo(route = "wallet_sot")
    object Send : WalletInfo(route = "send_wallet/{addressId}/{tokenName}") {
        fun createRoute(addressId: Long, tokenName: String): String {
            return "send_wallet/$addressId/$tokenName"
        }
    }
    object TXDetails : WalletInfo(route = "tx_details")
    object WalletSystem : WalletInfo(route = "wallet_system")
    object WalletSystemTRX : WalletInfo(route = "wallet_system_trx")
    object CentralAddressTxHistory : WalletInfo(route = "central_address_tx_history")
}

sealed class WalletSots(val route: String) {
    object WalletAddress : WalletSots(route = "wallet_address")
    object WalletArchivalSots : WalletSots(route = "wallet_archival_sots")
}

sealed class WalletAddress(val route: String) {
    object Receive : WalletAddress(route = "receive_wallet")
    object Send : WalletAddress(route = "send_from_wallet_address/{addressId}/{tokenName}") {
        fun createRoute(addressId: Long, tokenName: String): String {
            return "send_from_wallet_address/$addressId/$tokenName"
        }
    }
}

sealed class WalletSystem(val route: String) {
    object CoRAFromWS : WalletSystem(route = "c_o_r_a_from_wallet_system")
}
