package com.profpay.wallet.ui.feature.wallet.transaction

import androidx.compose.runtime.Composable
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.ui.components.feature.transaction.TransactionCard

@Composable
fun CACardHistoryTransactionsFeature(
    paintIconId: Int,
    amount: String,
    shortNameToken: String,
    transactionEntity: TransactionEntity,
) {
    val (title, details) =
        "Получено" to "Откуда: ${transactionEntity.senderAddress.take(5)}...${
            transactionEntity.senderAddress.takeLast(
                5
            )
        }"
    TransactionCard(
        title = title,
        details = details,
        amount = "$amount $shortNameToken",
        iconRes = paintIconId,
    )
}
