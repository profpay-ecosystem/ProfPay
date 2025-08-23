package com.example.walletcore.primitives

import kotlinx.serialization.Serializable

@Serializable
data class Account (
    val chain: Chain,
    val address: String,
    val derivationPath: String,
    val extendedPublicKey: String? = null
)
