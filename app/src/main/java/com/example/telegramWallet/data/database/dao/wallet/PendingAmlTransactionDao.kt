package com.example.telegramWallet.data.database.dao.wallet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.telegramWallet.data.database.entities.wallet.PendingAmlTransactionEntity

@Dao
interface PendingAmlTransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(pendingAmlTransactionEntity: PendingAmlTransactionEntity): Long

    @Query("UPDATE pending_aml_transactions SET is_successful = 1 WHERE tx_id = :txId")
    fun markAsSuccessful(txId: String)

    @Query("UPDATE pending_aml_transactions SET is_error = 1 WHERE tx_id = :txId")
    fun markAsError(txId: String)

    @Query("SELECT EXISTS(SELECT * FROM pending_aml_transactions WHERE tx_id = :txId AND is_successful = 0 AND is_error = 0)")
    fun isPendingAmlTransactionExists(txId: String): Boolean
}
