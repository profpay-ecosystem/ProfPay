package com.example.telegramWallet.data.database.entities.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_aml_transactions",
)
data class PendingAmlTransactionEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long? = null,
    @ColumnInfo(name = "tx_id") val txid: String,
    @ColumnInfo(name = "is_successful", defaultValue = "0") val isSuccessful: Boolean = false,
    @ColumnInfo(name = "is_error", defaultValue = "0") val isError: Boolean = false,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "ttl_mills") val ttlMillis: Long = 15 * 60 * 1000, // TTL 15 минут
)