package com.profpay.wallet.ui.feature.wallet.walletAddress.horizontalListsTrans

import StackedSnakbarHostState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.ui.components.feature.transaction.TransactionCard
import com.profpay.wallet.ui.feature.wallet.tx_details.bottomSheet.bottomSheetTransOnGeneralReceipt
import com.profpay.wallet.ui.feature.wallet.walletAddress.model.toUiModel
import com.profpay.wallet.ui.shared.sharedPref

@Composable
fun CardHistoryTransactionsForWAFeature(
    viewModel: WalletAddressViewModel,
    transactionEntity: TransactionEntity,
    addressWithTokens: AddressWithTokens,
    address: String,
    typeTransaction: Int,
    paintIconId: Int,
    amount: String,
    shortNameToken: String,
    onClick: () -> Unit = {},
    goToSystemTRX: () -> Unit = {},
    stackedSnackbarHostState: StackedSnakbarHostState,
) {
    val sharedPref = sharedPref()
    val addressWa = sharedPref.getString(PrefKeys.ADDRESS_FOR_WALLET_ADDRESS, "")

    val isActivated by viewModel.isActivated.collectAsState()
    val isGeneralAddressReceive by viewModel.isGeneralAddress.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkActivation(transactionEntity.receiverAddress)
        viewModel.isGeneralAddress(transactionEntity.receiverAddress)
    }

    val uiModel = transactionEntity.toUiModel(
        typeTransaction = typeTransaction,
        address = address,
        addressWa = addressWa ?: "",
        isGeneralAddressReceive = isGeneralAddressReceive,
    )

    val (_, setIsOpenTransOnGeneralReceiptSheet) =
        bottomSheetTransOnGeneralReceipt(
            addressWithTokens = addressWithTokens,
            snackbar = stackedSnackbarHostState,
            tokenName = transactionEntity.tokenName,
            walletId = transactionEntity.walletId,
            balance = transactionEntity.amount,
        )

    TransactionCard(
        title = uiModel.title,
        details = uiModel.details,
        amount = "$amount $shortNameToken",
        iconRes = paintIconId,
        onClick = onClick,
        extraContent = {
            if (uiModel.showGeneralReceiveCard) {
                GeneralReceiveCardButtonFeature(
                    isActivated = isActivated,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                    goToSystemTRX = goToSystemTRX,
                    setIsOpenTransOnGeneralReceiptSheet = setIsOpenTransOnGeneralReceiptSheet,
                )
            }
        },
    )
}

@Composable
private fun GeneralReceiveCardButtonFeature(
    isActivated: Boolean,
    stackedSnackbarHostState: StackedSnakbarHostState,
    goToSystemTRX: () -> Unit,
    setIsOpenTransOnGeneralReceiptSheet: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth()
            .shadow(7.dp, RoundedCornerShape(7.dp))
            .clickable {
                if (!isActivated) {
                    stackedSnackbarHostState.showErrorSnackbar(
                        title = "Перевод валюты невозможен",
                        description = "Для активации необходимо перейти в «Системный TRX»",
                        actionTitle = "Перейти",
                        action = { goToSystemTRX() },
                    )
                } else {
                    setIsOpenTransOnGeneralReceiptSheet(true)
                }
            },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Принять на Главный адрес",
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
