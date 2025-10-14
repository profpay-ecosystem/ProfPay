package com.profpay.wallet.data.database.dao.wallet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.profpay.wallet.data.database.entities.wallet.ExchangeRatesEntity

@Dao
interface ExchangeRatesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exchangeRatesEntity: ExchangeRatesEntity): Long

    @Query("SELECT EXISTS(SELECT * FROM exchange_rates WHERE symbol = :symbol)")
    suspend fun doesSymbolExist(symbol: String): Boolean

    @Query("UPDATE exchange_rates SET value = :value WHERE symbol = :symbol")
    suspend fun updateExchangeRate(
        symbol: String,
        value: Double,
    )

    @Query("SELECT value FROM exchange_rates WHERE symbol = :symbol")
    suspend fun getExchangeRateValue(symbol: String): Double
}
