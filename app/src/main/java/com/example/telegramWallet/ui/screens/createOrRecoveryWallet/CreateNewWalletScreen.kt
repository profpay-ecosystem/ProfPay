package com.example.telegramWallet.ui.screens.createOrRecoveryWallet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.create_or_recovery_wallet.CreateNewWalletState
import com.example.telegramWallet.bridge.view_model.create_or_recovery_wallet.CreateNewWalletViewModel
import com.example.telegramWallet.ui.widgets.CreateNewWalletWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewWalletScreen(
    viewModel: CreateNewWalletViewModel = hiltViewModel(),
    goToSeedPhraseConfirmation: () -> Unit,
    goToBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold { padding ->
        Surface(modifier = Modifier.padding()) {
            when (state) {
                is CreateNewWalletState.Loading ->
                    Box(
                        Modifier.fillMaxSize().paint(
                            painterResource(id = R.drawable.create_recovery_bg),
                            contentScale = ContentScale.FillBounds,
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                is CreateNewWalletState.Success ->
                    CreateNewWalletWidget(
                        goToBack = goToBack,
                        goToSeedPhraseConfirmation = goToSeedPhraseConfirmation,
                        addressGenerateResult = (state as CreateNewWalletState.Success).addressGenerateResult,
                    )
                else -> {}
            }
        }
    }
}
