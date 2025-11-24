package com.profpay.wallet.ui.feature.wallet.send.bottomsheet.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.profpay.wallet.bridge.viewmodel.wallet.send.SendFromWalletViewModel
import com.profpay.wallet.ui.feature.wallet.send.bottomsheet.ModelTransferFromBS
import com.profpay.wallet.utils.decimalFormat
import java.math.BigDecimal
import java.math.BigInteger


@Composable
internal fun rememberTransferConfirmationState(
    viewModel: SendFromWalletViewModel,
    modelTransferFromBS: ModelTransferFromBS
): TransferConfirmationState {
    val tokenNameModel = modelTransferFromBS.tokenName
    val isContractAddress = viewModel.isContractAddress.collectAsState().value
    val trxToUsdtRate by viewModel.trxToUsdtRate.collectAsState()
    val isActivated by viewModel.isActivated.collectAsState()

    val (showContractWarning, setShowContractWarning) = remember { mutableStateOf(false) }
    val isConfirmButtonEnabled = remember { mutableStateOf(true) }
    val (createNewAccountFee, setCreateNewAccountFee) = remember { mutableStateOf(BigInteger.ZERO) }

    val onConfirmTransaction: () -> Unit = { viewModel.onConfirmTransaction(modelTransferFromBS) }

    // Эффекты вынесены в отдельные блоки
    LaunchedEffect(isActivated) {
        if (isActivated) return@LaunchedEffect
        setCreateNewAccountFee(
            viewModel.tron.addressUtilities.getCreateNewAccountFeeInSystemContract()
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadTrxToUsdtRate()
        viewModel.checkActivation(modelTransferFromBS.addressReceiver)
        viewModel.verifyContractAddress(modelTransferFromBS.addressReceiver)
    }

    LaunchedEffect(isContractAddress) {
        setShowContractWarning(isContractAddress)
    }

    val amountUSD = calculateAmountUSD(
        amount = modelTransferFromBS.amount,
        tokenShortName = tokenNameModel.shortName,
        trxToUsdtRate = trxToUsdtRate
    )

    return TransferConfirmationState(
        amountUSD = amountUSD,
        trxToUsdtRate = trxToUsdtRate,
        isActivated = isActivated,
        isContractAddress = isContractAddress,
        showContractWarning = showContractWarning,
        isConfirmButtonEnabled = isConfirmButtonEnabled.value,
        createNewAccountFee = createNewAccountFee,
        setShowContractWarning = setShowContractWarning,
        setConfirmButtonEnabled = { isConfirmButtonEnabled.value = it },
        onConfirm = onConfirmTransaction,
        onClickConfirm = {
            handleConfirmationClick(
            isEnabled = isConfirmButtonEnabled.value,
            isContractAddress = isContractAddress,
            onConfirm = onConfirmTransaction,
            onShowContractWarning = setShowContractWarning
        )}
    )
}

// Data class для состояния
internal data class TransferConfirmationState(
    val amountUSD: String,
    val trxToUsdtRate: BigDecimal,
    val isActivated: Boolean,
    val isContractAddress: Boolean,
    val showContractWarning: Boolean,
    val isConfirmButtonEnabled: Boolean,
    val createNewAccountFee: BigInteger,
    val setShowContractWarning: (Boolean) -> Unit,
    val setConfirmButtonEnabled: (Boolean) -> Unit,
    val onClickConfirm: () -> Unit,
    val onConfirm: () -> Unit,
)

// Вспомогательные функции
private fun calculateAmountUSD(amount: BigDecimal, tokenShortName: String, trxToUsdtRate: BigDecimal): String {
    return if (tokenShortName == "TRX") {
        decimalFormat(amount * trxToUsdtRate)
    } else {
        decimalFormat(amount)
    }
}
private fun handleConfirmationClick(
    isEnabled: Boolean,
    isContractAddress: Boolean,
    onConfirm: () -> Unit,
    onShowContractWarning: (Boolean) -> Unit
) {
    if (!isEnabled) return

    if (isContractAddress) {
        onShowContractWarning(true)
    } else {
        onConfirm()
    }
}
