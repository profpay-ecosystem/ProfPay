package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.AddressDao
import com.profpay.wallet.data.database.entities.wallet.AddressEntity
import com.profpay.wallet.data.database.models.AddressWithTokens
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface AddressRepo {
    suspend fun insert(addressEntity: AddressEntity): Long

    suspend fun updateSotIndexByAddressId(
        index: Byte,
        addressId: Long,
    )

    suspend fun getAddressEntityByAddress(address: String): AddressEntity?

    suspend fun isGeneralAddress(address: String): Boolean

    fun getAddressesSotsWithTokensByBlockchainFlow(
        walletId: Long,
        blockchainName: String,
    ): Flow<List<AddressWithTokens>>

    suspend fun getAddressesSotsWithTokensByBlockchain(blockchainName: String): List<AddressWithTokens>

    fun getAddressesSotsWithTokensFlow(walletId: Long): Flow<List<AddressWithTokens>>

    suspend fun getAddressEntityById(id: Long): AddressEntity?

    fun getGeneralAddressWithTokensFlow(
        addressId: Long,
        blockchainName: String,
    ): Flow<AddressWithTokens>

    suspend fun getGeneralAddressWithTokens(
        addressId: Long,
        blockchainName: String,
    ): AddressWithTokens

    fun getAddressWithTokensFlow(
        addressId: Long,
        blockchainName: String,
    ): Flow<AddressWithTokens>

    suspend fun getGeneralAddressByWalletId(walletId: Long): String

    fun getAddressEntityByAddressFlow(address: String): Flow<AddressEntity>

    fun getAddressWithTokensByAddressFlow(address: String): Flow<AddressWithTokens>

    suspend fun getMaxSotDerivationIndex(id: Long): Int

    fun getAddressesWithTokensArchivalByBlockchainFlow(
        walletId: Long,
        blockchainName: String,
    ): Flow<List<AddressWithTokens>>

    suspend fun getGeneralPublicKeyByWalletId(walletId: Long): String

    suspend fun getGeneralAddressEntityByWalletId(walletId: Long): AddressEntity

    suspend fun getGeneralAddresses(): List<AddressEntity>

    suspend fun getSortedDerivationIndices(walletId: Long): List<Int>
}

@Singleton
class AddressRepoImpl
    @Inject
    constructor(
        private val addressDao: AddressDao,
    ) : AddressRepo {
        override suspend fun insert(addressEntity: AddressEntity): Long = addressDao.insert(addressEntity)

        override suspend fun updateSotIndexByAddressId(
            index: Byte,
            addressId: Long,
        ) = addressDao.updateSotIndexByAddressId(index, addressId)

        override suspend fun getAddressEntityByAddress(address: String): AddressEntity? = addressDao.getAddressEntityByAddress(address)

        override suspend fun isGeneralAddress(address: String): Boolean = addressDao.isGeneralAddress(address)

        override fun getAddressesSotsWithTokensByBlockchainFlow(
            walletId: Long,
            blockchainName: String,
        ): Flow<List<AddressWithTokens>> = addressDao.getAddressesSotsWithTokensByBlockchainFlow(walletId, blockchainName)

        override suspend fun getAddressesSotsWithTokensByBlockchain(blockchainName: String): List<AddressWithTokens> =
            addressDao.getAddressesSotsWithTokensByBlockchain(blockchainName)

        override fun getAddressesSotsWithTokensFlow(walletId: Long): Flow<List<AddressWithTokens>> =
            addressDao.getAddressesSotsWithTokensFlow(walletId)

        override suspend fun getAddressEntityById(id: Long): AddressEntity? = addressDao.getAddressEntityById(id)

        override fun getGeneralAddressWithTokensFlow(
            addressId: Long,
            blockchainName: String,
        ): Flow<AddressWithTokens> = addressDao.getGeneralAddressWithTokensFlow(addressId, blockchainName)

        override suspend fun getGeneralAddressWithTokens(
            addressId: Long,
            blockchainName: String,
        ): AddressWithTokens = addressDao.getGeneralAddressWithTokens(addressId, blockchainName)

        override fun getAddressWithTokensFlow(
            addressId: Long,
            blockchainName: String,
        ): Flow<AddressWithTokens> = addressDao.getAddressWithTokensFlow(addressId, blockchainName)

        override suspend fun getGeneralAddressByWalletId(walletId: Long): String = addressDao.getGeneralAddressByWalletId(walletId)

        override fun getAddressEntityByAddressFlow(address: String): Flow<AddressEntity> = addressDao.getAddressEntityByAddressFlow(address)

        override fun getAddressWithTokensByAddressFlow(address: String): Flow<AddressWithTokens> =
            addressDao.getAddressWithTokensByAddressFlow(address)

        override suspend fun getMaxSotDerivationIndex(id: Long): Int = addressDao.getMaxSotDerivationIndex(id)

        override fun getAddressesWithTokensArchivalByBlockchainFlow(
            walletId: Long,
            blockchainName: String,
        ): Flow<List<AddressWithTokens>> = addressDao.getAddressesWithTokensArchivalByBlockchainFlow(walletId, blockchainName)

        override suspend fun getGeneralPublicKeyByWalletId(walletId: Long): String = addressDao.getGeneralPublicKeyByWalletId(walletId)

        override suspend fun getGeneralAddressEntityByWalletId(walletId: Long): AddressEntity =
            addressDao.getGeneralAddressEntityByWalletId(walletId)

        override suspend fun getGeneralAddresses(): List<AddressEntity> = addressDao.getGeneralAddresses()

        override suspend fun getSortedDerivationIndices(walletId: Long): List<Int> = addressDao.getSortedDerivationIndices(walletId)
    }
