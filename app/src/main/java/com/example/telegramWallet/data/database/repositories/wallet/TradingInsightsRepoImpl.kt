package com.example.telegramWallet.data.database.repositories.wallet

import com.example.telegramWallet.data.database.dao.wallet.TradingInsightsDao
import com.example.telegramWallet.data.database.entities.wallet.TradingInsightsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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
class TradingInsightsRepoImpl
    @Inject
    constructor(
        private val tradingInsightsDao: TradingInsightsDao,
    ) : TradingInsightsRepo {
        override suspend fun insert(tradingInsightsEntity: TradingInsightsEntity): Long {
            return withContext(Dispatchers.IO) {
                return@withContext tradingInsightsDao.insert(tradingInsightsEntity)
            }
        }

        override suspend fun doesSymbolExist(symbol: String): Boolean {
            return withContext(Dispatchers.IO) {
                return@withContext tradingInsightsDao.doesSymbolExist(symbol)
            }
        }

        override suspend fun updatePriceChangePercentage24h(
            symbol: String,
            priceChangePercentage24h: Double,
        ) {
            return withContext(Dispatchers.IO) {
                return@withContext tradingInsightsDao.updatePriceChangePercentage24h(symbol, priceChangePercentage24h)
            }
        }

        override suspend fun getPriceChangePercentage24h(symbol: String): Double {
            return withContext(Dispatchers.IO) {
                return@withContext tradingInsightsDao.getPriceChangePercentage24h(symbol)
            }
        }
    }
