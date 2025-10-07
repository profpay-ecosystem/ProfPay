package com.profpay.wallet.data.database.entities.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigInteger

@Entity(
    tableName = "central_address",
    indices = [
        Index(
            value = ["address"],
            name = "central_address_ind_address",
            unique = true,
        ),
        Index(
            value = ["public_key"],
            name = "central_address_ind_public_key",
            unique = true,
        ),
        Index(
            value = ["private_key"],
            name = "central_address_ind_private_key",
            unique = true,
        ),
    ],
)
class CentralAddressEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "central_id") val centralId: Long,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "public_key") val publicKey: String,
    @ColumnInfo(name = "private_key") val privateKey: String,
    @ColumnInfo(name = "balance", defaultValue = "0") val balance: BigInteger,
) {
    constructor(address: String, publicKey: String, privateKey: String) : this(
        centralId = 0L,
        address = address,
        publicKey = publicKey,
        privateKey = privateKey,
        balance = BigInteger.ZERO,
    )
}
