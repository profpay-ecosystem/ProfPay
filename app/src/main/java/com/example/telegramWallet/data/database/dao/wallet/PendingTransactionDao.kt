package com.example.telegramWallet.data.database.dao.wallet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.telegramWallet.data.database.entities.wallet.PendingTransactionEntity

@Dao
interface PendingTransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(pendingTransactionEntity: PendingTransactionEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM pending_transaction WHERE tx_id = :txid)")
    fun pendingTransactionIsExistsByTxId(txid: String): Boolean

    @Query("DELETE FROM pending_transaction WHERE tx_id = :txid")
    fun deletePendingTransactionByTxId(txid: String)

    @Query("SELECT * FROM pending_transaction WHERE timestamp + ttl_mills < :currentTime")
    fun getExpiredTransactions(currentTime: Long): List<PendingTransactionEntity>
}
