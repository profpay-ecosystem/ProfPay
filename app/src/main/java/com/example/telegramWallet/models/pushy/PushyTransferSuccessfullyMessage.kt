package com.example.telegramWallet.models.pushy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushyTransferSuccessfullyMessage(
    @SerialName("txid")
    val txid: String,
    @SerialName("sender_address")
    val senderAddress: String,
    @SerialName("amount")
    val amount: Long,
    @SerialName("transaction_type")
    val transactionType: String,
)
