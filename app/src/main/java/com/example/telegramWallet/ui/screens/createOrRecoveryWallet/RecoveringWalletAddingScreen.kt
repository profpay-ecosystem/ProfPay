package com.example.telegramWallet.ui.screens.createOrRecoveryWallet

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.PrefKeys
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.create_or_recovery_wallet.RecoverWalletState
import com.example.telegramWallet.bridge.view_model.create_or_recovery_wallet.RecoverWalletViewModel
import com.example.telegramWallet.bridge.view_model.create_or_recovery_wallet.WalletAddedViewModel
import com.example.telegramWallet.data.flow_db.repo.RecoveryResult
import com.example.telegramWallet.tron.AddressGenerateFromSeedPhr
import com.example.telegramWallet.ui.app.theme.BackgroundDark
import com.example.telegramWallet.ui.app.theme.BackgroundLight
import com.example.telegramWallet.ui.shared.sharedPref
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveringWalletAddingScreen(
    viewModel: RecoverWalletViewModel = hiltViewModel(),
    goToHome: () -> Unit,
    goToBack: () -> Unit,
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
            is RecoverWalletState.Loading ->
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

            is RecoverWalletState.Success -> {
                val successState = state as RecoverWalletState.Success
                when (val result = successState.addressRecoverResult) {
                    is RecoveryResult.Success -> {
                        RecoveringWalletAddingWidget(
                            addressRecoverResult = result.address,
                            goToHome = goToHome,
                            accountWasFound = result.accountWasFound,
                            userId = result.userId,
                            clearState = { viewModel.clearAddressFromMnemonic() },
                        )
                    }
                    is RecoveryResult.AddressNotFound -> { /* handle */ }
                    is RecoveryResult.Error -> { /* handle */ }
                    is RecoveryResult.InvalidMnemonic -> { /* handle */ }
                    is RecoveryResult.RepeatingMnemonic -> { /* handle */ }
                    is RecoveryResult.Empty -> { /* handle */ }
                }
            }
        }
    }
}

@Composable
fun RecoveringWalletAddingWidget(
    addressRecoverResult: AddressGenerateFromSeedPhr,
    accountWasFound: Boolean,
    userId: Long?,
    goToHome: () -> Unit,
    clearState: () -> Unit,
    viewModel: WalletAddedViewModel = hiltViewModel(),
) {
    val sharedPref = sharedPref()

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
                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        val addressesWithKeysForM = addressRecoverResult.addressesWithKeysForM
                        val deviceToken = sharedPref.getString(PrefKeys.DEVICE_TOKEN, null) ?: throw Exception("Device Token not found.")
                        if (accountWasFound && userId == null) throw Exception("User ID not found.")
                        val isFirstStarted = sharedPref.getBoolean(PrefKeys.FIRST_STARTED, true)

                        try {
                            if (isFirstStarted) {
                                if (accountWasFound && userId != null) {
                                    viewModel.registerUserDevice(
                                        userId = userId,
                                        deviceToken = deviceToken,
                                        sharedPref = sharedPref,
                                    )
                                    viewModel.insertNewCryptoAddresses(addressesWithKeysForM)
                                } else {
                                    viewModel.registerUserAccount(deviceToken = deviceToken, sharedPref = sharedPref)
                                    viewModel.createCryptoAddresses(addressesWithKeysForM)
                                    viewModel.insertNewCryptoAddresses(addressesWithKeysForM)
                                }
                                sharedPref.edit { putBoolean(PrefKeys.FIRST_STARTED, false) }
                            } else {
                                viewModel.createCryptoAddresses(addressesWithKeysForM)
                                viewModel.insertNewCryptoAddresses(addressesWithKeysForM)
                            }

                            withContext(Dispatchers.Main) {
                                clearState()
                                goToHome()
                            }
                        } catch (e: Exception) {
                            if (!sharedPref.getBoolean(PrefKeys.FIRST_STARTED, true)) {
                                sharedPref.edit { putBoolean(PrefKeys.FIRST_STARTED, true) }
                            }
                            clearState()
                            Sentry.captureException(e)
                        }
                    }
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
//        WalletCheckBoxFeature(
//            funcButton = {
//                viewModel.walletAddingProcessing(
//                    addressRecoverResult.addresses
//                )
//                goToHome()
//            },
//            textButton = "Принять и продолжить"
//        )
