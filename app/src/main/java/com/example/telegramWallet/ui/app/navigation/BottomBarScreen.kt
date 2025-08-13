package com.example.telegramWallet.ui.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.telegramWallet.R
import com.example.telegramWallet.ui.app.navigation.graphs.Graph
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.SettingsS
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.WalletAddress
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.WalletInfo
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.WalletSots

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: @Composable () -> ImageVector,
    val routes: List<String>
) {
    data object SmartContractList : BottomBarScreen(
        route = "SMART",
        title = "Smart",
        icon = {
            ImageVector.vectorResource(id = R.drawable.icon_smart)
        },
        routes = listOf("SMART")
    )

    data object Profile : BottomBarScreen(
        route = Graph.Profile.route,
        title = "Wallet",
        icon = {
            ImageVector.vectorResource(id = R.drawable.icon_wallet)
        },
        routes = listOf(
            "WALLET",
            WalletInfo.WalletSots.route,
            WalletInfo.Send.route,
            WalletSots.WalletAddress.route,
            WalletAddress.Receive.route,
            WalletInfo.TXDetails.route,
            WalletInfo.WalletSystem.route,
            WalletAddress.Send.route,
            WalletInfo.WalletSystemTRX.route,
            WalletSots.WalletArchivalSots.route
        )
    )

    data object Settings : BottomBarScreen(
        route = Graph.Settings.route,
        title = "Settings",
        icon = {
            ImageVector.vectorResource(id = R.drawable.icon_settings)
        },
        routes = listOf(
            "SETTINGS",
            SettingsS.SettingsNotifications.route,
            SettingsS.SettingsSecurity.route,
            SettingsS.SettingsAccount.route,
        )
    )
}