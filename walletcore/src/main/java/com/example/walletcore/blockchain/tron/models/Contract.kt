package com.example.walletcore.blockchain.tron.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TronSmartContractCall (
    @SerialName("contract_address")
    val contractAddress: String,
    @SerialName("function_selector")
    val functionSelector: String,
    val parameter: String? = null,
    @SerialName("fee_limit")
    val feeLimit: UInt? = null,
    @SerialName("call_value")
    val callValue: UInt? = null,
    @SerialName("owner_address")
    val ownerAddress: String,
    val visible: Boolean? = null
)

@Serializable
data class TronSmartContractResultMessage (
    val result: Boolean,
    val message: String? = null
)

@Serializable
data class TronSmartContractResult (
    val result: TronSmartContractResultMessage,
    @SerialName("constant_result")
    val constantResult: List<String>,
    @SerialName("energy_used")
    val energyUsed: Int
)