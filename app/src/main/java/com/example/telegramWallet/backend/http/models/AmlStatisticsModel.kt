package com.example.telegramWallet.backend.http.models

import kotlinx.serialization.Serializable

@Serializable
data class CrystalMonitorTxAddRequest(
    val token_id: Long, // 9 usdt, 0 trx - wtf?
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
    val check_id: Long,
)
