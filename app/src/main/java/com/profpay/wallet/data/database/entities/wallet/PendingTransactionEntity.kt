package com.profpay.wallet.data.database.entities.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigInteger

@Entity(
    tableName = "pending_transaction",
    foreignKeys = [
        ForeignKey(
            entity = TokenEntity::class,
            parentColumns = ["token_id"],
            childColumns = ["token_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("token_id")],
)
data class PendingTransactionEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long? = null,
    @ColumnInfo(name = "token_id") val tokenId: Long,
    @ColumnInfo(name = "tx_id") val txid: String,
    @ColumnInfo(name = "amount") val amount: BigInteger,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "ttl_mills") val ttlMillis: Long = 15 * 60 * 1000, // TTL 15 минут
)
