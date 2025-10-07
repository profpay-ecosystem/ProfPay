package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.PendingTransactionDao
import com.profpay.wallet.data.database.entities.wallet.PendingTransactionEntity
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
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
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : PendingTransactionRepo {
        override suspend fun insert(pendingTransactionEntity: PendingTransactionEntity): Long {
            return withContext(ioDispatcher) {
                return@withContext pendingTransactionDao.insert(pendingTransactionEntity)
            }
        }

        override suspend fun pendingTransactionIsExistsByTxId(txid: String): Boolean {
            return withContext(ioDispatcher) {
                return@withContext pendingTransactionDao.pendingTransactionIsExistsByTxId(txid)
            }
        }

        override suspend fun deletePendingTransactionByTxId(txid: String) {
            return withContext(ioDispatcher) {
                return@withContext pendingTransactionDao.deletePendingTransactionByTxId(txid)
            }
        }

        override suspend fun getExpiredTransactions(currentTime: Long): List<PendingTransactionEntity> {
            return withContext(ioDispatcher) {
                return@withContext pendingTransactionDao.getExpiredTransactions(currentTime)
            }
        }
    }
