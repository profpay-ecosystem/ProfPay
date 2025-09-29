package com.profpay.wallet.models.pushy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AmlPaymentErrorMessage(
    @SerialName("txid")
    val transactionId: String,
)
