package com.profpay.wallet.ui.feature.wallet.walletAddress.model

import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.database.entities.wallet.TransactionType

fun TransactionEntity.toUiModel(
    typeTransaction: Int,
    address: String,
    addressWa: String = "",
    isGeneralAddressReceive: Boolean = false,
): CardTransactionUiModel {
    val title = when (typeTransaction) {
        TransactionType.SEND.index -> "Отправлено"
        TransactionType.RECEIVE.index -> "Получено"
        TransactionType.BETWEEN_YOURSELF.index -> "Между своими"
        else -> ""
    }

    val details = when (typeTransaction) {
        TransactionType.SEND.index -> "Куда: ${address.take(5)}...${address.takeLast(5)}"
        TransactionType.RECEIVE.index -> "Откуда: ${address.take(5)}...${address.takeLast(5)}"
        TransactionType.BETWEEN_YOURSELF.index ->
            "Откуда: ${senderAddress.take(5)}...${senderAddress.takeLast(5)}\n" +
                    "Куда: ${receiverAddress.take(5)}...${receiverAddress.takeLast(5)}"
        else -> ""
    }

    val betweenYourselfReceiver =
        typeTransaction == TransactionType.BETWEEN_YOURSELF.index && receiverAddress == addressWa

    val showGeneralReceiveCard =
        (!isGeneralAddressReceive && typeTransaction == TransactionType.RECEIVE.index && !isProcessed) ||
                (!isGeneralAddressReceive && betweenYourselfReceiver && !isProcessed)

    return CardTransactionUiModel(
        title = title,
        details = details,
        showGeneralReceiveCard = showGeneralReceiveCard,
    )
}