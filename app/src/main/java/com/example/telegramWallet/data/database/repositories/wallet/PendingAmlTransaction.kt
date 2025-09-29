package com.example.telegramWallet.data.database.repositories.wallet

import com.example.telegramWallet.data.database.dao.wallet.PendingAmlTransactionDao
import com.example.telegramWallet.data.database.entities.wallet.PendingAmlTransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface PendingAmlTransactionRepo {
    suspend fun insert(pendingAmlTransactionEntity: PendingAmlTransactionEntity): Long

    suspend fun markAsSuccessful(txId: String)

    suspend fun markAsError(txId: String)

    suspend fun isPendingAmlTransactionExists(txId: String): Boolean
}

class PendingAmlTransactionRepoImpl
    @Inject
    constructor(
        private val pendingAmlTransactionDao: PendingAmlTransactionDao,
    ) : PendingAmlTransactionRepo {
        override suspend fun insert(pendingAmlTransactionEntity: PendingAmlTransactionEntity): Long {
            return withContext(Dispatchers.IO) {
                return@withContext pendingAmlTransactionDao.insert(pendingAmlTransactionEntity)
            }
        }

        override suspend fun markAsSuccessful(txId: String) {
            return withContext(Dispatchers.IO) {
                return@withContext pendingAmlTransactionDao.markAsSuccessful(txId)
            }
        }

        override suspend fun markAsError(txId: String) {
            return withContext(Dispatchers.IO) {
                return@withContext pendingAmlTransactionDao.markAsError(txId)
            }
        }

        override suspend fun isPendingAmlTransactionExists(txId: String): Boolean {
            return withContext(Dispatchers.IO) {
                return@withContext pendingAmlTransactionDao.isPendingAmlTransactionExists(txId)
            }
        }
    }
