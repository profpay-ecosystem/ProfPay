package com.example.walletcore.primitives

import kotlinx.serialization.Serializable

@Serializable
data class Asset (
    val id: AssetId,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val type: AssetType
)
