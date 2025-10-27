package com.profpay.wallet.ui.feature.wallet.send.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.bridge.viewmodel.wallet.send.SendFromWalletViewModel
import com.profpay.wallet.ui.extensions.protectFromTapjacking
import com.profpay.wallet.ui.feature.wallet.send.bottomsheet.model.rememberTransferConfirmationState

@Composable
fun ContentBottomSheetTransferConfirmation(
    viewModel: SendFromWalletViewModel = hiltViewModel(),
    isDetails: Boolean,
    modelTransferFromBS: ModelTransferFromBS,
    closeBS: () -> Unit,
) {
    val uiState = rememberTransferConfirmationState(viewModel, modelTransferFromBS)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AmountForBSTransferConfirmation(
            amount = modelTransferFromBS.amount.toString(),
            amountUSD = uiState.amountUSD,
            tokenShortName = modelTransferFromBS.tokenName.shortName
        )

        CardInfoForBSTransferConfirmation(
            tokenNameModel = modelTransferFromBS.tokenName,
            addressSender = modelTransferFromBS.addressSender,
            addressReceiver = modelTransferFromBS.addressReceiver
        )

        CardFeesAndTotalForBSTransferConfirmation(
            commission = modelTransferFromBS.commission,
            trxToUsdtRate = uiState.trxToUsdtRate,
            isActivated = uiState.isActivated,
            createNewAccountFee = uiState.createNewAccountFee,
            totalAmount = modelTransferFromBS.amount
        )

        if (!isDetails) {
            ConfirmationButtonForBSTransferConfirmation(
                modifier = Modifier.protectFromTapjacking(),
                isEnabled = uiState.isConfirmButtonEnabled,
                showContractWarning = uiState.showContractWarning,
                onConfirm = uiState.onConfirm,
                onShowContractWarning = uiState.setShowContractWarning,
                onClose = closeBS
            )
        }
    }
}
