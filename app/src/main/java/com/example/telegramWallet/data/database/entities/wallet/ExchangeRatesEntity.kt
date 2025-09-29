package com.example.telegramWallet.data.database.entities.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exchange_rates",
    indices = [
        Index(
            value = ["symbol"],
            name = "exchange_data_index_symbol",
            unique = true,
        ),
    ],
)
data class ExchangeRatesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ColumnInfo(name = "symbol") val symbol: String,
    @ColumnInfo(name = "value") val value: Double,
)
