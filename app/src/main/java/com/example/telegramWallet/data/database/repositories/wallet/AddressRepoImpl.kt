package com.example.telegramWallet.data.database.repositories.wallet

import androidx.lifecycle.LiveData
import com.example.telegramWallet.data.database.dao.wallet.AddressDao
import com.example.telegramWallet.data.database.entities.wallet.AddressEntity
import com.example.telegramWallet.data.database.models.AddressWithTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface AddressRepo {
    suspend fun insertNewAddress(addressEntity: AddressEntity): Long

    suspend fun getAddressEntityByAddress(address: String): AddressEntity?

    suspend fun isGeneralAddress(address: String): Boolean

    suspend fun getAddressesSotsWithTokensByBlockchainLD(
        walletId: Long,
        blockchainName: String,
    ): LiveData<List<AddressWithTokens>>

    suspend fun getAddressesSotsWithTokensByBlockchain(blockchainName: String): List<AddressWithTokens>

    suspend fun getAddressesSotsWithTokensLD(walletId: Long): LiveData<List<AddressWithTokens>>

    suspend fun getAddressEntityById(id: Long): AddressEntity?

    suspend fun getGeneralAddressWithTokensLiveData(
        addressId: Long,
        blockchainName: String,
    ): LiveData<AddressWithTokens>

    suspend fun getGeneralAddressWithTokens(
        addressId: Long,
        blockchainName: String,
    ): AddressWithTokens

    suspend fun getAddressWithTokens(
        addressId: Long,
        blockchainName: String,
    ): LiveData<AddressWithTokens>

    suspend fun getGeneralAddressByWalletId(walletId: Long): String

    suspend fun getAddressEntityByAddressLD(address: String): LiveData<AddressEntity>

    suspend fun getAddressWithTokensByAddressLD(address: String): LiveData<AddressWithTokens>

    suspend fun getMaxSotDerivationIndex(id: Long): Int

    suspend fun updateSotIndexByAddressId(
        index: Byte,
        addressId: Long,
    )

    suspend fun getAddressesWithTokensArchivalByBlockchainLD(
        walletId: Long,
        blockchainName: String,
    ): LiveData<List<AddressWithTokens>>

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
        override suspend fun insertNewAddress(addressEntity: AddressEntity): Long = addressDao.insertNewAddress(addressEntity)

        override suspend fun getAddressEntityByAddress(address: String): AddressEntity? {
            return withContext(Dispatchers.IO) {
                return@withContext addressDao.getAddressEntityByAddress(address)
            }
        }

        override suspend fun isGeneralAddress(address: String): Boolean {
            return withContext(Dispatchers.IO) {
                return@withContext addressDao.isGeneralAddress(address)
            }
        }

        override suspend fun getAddressesSotsWithTokensByBlockchainLD(
            walletId: Long,
            blockchainName: String,
        ): LiveData<List<AddressWithTokens>> = addressDao.getAddressesSotsWithTokensByBlockchainLD(walletId, blockchainName)

        override suspend fun getAddressesSotsWithTokensByBlockchain(blockchainName: String): List<AddressWithTokens> =
            addressDao.getAddressesSotsWithTokensByBlockchain(blockchainName)

        override suspend fun getAddressesSotsWithTokensLD(walletId: Long): LiveData<List<AddressWithTokens>> =
            addressDao.getAddressesSotsWithTokensLD(walletId)

        override suspend fun getAddressEntityById(id: Long): AddressEntity? {
            return withContext(Dispatchers.IO) {
                return@withContext addressDao.getAddressEntityById(id)
            }
        }

        override suspend fun getGeneralAddressWithTokensLiveData(
            addressId: Long,
            blockchainName: String,
        ): LiveData<AddressWithTokens> {
            return withContext(Dispatchers.IO) {
                return@withContext addressDao.getGeneralAddressWithTokensLiveData(addressId, blockchainName)
            }
        }

        override suspend fun getGeneralAddressWithTokens(
            addressId: Long,
            blockchainName: String,
        ): AddressWithTokens {
            return withContext(Dispatchers.IO) {
                return@withContext addressDao.getGeneralAddressWithTokens(addressId, blockchainName)
            }
        }

        override suspend fun getAddressWithTokens(
            addressId: Long,
            blockchainName: String,
        ): LiveData<AddressWithTokens> = addressDao.getAddressWithTokens(addressId, blockchainName)

        override suspend fun getGeneralAddressByWalletId(walletId: Long): String {
            return withContext(Dispatchers.IO) {
                return@withContext addressDao.getGeneralAddressByWalletId(walletId)
            }
        }

        override suspend fun getAddressWithTokensByAddressLD(address: String): LiveData<AddressWithTokens> =
            addressDao.getAddressWithTokensByAddressLD(address)

        override suspend fun getMaxSotDerivationIndex(id: Long): Int = addressDao.getMaxSotDerivationIndex(id)

        override suspend fun updateSotIndexByAddressId(
            index: Byte,
            addressId: Long,
        ) {
            addressDao.updateSotIndexByAddressId(index, addressId)
        }

        override suspend fun getAddressesWithTokensArchivalByBlockchainLD(
            walletId: Long,
            blockchainName: String,
        ): LiveData<List<AddressWithTokens>> = addressDao.getAddressWithTokensArchivalByBlockchainLD(walletId, blockchainName)

        override suspend fun getGeneralPublicKeyByWalletId(walletId: Long): String = addressDao.getGeneralPublicKeyByWalletId(walletId)

        override suspend fun getGeneralAddressEntityByWalletId(walletId: Long): AddressEntity =
            addressDao.getGeneralAddressEntityByWalletId(walletId)

        override suspend fun getGeneralAddresses(): List<AddressEntity> {
            return withContext(Dispatchers.IO) {
                return@withContext addressDao.getGeneralAddresses()
            }
        }

        override suspend fun getSortedDerivationIndices(walletId: Long): List<Int> {
            return withContext(Dispatchers.IO) {
                return@withContext addressDao.getSortedDerivationIndices(walletId)
            }
        }

        override suspend fun getAddressEntityByAddressLD(address: String): LiveData<AddressEntity> =
            addressDao.getAddressEntityByAddressLD(address)
    }
