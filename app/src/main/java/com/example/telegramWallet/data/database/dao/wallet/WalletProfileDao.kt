package com.example.telegramWallet.data.database.dao.wallet

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.telegramWallet.data.database.entities.wallet.WalletProfileEntity

@Dao
interface WalletProfileDao {
    @Insert(entity = WalletProfileEntity::class)
    fun insertNewWalletProfileEntity(walletProfileEntity: WalletProfileEntity): Long

    @Query("SELECT name FROM wallet_profile WHERE id = :walletId")
    fun getWalletNameById(walletId: Long): String?

    @Query("SELECT id, name FROM wallet_profile")
    fun getListAllWallets(): LiveData<List<WalletProfileModel>>

    @Query("SELECT COUNT(*) FROM wallet_profile")
    fun getCountRecords(): Long

    @Query("SELECT priv_key_bytes, chain_code FROM wallet_profile WHERE id = :id")
    fun getWalletPrivateKeyAndChainCodeById(id: Long): WalletPrivateKeyAndChainCodeModel

    @Query("UPDATE wallet_profile SET name = :newName WHERE id = :id")
    suspend fun updateNameById(id: Long, newName: String)

    @Query("DELETE FROM wallet_profile WHERE id = :id")
    suspend fun deleteWalletProfile(id: Long)

    @Query("SELECT entropy FROM wallet_profile WHERE id = :id")
    suspend fun getWalletEncryptedEntropy(id: Long): ByteArray?

    @Query("SELECT EXISTS(SELECT 1 FROM wallet_profile LIMIT 1)")
    suspend fun hasAnyWalletProfile(): Boolean
}

data class WalletProfileModel(
    @ColumnInfo(name = "id") val id: Long? = null,
    @ColumnInfo(name = "name") val name: String
)

data class WalletPrivateKeyAndChainCodeModel(
    @ColumnInfo(name = "priv_key_bytes") val privKeyBytes: ByteArray,
    @ColumnInfo(name = "chain_code") val chainCode: ByteArray
)