package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.ExchangeRatesDao
import com.profpay.wallet.data.database.entities.wallet.ExchangeRatesEntity
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
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
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ExchangeRatesRepo {
        override suspend fun insert(exchangeRatesEntity: ExchangeRatesEntity): Long {
            return withContext(ioDispatcher) {
                return@withContext exchangeRatesDao.insert(exchangeRatesEntity)
            }
        }

        override suspend fun doesSymbolExist(symbol: String): Boolean {
            return withContext(ioDispatcher) {
                return@withContext exchangeRatesDao.doesSymbolExist(symbol)
            }
        }

        override suspend fun updateExchangeRate(
            symbol: String,
            value: Double,
        ) {
            return withContext(ioDispatcher) {
                return@withContext exchangeRatesDao.updateExchangeRate(symbol, value)
            }
        }

        override suspend fun getExchangeRateValue(symbol: String): Double {
            return withContext(ioDispatcher) {
                return@withContext exchangeRatesDao.getExchangeRateValue(symbol)
            }
        }
    }
