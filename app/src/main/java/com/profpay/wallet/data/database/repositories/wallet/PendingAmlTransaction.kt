package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.PendingAmlTransactionDao
import com.profpay.wallet.data.database.entities.wallet.PendingAmlTransactionEntity
import javax.inject.Inject
import javax.inject.Singleton

interface PendingAmlTransactionRepo {
    suspend fun insert(pendingAmlTransactionEntity: PendingAmlTransactionEntity): Long

    suspend fun markAsSuccessful(txId: String)

    suspend fun markAsError(txId: String)

    suspend fun isPendingAmlTransactionExists(txId: String): Boolean
}

class PendingAmlTransactionRepoImpl @Inject constructor(
    private val pendingAmlTransactionDao: PendingAmlTransactionDao,
) : PendingAmlTransactionRepo {
    override suspend fun insert(pendingAmlTransactionEntity: PendingAmlTransactionEntity): Long =
        pendingAmlTransactionDao.insert(pendingAmlTransactionEntity)

    override suspend fun markAsSuccessful(txId: String) =
        pendingAmlTransactionDao.markAsSuccessful(txId)

    override suspend fun markAsError(txId: String) =
        pendingAmlTransactionDao.markAsError(txId)

    override suspend fun isPendingAmlTransactionExists(txId: String): Boolean =
        pendingAmlTransactionDao.isPendingAmlTransactionExists(txId)
}
