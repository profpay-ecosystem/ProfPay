package com.example.telegramWallet.data.database.models

import androidx.room.Embedded
import androidx.room.Relation
import com.example.telegramWallet.data.database.entities.wallet.AddressEntity
import com.example.telegramWallet.data.database.entities.wallet.TokenEntity

data class AddressWithTokens(
    @Embedded val addressEntity: AddressEntity,
    @Relation(
        parentColumn = "address_id",
        entityColumn = "address_id",
        entity = TokenEntity::class,
        projection = ["token_id", "address_id", "token_name", "balance"],
    )
    val tokens: List<TokenWithPendingTransactions>,
)
