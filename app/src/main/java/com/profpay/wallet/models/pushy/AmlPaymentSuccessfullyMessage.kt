package com.profpay.wallet.models.pushy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AmlPaymentSuccessfullyMessage(
    @SerialName("txid")
    val transactionId: String,
)
