package com.example.telegramWallet.ui.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.telegramWallet.bridge.view_model.BlockingAppState
import com.example.telegramWallet.bridge.view_model.BlockingAppViewModel
import com.example.telegramWallet.data.services.NetworkMonitor
import com.example.telegramWallet.ui.app.navigation.graphs.RootNavigationGraph

@Composable
fun MyApp(
    navController: NavHostController,
    networkMonitor: NetworkMonitor,
    viewModel: BlockingAppViewModel = hiltViewModel(),
) {
    val blockAppState by viewModel.state.collectAsStateWithLifecycle()
    when (blockAppState) {
        is BlockingAppState.Loading -> viewModel.getBlockedAppState()
        is BlockingAppState.Success -> {
            viewModel.getBlockedAppState()
            RootNavigationGraph(
                navController = navController,
                networkMonitor = networkMonitor
            )
        }
    }
}