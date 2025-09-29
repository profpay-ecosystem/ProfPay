package com.profpay.wallet.models.pushy

import kotlinx.serialization.Serializable

@Serializable
data class PushyDeployContractSuccessfullyMessage(
    val address: String,
    val contractAddress: String,
    val transactionId: String,
)
