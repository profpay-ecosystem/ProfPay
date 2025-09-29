package com.profpay.wallet.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.database.models.TransactionModel

@Dao
interface TransactionsDao {
    @Insert(entity = TransactionEntity::class)
    fun insertNewTransaction(transactionsEntity: TransactionEntity)

    @Query("SELECT EXISTS(SELECT * FROM transactions WHERE tx_id = :txid)")
    fun transactionExistsViaTxid(txid: String): Int

    @Query("SELECT EXISTS(SELECT * FROM transactions WHERE tx_id = :txid AND status_code = 0)")
    suspend fun isTransactionPending(txid: String): Boolean

    @Transaction
    @Query(
        "SELECT *, DATE(ROUND(transactions.timestamp / 1000), 'unixepoch') AS transaction_date " +
            "FROM transactions " +
            "WHERE wallet_id = :walletId " +
            "ORDER BY timestamp DESC",
    )
    fun getAllRelatedTransactionsLD(walletId: Long): LiveData<List<TransactionModel>>

    @Query("SELECT * FROM transactions WHERE transaction_id = :transactionId")
    fun getTransactionLiveDataById(transactionId: Long): LiveData<TransactionEntity>

    @Query("UPDATE transactions SET is_processed = 1 WHERE transaction_id = :id")
    fun transactionSetProcessedUpdateTrueById(id: Long)

    @Query("UPDATE transactions SET is_processed = 0 WHERE transaction_id = :id")
    fun transactionSetProcessedUpdateFalseById(id: Long)

    @Query("UPDATE transactions SET is_processed = 0 WHERE tx_id = :txId")
    fun transactionSetProcessedUpdateFalseByTxId(txId: String)

    @Query("UPDATE transactions SET is_processed = 1 WHERE tx_id = :txId")
    fun transactionSetProcessedUpdateTrueByTxId(txId: String)

    @Transaction
    @Query(
        """
        SELECT *, DATE(ROUND(transactions.timestamp / 1000), 'unixepoch') AS transaction_date
        FROM transactions
        WHERE wallet_id = :walletId
          AND token_name = :tokenName
          AND (
            (:isSender = 1 AND sender_address = :address)
            OR
            (:isSender = 0 AND receiver_address = :address)
          )
          AND (
            :isCentralAddress = 0 OR type = 4
          )
        ORDER BY timestamp DESC
        """,
    )
    fun getTransactionsByAddressAndTokenLD(
        walletId: Long,
        address: String,
        tokenName: String,
        isSender: Boolean,
        isCentralAddress: Boolean,
    ): LiveData<List<TransactionModel>>

    @Query("SELECT * FROM transactions WHERE tx_id = :txId")
    fun getTransactionByTxId(txId: String): TransactionEntity

    @Query("UPDATE transactions SET status_code = :statusCode, timestamp = :timestamp WHERE tx_id = :txid")
    suspend fun updateStatusAndTimestampByTxId(
        statusCode: Int,
        timestamp: Long,
        txid: String,
    )

    @Query("SELECT EXISTS(SELECT * FROM transactions WHERE tx_id = :txid AND status_code = 1)")
    suspend fun isTransactionSuccessful(txid: String): Boolean
}
