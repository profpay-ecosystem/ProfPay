package com.profpay.wallet.tron.http.models

import kotlinx.serialization.Serializable

@Serializable
data class Trc20TransactionsResponse(
    val data: List<Trc20TransactionsDataResponse>,
    val success: Boolean,
    val meta: Trc20TransactionsMetaResponse,
)

@Serializable
data class Trc20TransactionsDataResponse(
    val transaction_id: String,
    val token_info: Trc20TransactionsDataTokenInfoResponse,
    val block_timestamp: Long,
    val from: String,
    val to: String,
    val type: String,
    val value: String,
)

@Serializable
data class Trc20TransactionsDataTokenInfoResponse(
    val symbol: String,
    val address: String,
    val decimals: Int,
    val name: String,
)

@Serializable
data class Trc20TransactionsMetaResponse(
    val at: Long,
    val page_size: Long,
)
