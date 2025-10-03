package com.profpay.wallet.backend.http.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CrystalMonitorTxAddRequest(
    @SerialName("token_id") val tokenId: Long, // 9 usdt, 0 trx - wtf?
    val tx: String, // transaction hash
    val direction: String, // deposit
    val address: String, // customer address
    val name: String, // customer name
    val currency: String, // trx
)

@Serializable
data class AmlStatisticsModelResponse(
    val result: Boolean,
    val message: String,
    @SerialName("check_id") val checkId: Long,
)
