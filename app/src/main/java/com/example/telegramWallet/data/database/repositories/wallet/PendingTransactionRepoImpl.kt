package com.example.telegramWallet.data.database.repositories.wallet

import com.example.telegramWallet.data.database.dao.wallet.PendingTransactionDao
import com.example.telegramWallet.data.database.entities.wallet.PendingTransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface PendingTransactionRepo {
    suspend fun insert(pendingTransactionEntity: PendingTransactionEntity): Long

    suspend fun pendingTransactionIsExistsByTxId(txid: String): Boolean

    suspend fun deletePendingTransactionByTxId(txid: String)

    suspend fun getExpiredTransactions(currentTime: Long): List<PendingTransactionEntity>
}

@Singleton
class PendingTransactionRepoImpl
    @Inject
    constructor(
        private val pendingTransactionDao: PendingTransactionDao,
    ) : PendingTransactionRepo {
        override suspend fun insert(pendingTransactionEntity: PendingTransactionEntity): Long {
            return withContext(Dispatchers.IO) {
                return@withContext pendingTransactionDao.insert(pendingTransactionEntity)
            }
        }

        override suspend fun pendingTransactionIsExistsByTxId(txid: String): Boolean {
            return withContext(Dispatchers.IO) {
                return@withContext pendingTransactionDao.pendingTransactionIsExistsByTxId(txid)
            }
        }

        override suspend fun deletePendingTransactionByTxId(txid: String) {
            return withContext(Dispatchers.IO) {
                return@withContext pendingTransactionDao.deletePendingTransactionByTxId(txid)
            }
        }

        override suspend fun getExpiredTransactions(currentTime: Long): List<PendingTransactionEntity> {
            return withContext(Dispatchers.IO) {
                return@withContext pendingTransactionDao.getExpiredTransactions(currentTime)
            }
        }
    }
