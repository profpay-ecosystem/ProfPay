package com.profpay.wallet.data.database.dao.wallet

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.profpay.wallet.data.database.entities.wallet.AddressEntity
import com.profpay.wallet.data.database.models.AddressWithTokens

@Dao
interface AddressDao {
    @Insert(entity = AddressEntity::class)
    fun insertNewAddress(addressEntity: AddressEntity): Long

    @Query("SELECT is_general_address FROM addresses WHERE address =:address")
    fun isGeneralAddress(address: String): Boolean

    @Query(
        "SELECT address_id, wallet_id, blockchain_name, address, is_general_address, sot_index, sot_derivation_index, public_key FROM addresses WHERE address =:address",
    )
    fun getAddressEntityByAddress(address: String): AddressEntity?

    @Query(
        "SELECT address_id, wallet_id, blockchain_name, address, is_general_address, sot_index, sot_derivation_index, public_key FROM addresses WHERE address_id =:id",
    )
    fun getAddressEntityById(id: Long): AddressEntity?

    @Transaction
    @Query(
        "SELECT * FROM addresses " +
            "WHERE addresses.wallet_id = :walletId " +
            "AND addresses.blockchain_name = :blockchainName " +
            "AND addresses.sot_index >= 0 ORDER BY addresses.sot_index ASC",
    )
    fun getAddressesSotsWithTokensByBlockchainLD(
        walletId: Long,
        blockchainName: String,
    ): LiveData<List<AddressWithTokens>>

    @Transaction
    @Query(
        "SELECT * FROM addresses " +
            "WHERE addresses.address_id = :addressId " +
            "AND addresses.blockchain_name = :blockchainName " +
            "AND addresses.is_general_address = 1",
    )
    fun getGeneralAddressWithTokensLiveData(
        addressId: Long,
        blockchainName: String,
    ): LiveData<AddressWithTokens>

    @Query(
        "SELECT * FROM addresses " +
            "WHERE addresses.address_id = :addressId " +
            "AND addresses.blockchain_name = :blockchainName " +
            "AND addresses.is_general_address = 1",
    )
    fun getGeneralAddressWithTokens(
        addressId: Long,
        blockchainName: String,
    ): AddressWithTokens

    @Transaction
    @Query(
        "SELECT * FROM addresses " +
            "WHERE addresses.address_id = :addressId " +
            "AND addresses.blockchain_name = :blockchainName",
    )
    fun getAddressWithTokens(
        addressId: Long,
        blockchainName: String,
    ): LiveData<AddressWithTokens>

    @Transaction
    @Query(
        "SELECT * FROM addresses " +
            "WHERE addresses.wallet_id = :walletId " +
            "AND addresses.blockchain_name = :blockchainName " +
            "AND addresses.sot_index == -1 ",
    )
    fun getAddressWithTokensArchivalByBlockchainLD(
        walletId: Long,
        blockchainName: String,
    ): LiveData<List<AddressWithTokens>>

    @Transaction
    @Query(
        "SELECT * FROM addresses " +
            "WHERE addresses.address = :address ",
    )
    fun getAddressWithTokensByAddressLD(address: String): LiveData<AddressWithTokens>

    @Query("SELECT * FROM addresses WHERE address =:address")
    fun getAddressEntityByAddressLD(address: String): LiveData<AddressEntity>

    @Query("SELECT address FROM addresses WHERE wallet_id = :walletId AND is_general_address = 1")
    fun getGeneralAddressByWalletId(walletId: Long): String

    @Transaction
    @Query(
        "SELECT * FROM addresses " +
            "WHERE addresses.blockchain_name = :blockchainName " +
            "AND addresses.sot_index >= 0 ORDER BY addresses.sot_index ASC",
    )
    fun getAddressesSotsWithTokensByBlockchain(blockchainName: String): List<AddressWithTokens>

    @Transaction
    @Query(
        "SELECT * FROM addresses " +
            "WHERE addresses.wallet_id = :walletId " +
            "AND addresses.sot_index >= 0 ",
    )
    fun getAddressesSotsWithTokensLD(walletId: Long): LiveData<List<AddressWithTokens>>

    @Query("SELECT max(sot_derivation_index) FROM addresses WHERE wallet_id = :id")
    fun getMaxSotDerivationIndex(id: Long): Int

    @Query("UPDATE addresses SET sot_index = :index WHERE address_id = :addressId")
    fun updateSotIndexByAddressId(
        index: Byte,
        addressId: Long,
    )

    @Query("SELECT public_key FROM addresses WHERE wallet_id = :walletId AND is_general_address = 1")
    fun getGeneralPublicKeyByWalletId(walletId: Long): String

    @Query("SELECT * FROM addresses WHERE wallet_id = :walletId AND is_general_address = 1")
    fun getGeneralAddressEntityByWalletId(walletId: Long): AddressEntity

    @Query("SELECT * FROM addresses WHERE is_general_address = 1")
    fun getGeneralAddresses(): List<AddressEntity>

    @Query(
        """
        SELECT sot_derivation_index FROM addresses WHERE sot_index != 0 AND sot_index > 0 AND wallet_id = :walletId ORDER BY sot_index ASC
    """,
    )
    suspend fun getSortedDerivationIndices(walletId: Long): List<Int>
}
