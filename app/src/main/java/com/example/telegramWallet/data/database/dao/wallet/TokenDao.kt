package com.example.telegramWallet.data.database.dao.wallet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.telegramWallet.data.database.entities.wallet.TokenEntity
import java.math.BigInteger

@Dao
interface TokenDao {
    @Insert(entity = TokenEntity::class)
    fun insertNewTokenEntity(tokenEntity: TokenEntity): Long

    @Query("UPDATE tokens SET balance = :amount WHERE address_id = :addressId AND token_name = :tokenName")
    fun updateTronBalanceViaId(
        amount: BigInteger,
        addressId: Long,
        tokenName: String,
    )

    @Query("SELECT token_id FROM tokens WHERE address_id = :addressId AND token_name = :tokenName")
    fun getTokenIdByAddressIdAndTokenName(
        addressId: Long,
        tokenName: String,
    ): Long
}
