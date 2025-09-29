package com.profpay.wallet.tron.http.models

import kotlinx.serialization.Serializable

@Serializable
data class TrxTransactionResponse(
    val data: List<TrxTransactionDataResponse>,
)

@Serializable
data class TrxTransactionDataResponse(
    val ret: List<TrxTransactionDataRetResponse>,
    val signature: List<String>,
    val txID: String,
    val net_usage: Long,
    val raw_data_hex: String,
    val net_fee: Long,
    val energy_usage: Long,
    val blockNumber: Long,
    val block_timestamp: Long,
    val energy_fee: Long,
    val energy_usage_total: Long,
    val raw_data: TrxTransactionDataRawDataResponse,
)

@Serializable
data class TrxTransactionDataRetResponse(
    val contractRet: String,
    val fee: Long,
)

@Serializable
data class TrxTransactionDataRawDataResponse(
    val contract: List<TrxTransactionDataRawDataContractResponse>,
    val ref_block_bytes: String,
    val ref_block_hash: String,
    val expiration: Long,
    val timestamp: Long? = null,
)

@Serializable
data class TrxTransactionDataRawDataContractResponse(
    val parameter: TrxTransactionDataRawDataContractParameterResponse,
    val type: String,
)

@Serializable
data class TrxTransactionDataRawDataContractParameterResponse(
    val value: TrxTransactionDataRawDataContractParameterValueResponse,
    val type_url: String,
)

@Serializable
data class TrxTransactionDataRawDataContractParameterValueResponse(
    val amount: Long? = null,
    val owner_address: String,
    val to_address: String? = null,
)
