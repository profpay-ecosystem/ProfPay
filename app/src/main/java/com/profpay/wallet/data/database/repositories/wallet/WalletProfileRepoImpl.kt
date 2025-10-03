package com.profpay.wallet.data.database.repositories.wallet

import androidx.lifecycle.LiveData
import com.profpay.wallet.data.database.dao.wallet.WalletProfileCipher
import com.profpay.wallet.data.database.dao.wallet.WalletProfileDao
import com.profpay.wallet.data.database.dao.wallet.WalletProfileModel
import com.profpay.wallet.data.database.entities.wallet.WalletProfileEntity
import com.profpay.wallet.security.KeystoreEncryptionUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface WalletProfileRepo {
    suspend fun insertNewWalletProfileEntity(
        name: String,
        iv: ByteArray,
        cipherText: ByteArray,
    ): Long

    suspend fun getWalletNameById(walletId: Long): String?

    suspend fun getListAllWallets(): LiveData<List<WalletProfileModel>>

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
class WalletProfileRepoImpl
    @Inject
    constructor(
        private val walletProfileDao: WalletProfileDao,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : WalletProfileRepo {
    override suspend fun insertNewWalletProfileEntity(
        name: String,
        iv: ByteArray,
        cipherText: ByteArray,
    ): Long {
        return walletProfileDao.insertNewWalletProfileEntity(
            WalletProfileEntity(
                name = name,
                iv = iv,
                cipherText = cipherText
            ),
        )
    }

    override suspend fun getWalletNameById(walletId: Long): String? = walletProfileDao.getWalletNameById(walletId)

    override suspend fun getListAllWallets(): LiveData<List<WalletProfileModel>> = walletProfileDao.getListAllWallets()

    override suspend fun getCountRecords(): Long = walletProfileDao.getCountRecords()

    override suspend fun updateNameById(
        id: Long,
        newName: String,
    ) {
        walletProfileDao.updateNameById(id, newName)
    }

    override suspend fun deleteWalletProfile(id: Long) {
        return withContext(dispatcher) {
            return@withContext walletProfileDao.deleteWalletProfile(id)
        }
    }

    override suspend fun hasAnyWalletProfile(): Boolean = walletProfileDao.hasAnyWalletProfile()
    override suspend fun getWalletCipherData(id: Long): WalletProfileCipher {
        return withContext(dispatcher) {
            return@withContext walletProfileDao.getWalletCipherData(id)
        }
    }
}
