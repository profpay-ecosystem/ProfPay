package com.profpay.wallet.data.scheduler.transfer.tron

import com.profpay.wallet.data.database.repositories.wallet.PendingTransactionRepo

suspend fun rollbackFrozenTransactions(pendingTransactionRepo: PendingTransactionRepo) {
    val now = System.currentTimeMillis()
    val expiredTxs = pendingTransactionRepo.getExpiredTransactions(now)

    for (tx in expiredTxs) {
        pendingTransactionRepo.deletePendingTransactionByTxId(tx.txid)
    }
}
