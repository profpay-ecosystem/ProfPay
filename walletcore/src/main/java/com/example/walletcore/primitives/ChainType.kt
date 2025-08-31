package com.example.walletcore.primitives

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ChainType(val string: String) {
    @SerialName("tron")
    Tron("tron"),
}