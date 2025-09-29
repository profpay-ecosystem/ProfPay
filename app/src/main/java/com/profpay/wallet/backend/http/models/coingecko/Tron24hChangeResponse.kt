package com.profpay.wallet.backend.http.models.coingecko

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tron24hChangeResponse(
    @SerialName(value = "market_data") val marketData: MarketData,
)
