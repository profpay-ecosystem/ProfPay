package com.example.telegramWallet.backend.http.models.binance

import kotlinx.serialization.Serializable

@Serializable
data class BinancePriceConverterResponse(
    val symbol: String,
    val price: Double,
)
