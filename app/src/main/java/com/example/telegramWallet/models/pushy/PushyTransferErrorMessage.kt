package com.example.telegramWallet.models.pushy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushyTransferErrorMessage(
    @SerialName("address")
    val senderAddress: String,
    @SerialName("amount")
    val amount: Long,
    @SerialName("trs_type")
    val transactionType: String,
    @SerialName("tx_id")
    val transactionId: String,
)
