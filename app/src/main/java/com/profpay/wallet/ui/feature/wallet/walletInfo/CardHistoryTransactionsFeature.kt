package com.profpay.wallet.ui.feature.wallet.walletInfo

import androidx.compose.runtime.Composable
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.ui.components.feature.transaction.TransactionCard
import com.profpay.wallet.ui.feature.wallet.walletAddress.model.toUiModel

@Composable
fun CardHistoryTransactionsFeature(
    address: String,
    typeTransaction: Int,
    paintIconId: Int,
    amount: String,
    shortNameToken: String,
    transactionEntity: TransactionEntity,
    onClick: () -> Unit = {},
) {
    val uiModel =
        transactionEntity.toUiModel(
            typeTransaction = typeTransaction,
            address = address,
        )
    TransactionCard(
        title = uiModel.title,
        details = uiModel.details,
        amount = "$amount $shortNameToken",
        iconRes = paintIconId,
        onClick = onClick,
    )
}
