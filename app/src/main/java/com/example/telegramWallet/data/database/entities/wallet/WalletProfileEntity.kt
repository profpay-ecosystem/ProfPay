package com.example.telegramWallet.data.database.entities.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallet_profile",
)
data class WalletProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "entropy") val entropy: ByteArray? = null,
    @ColumnInfo(name = "priv_key_bytes") val privKeyBytes: ByteArray,
    @ColumnInfo(name = "chain_code") val chainCode: ByteArray,
)
