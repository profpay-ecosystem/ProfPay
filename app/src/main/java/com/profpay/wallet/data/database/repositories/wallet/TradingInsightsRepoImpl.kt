package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.TradingInsightsDao
import com.profpay.wallet.data.database.entities.wallet.TradingInsightsEntity
import javax.inject.Inject
import javax.inject.Singleton

interface TradingInsightsRepo {
    suspend fun insert(tradingInsightsEntity: TradingInsightsEntity): Long

    suspend fun doesSymbolExist(symbol: String): Boolean

    suspend fun updatePriceChangePercentage24h(
        symbol: String,
        priceChangePercentage24h: Double,
    )

    suspend fun getPriceChangePercentage24h(symbol: String): Double
}

@Singleton
class TradingInsightsRepoImpl @Inject constructor(
    private val tradingInsightsDao: TradingInsightsDao,
) : TradingInsightsRepo {
    override suspend fun insert(tradingInsightsEntity: TradingInsightsEntity): Long =
        tradingInsightsDao.insert(tradingInsightsEntity)

    override suspend fun doesSymbolExist(symbol: String): Boolean =
        tradingInsightsDao.doesSymbolExist(symbol)

    override suspend fun updatePriceChangePercentage24h(
        symbol: String,
        priceChangePercentage24h: Double,
    ) = tradingInsightsDao.updatePriceChangePercentage24h(symbol, priceChangePercentage24h)

    override suspend fun getPriceChangePercentage24h(symbol: String): Double =
        tradingInsightsDao.getPriceChangePercentage24h(symbol)
}
