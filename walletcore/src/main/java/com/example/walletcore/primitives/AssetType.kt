package com.example.walletcore.primitives

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AssetSubtype(val string: String) {
    @SerialName("NATIVE")
    NATIVE("NATIVE"),
    @SerialName("TOKEN")
    TOKEN("TOKEN"),
}

@Serializable
enum class AssetType(val string: String) {
    @SerialName("NATIVE")
    NATIVE("NATIVE"),
    @SerialName("TRC20")
    TRC20("TRC20"),
}