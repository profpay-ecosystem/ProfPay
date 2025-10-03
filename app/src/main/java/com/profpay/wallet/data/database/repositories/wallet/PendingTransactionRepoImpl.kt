package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.PendingTransactionDao
import com.profpay.wallet.data.database.entities.wallet.PendingTransactionEntity
import kotlinx.coroutines.CoroutineDispatcher
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
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : PendingTransactionRepo {
        override suspend fun insert(pendingTransactionEntity: PendingTransactionEntity): Long {
            return withContext(dispatcher) {
                return@withContext pendingTransactionDao.insert(pendingTransactionEntity)
            }
        }

        override suspend fun pendingTransactionIsExistsByTxId(txid: String): Boolean {
            return withContext(dispatcher) {
                return@withContext pendingTransactionDao.pendingTransactionIsExistsByTxId(txid)
            }
        }

        override suspend fun deletePendingTransactionByTxId(txid: String) {
            return withContext(dispatcher) {
                return@withContext pendingTransactionDao.deletePendingTransactionByTxId(txid)
            }
        }

        override suspend fun getExpiredTransactions(currentTime: Long): List<PendingTransactionEntity> {
            return withContext(dispatcher) {
                return@withContext pendingTransactionDao.getExpiredTransactions(currentTime)
            }
        }
    }
