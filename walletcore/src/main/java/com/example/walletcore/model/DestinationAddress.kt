package com.example.walletcore.model

import kotlinx.serialization.Serializable

@Serializable
data class DestinationAddress(
    val address: String,
    val domainName: String? = null,
)