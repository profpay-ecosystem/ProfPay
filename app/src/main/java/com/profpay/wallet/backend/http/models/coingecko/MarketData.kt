package com.profpay.wallet.backend.http.models.coingecko

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarketData(
    @SerialName(value = "price_change_percentage_24h") val priceChangePercentage24h: Double,
)
