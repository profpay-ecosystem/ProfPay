package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.WalletProfileCipher
import com.profpay.wallet.data.database.dao.wallet.WalletProfileDao
import com.profpay.wallet.data.database.dao.wallet.WalletProfileModel
import com.profpay.wallet.data.database.entities.wallet.WalletProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface WalletProfileRepo {
    suspend fun insert(walletProfileEntity: WalletProfileEntity): Long

    suspend fun getWalletNameById(walletId: Long): String?

    fun getListAllWalletsFlow(): Flow<List<WalletProfileModel>>

    suspend fun getCountRecords(): Long

    suspend fun updateNameById(
        id: Long,
        newName: String,
    )

    suspend fun deleteWalletProfile(id: Long)

    suspend fun hasAnyWalletProfile(): Boolean

    suspend fun getWalletCipherData(id: Long): WalletProfileCipher
}

@Singleton
class WalletProfileRepoImpl @Inject constructor(
    private val walletProfileDao: WalletProfileDao,
) : WalletProfileRepo {
    override suspend fun insert(walletProfileEntity: WalletProfileEntity): Long {
        val number = getCountRecords() + 1
        val entityWithName = walletProfileEntity.copy(name = "Wallet $number")
        return walletProfileDao.insert(entityWithName)
    }

    override suspend fun getWalletNameById(walletId: Long): String? =
        walletProfileDao.getWalletNameById(walletId)

    override fun getListAllWalletsFlow(): Flow<List<WalletProfileModel>> =
        walletProfileDao.getListAllWalletsFlow()

    override suspend fun getCountRecords(): Long =
        walletProfileDao.getCountRecords()

    override suspend fun updateNameById(
        id: Long,
        newName: String,
    ) = walletProfileDao.updateNameById(id, newName)

    override suspend fun deleteWalletProfile(id: Long) =
        walletProfileDao.deleteWalletProfile(id)

    override suspend fun hasAnyWalletProfile(): Boolean =
        walletProfileDao.hasAnyWalletProfile()

    override suspend fun getWalletCipherData(id: Long): WalletProfileCipher =
        walletProfileDao.getWalletCipherData(id)
}
