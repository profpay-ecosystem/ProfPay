package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.ExchangeRatesDao
import com.profpay.wallet.data.database.entities.wallet.ExchangeRatesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface ExchangeRatesRepo {
    suspend fun insert(exchangeRatesEntity: ExchangeRatesEntity): Long

    suspend fun doesSymbolExist(symbol: String): Boolean

    suspend fun updateExchangeRate(
        symbol: String,
        value: Double,
    )

    suspend fun getExchangeRateValue(symbol: String): Double
}

@Singleton
class ExchangeRatesRepoImpl
    @Inject
    constructor(
        private val exchangeRatesDao: ExchangeRatesDao,
    ) : ExchangeRatesRepo {
        override suspend fun insert(exchangeRatesEntity: ExchangeRatesEntity): Long {
            return withContext(Dispatchers.IO) {
                return@withContext exchangeRatesDao.insert(exchangeRatesEntity)
            }
        }

        override suspend fun doesSymbolExist(symbol: String): Boolean {
            return withContext(Dispatchers.IO) {
                return@withContext exchangeRatesDao.doesSymbolExist(symbol)
            }
        }

        override suspend fun updateExchangeRate(
            symbol: String,
            value: Double,
        ) {
            return withContext(Dispatchers.IO) {
                return@withContext exchangeRatesDao.updateExchangeRate(symbol, value)
            }
        }

        override suspend fun getExchangeRateValue(symbol: String): Double {
            return withContext(Dispatchers.IO) {
                return@withContext exchangeRatesDao.getExchangeRateValue(symbol)
            }
        }
    }
