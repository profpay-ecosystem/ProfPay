package com.profpay.wallet.data.database.entities.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallet_profile",
)
data class WalletProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "iv") val iv: ByteArray,
    @ColumnInfo(name = "cipher_text") val cipherText: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WalletProfileEntity) return false

        return id == other.id &&
            name == other.name &&
            iv.contentEquals(other.iv) &&
            cipherText.contentEquals(other.cipherText)
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + cipherText.contentHashCode()
        return result
    }
}
