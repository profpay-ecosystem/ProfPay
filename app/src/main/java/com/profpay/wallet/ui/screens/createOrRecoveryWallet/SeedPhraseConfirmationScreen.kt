package com.profpay.wallet.ui.screens.createOrRecoveryWallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.R
import com.profpay.wallet.bridge.view_model.create_or_recovery_wallet.SeedPhraseConfirmationState
import com.profpay.wallet.bridge.view_model.create_or_recovery_wallet.SeedPhraseConfirmationViewModel
import com.profpay.wallet.ui.widgets.SeedPhraseConfirmationWidget

@Composable
fun SeedPhraseConfirmationScreen(
    goToWalletAdded: () -> Unit,
    viewModel: SeedPhraseConfirmationViewModel = hiltViewModel(),
    goToBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Текущая клавиатура
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .clickable { keyboardController?.hide() },
        ) {}
        when (state) {
            is SeedPhraseConfirmationState.Loading ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .paint(
                            painterResource(id = R.drawable.create_recovery_bg),
                            contentScale = ContentScale.FillBounds,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            is SeedPhraseConfirmationState.Success ->
                SeedPhraseConfirmationWidget(
                    addressGenerateResult = (state as SeedPhraseConfirmationState.Success).addressGenerateResult,
                    goToBack = goToBack,
                    goToWalletAdded = goToWalletAdded,
                )
        }
    }
}
