package com.example.telegramWallet.data.database.models

import androidx.room.Embedded
import androidx.room.Relation
import com.example.telegramWallet.data.database.entities.wallet.PendingTransactionEntity
import com.example.telegramWallet.data.database.entities.wallet.TokenEntity
import java.math.BigInteger

data class TokenWithPendingTransactions(
    @Embedded val token: TokenEntity,
    @Relation(
        parentColumn = "token_id",
        entityColumn = "token_id",
    )
    val pendingTransactions: List<PendingTransactionEntity>,
) {
    val frozenBalance: BigInteger
        get() = pendingTransactions.sumOf { it.amount }
    val balanceWithoutFrozen: BigInteger
        get() = (token.balance - frozenBalance).coerceAtLeast(BigInteger.ZERO)
}
