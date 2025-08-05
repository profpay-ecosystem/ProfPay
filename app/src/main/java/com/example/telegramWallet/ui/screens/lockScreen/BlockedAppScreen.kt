package com.example.telegramWallet.ui.screens.lockScreen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.BlockingAppState
import com.example.telegramWallet.bridge.view_model.BlockingAppViewModel
import com.example.telegramWallet.ui.app.theme.LocalFontSize
import com.example.telegramWallet.ui.shared.sharedPref

@Composable
fun BlockedAppScreen(toNavigate: () -> Unit, viewModel: BlockingAppViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IsEthDisableBASFeature()
                when (state) {
                    is BlockingAppState.Loading -> {}
                    is BlockingAppState.Success -> {
                        val resultState = (state as BlockingAppState.Success).value
                        Log.e("isBlockedApp", resultState.isBlockedApp.toString())
                        if (!resultState.isBlockedApp) {
                            toNavigate()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IsEthDisableBASFeature() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(120.dp).padding(bottom = 20.dp),
            imageVector = ImageVector.vectorResource(id = R.drawable.icon_eth_disable),
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = "Проблемы с подключением к Интернету...",
            fontSize = LocalFontSize.Large.fS,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Text(text = "Пожалуйста, проверьте есть ли у вас доступ к сети и перезайдите в приложение.")

    }
}