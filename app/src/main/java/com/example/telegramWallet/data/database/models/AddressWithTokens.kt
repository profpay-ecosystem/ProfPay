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
        entity = TokenEntity::class
    )
    val tokens: List<TokenEntity>
)

