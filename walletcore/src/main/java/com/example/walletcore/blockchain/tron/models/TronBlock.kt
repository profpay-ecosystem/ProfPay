package com.example.walletcore.blockchain.tron.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TronHeader (
    val number: Long,
    val version: Long,
    val txTrieRoot: String,
    @SerialName("witness_address")
    val witnessAddress: String,
    val parentHash: String,
    val timestamp: Long
)

@Serializable
data class TronHeaderRawData (
    @SerialName("raw_data")
    val rawData: TronHeader
)

@Serializable
data class TronBlock (
    @SerialName("block_header")
    val blockHeader: TronHeaderRawData
)