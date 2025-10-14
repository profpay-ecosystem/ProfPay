package com.profpay.wallet.data.database.repositories

import com.profpay.wallet.data.database.dao.TransactionsDao
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.database.models.TransactionModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface TransactionsRepo {
    suspend fun insertNewTransaction(transactionsEntity: TransactionEntity)

    suspend fun transactionExistsViaTxid(txid: String): Int

    fun getAllRelatedTransactionsFlow(walletId: Long): Flow<List<TransactionModel>>

    fun getTransactionFlowById(transactionId: Long): Flow<TransactionEntity>

    suspend fun transactionSetProcessedUpdateTrueById(id: Long)

    suspend fun transactionSetProcessedUpdateFalseByTxId(txId: String)

    suspend fun getTransactionByTxId(txId: String): TransactionEntity

    suspend fun transactionSetProcessedUpdateFalseById(id: Long)

    suspend fun transactionSetProcessedUpdateTrueByTxId(txId: String)

    fun getTransactionsByAddressAndTokenFlow(
        walletId: Long,
        address: String,
        tokenName: String,
        isSender: Boolean,
        isCentralAddress: Boolean,
    ): Flow<List<TransactionModel>>

    suspend fun isTransactionPending(txid: String): Boolean

    suspend fun updateStatusAndTimestampByTxId(
        statusCode: Int,
        timestamp: Long,
        txid: String,
    )

    suspend fun isTransactionSuccessful(txid: String): Boolean
    suspend fun deleteTransactionByTxId(txid: String)
}

@Singleton
class TransactionsRepoImpl @Inject constructor(
    private val transactionsDao: TransactionsDao,
) : TransactionsRepo {
    override suspend fun insertNewTransaction(transactionsEntity: TransactionEntity) =
        transactionsDao.insertNewTransaction(transactionsEntity)

    override suspend fun transactionExistsViaTxid(txid: String): Int =
        transactionsDao.transactionExistsViaTxid(txid)

    override fun getAllRelatedTransactionsFlow(walletId: Long): Flow<List<TransactionModel>> =
        transactionsDao.getAllRelatedTransactionsFlow(walletId)

    override fun getTransactionFlowById(transactionId: Long): Flow<TransactionEntity> =
        transactionsDao.getTransactionFlowById(transactionId)

    override suspend fun transactionSetProcessedUpdateTrueById(id: Long) =
        transactionsDao.transactionSetProcessedUpdateTrueById(id)

    override suspend fun transactionSetProcessedUpdateFalseByTxId(txId: String) =
        transactionsDao.transactionSetProcessedUpdateFalseByTxId(txId)

    override suspend fun getTransactionByTxId(txId: String): TransactionEntity =
        transactionsDao.getTransactionByTxId(txId)

    override suspend fun transactionSetProcessedUpdateFalseById(id: Long) =
        transactionsDao.transactionSetProcessedUpdateFalseById(id)

    override suspend fun transactionSetProcessedUpdateTrueByTxId(txId: String) =
        transactionsDao.transactionSetProcessedUpdateTrueByTxId(txId)

    override fun getTransactionsByAddressAndTokenFlow(
        walletId: Long,
        address: String,
        tokenName: String,
        isSender: Boolean,
        isCentralAddress: Boolean,
    ): Flow<List<TransactionModel>> =
        transactionsDao.getTransactionsByAddressAndTokenFlow(
            walletId = walletId,
            address = address,
            tokenName = tokenName,
            isSender = isSender,
            isCentralAddress = isCentralAddress,
        )

    override suspend fun isTransactionPending(txid: String): Boolean =
        transactionsDao.isTransactionPending(txid)

    override suspend fun updateStatusAndTimestampByTxId(
        statusCode: Int,
        timestamp: Long,
        txid: String,
    ) = transactionsDao.updateStatusAndTimestampByTxId(statusCode, timestamp, txid)

    override suspend fun isTransactionSuccessful(txid: String): Boolean =
        transactionsDao.isTransactionSuccessful(txid)

    override suspend fun deleteTransactionByTxId(txid: String) =
        transactionsDao.deleteTransactionByTxId(txid)
}
