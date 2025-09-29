package com.example.telegramWallet.backend.grpc.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class DataRiskScoreSignals(
    val atm: Float,
    @SerialName(value = "child_exploitation") val childExploitation: Float,
    @SerialName(value = "dark_market") val darkMarket: Float,
    @SerialName(value = "dark_service") val darkService: Float,
    @SerialName(value = "enforcement_action") val enforcementAction: Float,
    @SerialName(value = "exchange_fraudulent") val exchangeFraudulent: Float,
    @SerialName(value = "exchange_licensed") val exchangeLicensed: Float,
    @SerialName(value = "exchange_unlicensed") val exchangeUnlicensed: Float,
    val gambling: Float,
    @SerialName(value = "illegal_service") val illegalService: Float,
    @SerialName(value = "liquidity_pools") val liquidityPools: Float,
    val marketplace: Float,
    val miner: Float,
    val mixer: Float,
    val other: Float,
    @SerialName(value = "p2p_exchange_licensed") val p2pExchangeLicensed: Float,
    @SerialName(value = "p2p_exchange_unlicensed") val p2pExchangeUnlicensed: Float,
    val payment: Float,
    val ransom: Float,
    val sanctions: Float,
    val scam: Float,
    @SerialName(value = "seized_assets") val seizedAssets: Float,
    @SerialName(value = "stolen_coins") val stolenCoins: Float,
    @SerialName(value = "terrorism_financing") val terrorismFinancing: Float,
    val wallet: Float,
)
