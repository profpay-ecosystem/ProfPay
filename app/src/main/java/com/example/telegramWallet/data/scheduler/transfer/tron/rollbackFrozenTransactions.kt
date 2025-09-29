package com.example.telegramWallet.data.scheduler.transfer.tron

import com.example.telegramWallet.data.database.repositories.wallet.PendingTransactionRepo

suspend fun rollbackFrozenTransactions(pendingTransactionRepo: PendingTransactionRepo) {
    val now = System.currentTimeMillis()
    val expiredTxs = pendingTransactionRepo.getExpiredTransactions(now)

    for (tx in expiredTxs) {
        pendingTransactionRepo.deletePendingTransactionByTxId(tx.txid)
    }
}
