package com.profpay.wallet.ui.screens.createOrRecoveryWallet

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.R
import com.profpay.wallet.bridge.view_model.create_or_recovery_wallet.SeedPhraseConfirmationState
import com.profpay.wallet.bridge.view_model.create_or_recovery_wallet.SeedPhraseConfirmationViewModel
import com.profpay.wallet.bridge.view_model.create_or_recovery_wallet.WalletAddedViewModel
import com.profpay.wallet.tron.AddressGenerateResult
import com.profpay.wallet.ui.app.theme.BackgroundDark
import com.profpay.wallet.ui.app.theme.BackgroundLight
import com.profpay.wallet.ui.shared.sharedPref

@Composable
fun CreatedWalletAddingScreen(
    goToHome: () -> Unit,
    viewModel: SeedPhraseConfirmationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
        ) {}
        when (state) {
            is SeedPhraseConfirmationState.Loading ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .paint(
                            painterResource(id = R.drawable.create_recovery_bg_end),
                            contentScale = ContentScale.FillBounds,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

            is SeedPhraseConfirmationState.Success ->
                CreatedWalletAddingWidget(
                    addressGenerateResult = (state as SeedPhraseConfirmationState.Success).addressGenerateResult,
                    goToHome = goToHome,
                )
        }
    }
}

@Composable
fun CreatedWalletAddingWidget(
    addressGenerateResult: AddressGenerateResult,
    goToHome: () -> Unit,
    viewModel: WalletAddedViewModel = hiltViewModel(),
) {
    val sharedPref = sharedPref()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is WalletAddedViewModel.WalletUiEvent.NavigateToHome -> goToHome()
                is WalletAddedViewModel.WalletUiEvent.ShowError -> {
                    Log.e("WalletAdded", "Error event received")
                }
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .paint(
                    painterResource(id = R.drawable.create_recovery_bg_end),
                    contentScale = ContentScale.FillBounds,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 70.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                modifier = Modifier.size(170.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.icon_smart),
                contentDescription = "",
                tint = BackgroundLight,
            )
            Text(
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .padding(vertical = 18.dp),
                text = "Всё готово!",
                style = MaterialTheme.typography.displayMedium,
                color = BackgroundLight,
            )
            Button(
                onClick = {
                    viewModel.onWalletCreatedClicked(addressGenerateResult.addressesWithKeysForM, sharedPref)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BackgroundLight),
                modifier =
                    Modifier
                        .padding(vertical = 16.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(text = "Приступить к работе", style = MaterialTheme.typography.titleSmall, color = BackgroundDark)
            }
        }
    }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.97f),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "ProfPay",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = BackgroundDark,
        )
        Text(
            text = "ProfPay IO, 2024",
            style = MaterialTheme.typography.titleSmall,
            color = BackgroundDark,
        )
    }
}
