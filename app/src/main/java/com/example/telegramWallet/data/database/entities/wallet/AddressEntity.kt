package com.example.telegramWallet.data.database.entities.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.telegramWallet.data.database.models.HasTronCredentials

@Entity(
    tableName = "addresses",
    indices = [
        Index(
            value = ["address"],
            unique = true,
        ),
        Index(
            value = ["public_key"],
            unique = true,
        ),
        Index(
            value = ["private_key"],
            unique = true,
        ),
    ],
)
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "address_id") val addressId: Long? = null,
    @ColumnInfo(name = "wallet_id") val walletId: Long,
    @ColumnInfo(name = "blockchain_name") val blockchainName: String,
    @ColumnInfo(name = "address") override val address: String,
    @ColumnInfo(name = "public_key") val publicKey: String,
    @ColumnInfo(name = "private_key") override val privateKey: String,
    @ColumnInfo(name = "is_general_address") val isGeneralAddress: Boolean,
    @ColumnInfo(name = "sot_index") val sotIndex: Byte,
    @ColumnInfo(name = "sot_derivation_index") val sotDerivationIndex: Int,
) : HasTronCredentials
