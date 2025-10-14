package com.profpay.wallet.data.database.dao.wallet

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.profpay.wallet.data.database.entities.wallet.WalletProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletProfileDao {
    @Insert(entity = WalletProfileEntity::class)
    suspend fun insert(walletProfileEntity: WalletProfileEntity): Long

    @Query("SELECT name FROM wallet_profile WHERE id = :walletId")
    suspend fun getWalletNameById(walletId: Long): String?

    @Query("SELECT id, name FROM wallet_profile")
    fun getListAllWalletsFlow(): Flow<List<WalletProfileModel>>

    @Query("SELECT COUNT(*) FROM wallet_profile")
    suspend fun getCountRecords(): Long

    @Query("UPDATE wallet_profile SET name = :newName WHERE id = :id")
    suspend fun updateNameById(
        id: Long,
        newName: String,
    )

    @Query("DELETE FROM wallet_profile WHERE id = :id")
    suspend fun deleteWalletProfile(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM wallet_profile LIMIT 1)")
    suspend fun hasAnyWalletProfile(): Boolean

    @Query("SELECT iv, cipher_text FROM wallet_profile WHERE id = :id")
    suspend fun getWalletCipherData(id: Long): WalletProfileCipher
}

data class WalletProfileModel(
    @ColumnInfo(name = "id") val id: Long? = null,
    @ColumnInfo(name = "name") val name: String,
)

data class WalletProfileCipher(
    @ColumnInfo(name = "iv") val iv: ByteArray,
    @ColumnInfo(name = "cipher_text") val cipherText: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WalletProfileCipher

        if (!iv.contentEquals(other.iv)) return false
        if (!cipherText.contentEquals(other.cipherText)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + cipherText.contentHashCode()
        return result
    }
}