package com.profpay.wallet.data.database.repositories

import androidx.lifecycle.LiveData
import com.profpay.wallet.data.database.dao.TransactionsDao
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.database.models.TransactionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface TransactionsRepo {
    suspend fun insertNewTransaction(transactionsEntity: TransactionEntity)

    suspend fun transactionExistsViaTxid(txid: String): Int

    suspend fun getAllRelatedTransactions(walletId: Long): LiveData<List<TransactionModel>>

    suspend fun getTransactionLiveDataById(transactionId: Long): LiveData<TransactionEntity>

    suspend fun transactionSetProcessedUpdateTrueById(id: Long)

    suspend fun transactionSetProcessedUpdateFalseByTxId(txId: String)

    suspend fun getTransactionByTxId(txId: String): TransactionEntity

    suspend fun transactionSetProcessedUpdateFalseById(id: Long)

    suspend fun transactionSetProcessedUpdateTrueByTxId(txId: String)

    suspend fun getTransactionsByAddressAndTokenLD(
        walletId: Long,
        address: String,
        tokenName: String,
        isSender: Boolean,
        isCentralAddress: Boolean,
    ): LiveData<List<TransactionModel>>

    suspend fun isTransactionPending(txid: String): Boolean

    suspend fun updateStatusAndTimestampByTxId(
        statusCode: Int,
        timestamp: Long,
        txid: String,
    )

    suspend fun isTransactionSuccessful(txid: String): Boolean
}

@Singleton
class TransactionsRepoImpl
    @Inject
    constructor(
        private val transactionsDao: TransactionsDao,
    ) : TransactionsRepo {
        override suspend fun insertNewTransaction(transactionsEntity: TransactionEntity) {
            withContext(Dispatchers.IO) {
                transactionsDao.insertNewTransaction(transactionsEntity)
            }
        }

        override suspend fun transactionExistsViaTxid(txid: String): Int {
            return withContext(Dispatchers.IO) {
                return@withContext transactionsDao.transactionExistsViaTxid(txid)
            }
        }

        override suspend fun getAllRelatedTransactions(walletId: Long): LiveData<List<TransactionModel>> {
            return withContext(Dispatchers.IO) {
                return@withContext transactionsDao.getAllRelatedTransactionsLD(walletId)
            }
        }

        override suspend fun getTransactionLiveDataById(transactionId: Long): LiveData<TransactionEntity> {
            return withContext(Dispatchers.IO) {
                return@withContext transactionsDao.getTransactionLiveDataById(transactionId)
            }
        }

        override suspend fun transactionSetProcessedUpdateTrueById(id: Long) {
            withContext(Dispatchers.IO) {
                transactionsDao.transactionSetProcessedUpdateTrueById(id)
            }
        }

        override suspend fun transactionSetProcessedUpdateFalseByTxId(txId: String) =
            transactionsDao.transactionSetProcessedUpdateFalseByTxId(txId)

        override suspend fun getTransactionByTxId(txId: String): TransactionEntity = transactionsDao.getTransactionByTxId(txId)

        override suspend fun transactionSetProcessedUpdateFalseById(id: Long) = transactionsDao.transactionSetProcessedUpdateFalseById(id)

        override suspend fun transactionSetProcessedUpdateTrueByTxId(txId: String) =
            transactionsDao.transactionSetProcessedUpdateTrueByTxId(txId)

        override suspend fun getTransactionsByAddressAndTokenLD(
            walletId: Long,
            address: String,
            tokenName: String,
            isSender: Boolean,
            isCentralAddress: Boolean,
        ): LiveData<List<TransactionModel>> =
            transactionsDao.getTransactionsByAddressAndTokenLD(
                walletId = walletId,
                address = address,
                tokenName = tokenName,
                isSender = isSender,
                isCentralAddress = isCentralAddress,
            )

        override suspend fun isTransactionPending(txid: String): Boolean {
            return withContext(Dispatchers.IO) {
                return@withContext transactionsDao.isTransactionPending(txid)
            }
        }

        override suspend fun updateStatusAndTimestampByTxId(
            statusCode: Int,
            timestamp: Long,
            txid: String,
        ) {
            return withContext(Dispatchers.IO) {
                return@withContext transactionsDao.updateStatusAndTimestampByTxId(statusCode, timestamp, txid)
            }
        }

        override suspend fun isTransactionSuccessful(txid: String): Boolean {
            return withContext(Dispatchers.IO) {
                return@withContext transactionsDao.isTransactionSuccessful(txid)
            }
        }
    }
