package com.profpay.wallet.ui.screens.wallet

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.bridge.view_model.dto.TokenName
import com.profpay.wallet.bridge.view_model.wallet.send.SendFromWalletViewModel
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.feature.wallet.send.ButtonSendFeature
import com.profpay.wallet.ui.feature.wallet.send.CardWithAddressForSendFromWallet
import com.profpay.wallet.ui.feature.wallet.send.RowSumForSendFeature
import com.profpay.wallet.ui.feature.wallet.send.SendWarningTextFeature
import com.profpay.wallet.ui.feature.wallet.send.TextFieldForSendFeature
import com.profpay.wallet.ui.feature.wallet.send.bottomsheet.ModelTransferFromBS
import com.profpay.wallet.ui.feature.wallet.send.bottomsheet.bottomSheetTransferConfirmation
import kotlinx.coroutines.FlowPreview
import rememberStackedSnackbarHostState
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun SendFromWalletInfoScreen(
    addressId: Long,
    tokenName: String,
    viewModel: SendFromWalletViewModel = hiltViewModel(),
    goToBack: () -> Unit,
    goToSystemTRX: () -> Unit,
) {
    val stackedSnackbarHostState = rememberStackedSnackbarHostState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val tokenNameModel = TokenName.valueOf(tokenName)
    val currentTokenName = TokenName.entries.find { it.tokenName == tokenName } ?: TokenName.USDT

    var addressSending by rememberSaveable { mutableStateOf("") }
    var sumSending by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadAddressWithTokens(
            addressId,
            tokenNameModel.blockchainName,
            currentTokenName.tokenName,
        )
    }

    LaunchedEffect(uiState.isAddressActivated) {
        if (!uiState.isAddressActivated) {
            stackedSnackbarHostState.showErrorSnackbar(
                title = "Перевод валюты невозможен",
                description = "Для активации необходимо перейти в «Системный TRX»",
                actionTitle = "Перейти",
                action = { goToSystemTRX() },
            )
        }
    }

    LaunchedEffect(addressSending, sumSending) {
        viewModel.updateInputs(addressSending, sumSending, currentTokenName)
    }

    LaunchedEffect(viewModel.stateCommission.collectAsStateWithLifecycle().value) {
        viewModel.onCommissionResult(viewModel.stateCommission.value)
    }

    val (_, setIsOpenTransferProcessingSheet) =
        bottomSheetTransferConfirmation(
            modelTransferFromBS =
                ModelTransferFromBS(
                    amount =
                        sumSending.takeIf { it.isNotBlank() }?.toBigDecimalOrNull()
                            ?: BigDecimal.ZERO,
                    tokenName = tokenNameModel,
                    addressReceiver = addressSending,
                    addressSender = uiState.addressWithTokens?.addressEntity?.address ?: "",
                    commission = uiState.commission,
                    addressWithTokens = uiState.addressWithTokens,
                    commissionResult = uiState.commissionResult,
                ),
            snackbar = stackedSnackbarHostState,
        )


    CustomScaffoldWallet(
        stackedSnackbarHostState = stackedSnackbarHostState,
        keyboardController = keyboardController,
        focusManager = focusManager,
    ) { bottomPadding ->
        CustomTopAppBar(title = "Transfer", goToBack = { goToBack() })

        CustomBottomCard(
            modifier = Modifier.weight(0.8f),
            modifierColumn = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            bottomPadding = bottomPadding,
        ) {
            CardWithAddressForSendFromWallet(
                title = "Адрес получения",
                addressSending = addressSending,
                onAddressChange = { addressSending = it },
                warningAddress = !uiState.isValidRecipientAddress && addressSending != "",
            )
            RowSumForSendFeature(
                tokenBalance = uiState.tokenBalance.toTokenAmount().toString(),
                currentTokenName = currentTokenName
            )

            TextFieldForSendFeature(
                sumSending = sumSending,
                currentTokenName = currentTokenName,
                tokenBalance = uiState.tokenBalance.toTokenAmount().toString(),
                onSumChange = { sumSending = it }
            )

            SendWarningTextFeature(uiState.warning)
            ButtonSendFeature(
                isButtonEnabled = uiState.isButtonEnabled,
                onClick = {
                    if (viewModel.tron.addressUtilities.isValidTronAddress(addressSending)) {
                        setIsOpenTransferProcessingSheet(true)
                    }
                }
            )
        }
    }
}

