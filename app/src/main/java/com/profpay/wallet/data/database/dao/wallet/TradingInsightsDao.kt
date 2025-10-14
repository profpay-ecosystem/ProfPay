package com.profpay.wallet.data.database.dao.wallet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.profpay.wallet.data.database.entities.wallet.TradingInsightsEntity

@Dao
interface TradingInsightsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tradingInsightsEntity: TradingInsightsEntity): Long

    @Query("SELECT EXISTS(SELECT * FROM trading_insights WHERE symbol = :symbol)")
    suspend fun doesSymbolExist(symbol: String): Boolean

    @Query("UPDATE trading_insights SET price_change_percentage_24h = :priceChangePercentage24h WHERE symbol = :symbol")
    suspend fun updatePriceChangePercentage24h(
        symbol: String,
        priceChangePercentage24h: Double,
    )

    @Query("SELECT price_change_percentage_24h FROM trading_insights WHERE symbol = :symbol")
    suspend fun getPriceChangePercentage24h(symbol: String): Double
}
