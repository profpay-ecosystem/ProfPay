package com.example.walletcore.primitives

import kotlinx.serialization.Serializable

@Serializable
data class ScanAddressTarget (
    val chain: Chain,
    val address: String
)

@Serializable
data class ScanTransaction (
    val isMalicious: Boolean,
    val isMemoRequired: Boolean
)

@Serializable
data class ScanTransactionPayload (
    val deviceId: String,
    val walletIndex: UInt,
    val origin: ScanAddressTarget,
    val target: ScanAddressTarget,
    val website: String? = null,
    val type: TransactionType
)