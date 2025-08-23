package com.example.walletcore.blockchain.tron.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TronChainParameter (
    val key: String,
    val value: Long? = null
)

@Serializable
data class TronChainParameters (
    val chainParameter: List<TronChainParameter>
)

@Serializable
enum class TronChainParameterKey(val string: String) {
    @SerialName("getCreateNewAccountFeeInSystemContract")
    GetCreateNewAccountFeeInSystemContract("getCreateNewAccountFeeInSystemContract"),
    @SerialName("getCreateAccountFee")
    GetCreateAccountFee("getCreateAccountFee"),
    @SerialName("getEnergyFee")
    GetEnergyFee("getEnergyFee"),
}