package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.PendingTransactionDao
import com.profpay.wallet.data.database.entities.wallet.PendingTransactionEntity
import javax.inject.Inject
import javax.inject.Singleton

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
        override suspend fun insert(pendingTransactionEntity: PendingTransactionEntity): Long =
            pendingTransactionDao.insert(pendingTransactionEntity)

        override suspend fun pendingTransactionIsExistsByTxId(txid: String): Boolean =
            pendingTransactionDao.pendingTransactionIsExistsByTxId(txid)

        override suspend fun deletePendingTransactionByTxId(txid: String) = pendingTransactionDao.deletePendingTransactionByTxId(txid)

        override suspend fun getExpiredTransactions(currentTime: Long): List<PendingTransactionEntity> =
            pendingTransactionDao.getExpiredTransactions(currentTime)
    }
