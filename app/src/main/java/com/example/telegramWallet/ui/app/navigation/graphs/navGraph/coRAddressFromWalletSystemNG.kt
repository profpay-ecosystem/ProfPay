package com.example.telegramWallet.ui.app.navigation.graphs.navGraph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.telegramWallet.ui.app.navigation.graphs.Graph
import com.example.telegramWallet.ui.screens.createOrRecoveryWallet.CreateNewWalletScreen
import com.example.telegramWallet.ui.screens.createOrRecoveryWallet.CreateOrRecoverWalletScreen
import com.example.telegramWallet.ui.screens.createOrRecoveryWallet.CreatedWalletAddingScreen
import com.example.telegramWallet.ui.screens.createOrRecoveryWallet.RecoverWalletScreen
import com.example.telegramWallet.ui.screens.createOrRecoveryWallet.RecoveringWalletAddingScreen
import com.example.telegramWallet.ui.screens.createOrRecoveryWallet.SeedPhraseConfirmationScreen

fun NavGraphBuilder.coRAddressFromWalletSystemNG(navController: NavHostController) {
    navigation(
        route = Graph.CreateOrRecoveryAddress.route,
        startDestination = WalletSystem.CoRAFromWS.route,
    ) {
        composable(route = WalletSystem.CoRAFromWS.route) {
            CreateOrRecoverWalletScreen(
                goToCreateNewWallet = {
                    navController.navigate(route = CreateOrRecoverWallet.CreateNewWallet.route)
                },
                goToRecoverWallet = {
                    navController.navigate(route = CreateOrRecoverWallet.RecoverWallet.route)
                },
                goToBack = { navController.navigateUp() },
            )
        }
        composable(route = CreateOrRecoverWallet.CreateNewWallet.route) {
            CreateNewWalletScreen(
                goToSeedPhraseConfirmation = {
                    navController.navigate(route = CreateNewWallet.SeedPhraseConfirmation.route)
                },
                goToBack = {
                    navController.navigateUp()
                },
            )
        }
        composable(route = CreateOrRecoverWallet.RecoverWallet.route) {
            RecoverWalletScreen(
                goToRecoveringWalletAdding = {
                    navController.navigate(route = RecoverWallet.RecoveringWalletAdding.route)
                },
                goToBack = {
                    navController.navigateUp()
                },
            )
        }
        composable(route = CreateNewWallet.SeedPhraseConfirmation.route) {
            SeedPhraseConfirmationScreen(
                goToWalletAdded = {
                    navController.navigate(route = SeedPhraseConfirmation.WalletAdded.route)
                },
                goToBack = {
                    navController.navigateUp()
                },
            )
        }
        composable(route = SeedPhraseConfirmation.WalletAdded.route) {
            CreatedWalletAddingScreen(
                goToHome = {
                    navController.navigate(route = Graph.Profile.route) {
                        popUpTo(route = Graph.Profile.route)
                    }
                },
                goToBack = {
                    navController.navigateUp()
                },
            )
        }
        composable(route = RecoverWallet.RecoveringWalletAdding.route) {
            RecoveringWalletAddingScreen(
                goToHome = {
                    navController.navigate(route = Graph.Profile.route) {
                        popUpTo(route = Graph.Profile.route)
                    }
                },
                goToBack = {
                    navController.navigateUp()
                },
            )
        }
    }
}

sealed class CreateOrRecoverWallet(
    val route: String,
) {
    data object CreateNewWallet : CreateOrRecoverWallet(route = "create_new_wallet")

    data object RecoverWallet : CreateOrRecoverWallet(route = "recover_new_wallet")
}

sealed class RecoverWallet(
    val route: String,
) {
    data object RecoveringWalletAdding : RecoverWallet(route = "recovering_wallet_adding")
}

sealed class CreateNewWallet(
    val route: String,
) {
    data object SeedPhraseConfirmation : CreateNewWallet(route = "seed_phrase_confirmation")
}

sealed class SeedPhraseConfirmation(
    val route: String,
) {
    data object WalletAdded : SeedPhraseConfirmation(route = "wallet_added")
}
