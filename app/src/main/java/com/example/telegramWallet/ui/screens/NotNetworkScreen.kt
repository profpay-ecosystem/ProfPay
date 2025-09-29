package com.example.telegramWallet.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.telegramWallet.R
import com.example.telegramWallet.ui.app.theme.LocalFontSize

@Composable
fun NotNetworkScreen() {
    Scaffold {
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(it),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                IsEthDisableBASFeature()
            }
        }
    }
}

@Composable
fun IsEthDisableBASFeature() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            modifier = Modifier.size(120.dp).padding(bottom = 20.dp),
            imageVector = ImageVector.vectorResource(id = R.drawable.icon_eth_disable),
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onPrimary,
        )
        Text(
            text = "Упс, что-то пошло не так",
            fontSize = LocalFontSize.Large.fS,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Text(text = "В данный момент доступ к серверу отсутствует. Пожалуйста, проверьте ваше соединение с интернетом.")
    }
}
