package com.example.walletcore.primitives

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType(val string: String) {
    @SerialName("transfer")
    Transfer("transfer"),
}