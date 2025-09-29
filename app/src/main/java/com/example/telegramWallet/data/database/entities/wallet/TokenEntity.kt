package com.example.telegramWallet.data.database.entities.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.math.BigInteger

@Entity(
    tableName = "tokens",
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = ["address_id"],
            childColumns = ["address_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TokenEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "token_id") val tokenId: Long? = null,
    @ColumnInfo(name = "address_id") val addressId: Long,
    @ColumnInfo(name = "token_name") val tokenName: String,
    @ColumnInfo(name = "balance", defaultValue = "0") val balance: BigInteger,
    @ColumnInfo(name = "frozen_balance", defaultValue = "0") val frozenBalance: BigInteger? = BigInteger.ZERO,
) {
    // Баланс с учётом замороженных средств
    fun getBalanceWithoutFrozen(): BigInteger = balance - frozenBalance!!
}
