package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.PendingAmlTransactionDao
import com.profpay.wallet.data.database.entities.wallet.PendingAmlTransactionEntity
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
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
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : PendingAmlTransactionRepo {
        override suspend fun insert(pendingAmlTransactionEntity: PendingAmlTransactionEntity): Long {
            return withContext(ioDispatcher) {
                return@withContext pendingAmlTransactionDao.insert(pendingAmlTransactionEntity)
            }
        }

        override suspend fun markAsSuccessful(txId: String) {
            return withContext(ioDispatcher) {
                return@withContext pendingAmlTransactionDao.markAsSuccessful(txId)
            }
        }

        override suspend fun markAsError(txId: String) {
            return withContext(ioDispatcher) {
                return@withContext pendingAmlTransactionDao.markAsError(txId)
            }
        }

        override suspend fun isPendingAmlTransactionExists(txId: String): Boolean {
            return withContext(ioDispatcher) {
                return@withContext pendingAmlTransactionDao.isPendingAmlTransactionExists(txId)
            }
        }
    }
