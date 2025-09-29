package com.example.telegramWallet.data.database.entities.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trading_insights",
    indices = [
        Index(
            value = ["symbol"],
            name = "trading_insights_index_symbol",
            unique = true,
        ),
    ],
)
data class TradingInsightsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ColumnInfo(name = "symbol") val symbol: String,
    @ColumnInfo(name = "price_change_percentage_24h") val priceChangePercentage24h: Double,
)
