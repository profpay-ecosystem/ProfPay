package com.profpay.wallet.data.database.dao.wallet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.profpay.wallet.data.database.entities.wallet.TokenEntity
import java.math.BigInteger

@Dao
interface TokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tokens: List<TokenEntity>)

    @Insert(entity = TokenEntity::class)
    suspend fun insert(tokenEntity: TokenEntity): Long

    @Query("UPDATE tokens SET balance = :amount WHERE address_id = :addressId AND token_name = :tokenName")
    suspend fun updateTronBalanceViaId(
        amount: BigInteger,
        addressId: Long,
        tokenName: String,
    )

    @Query("SELECT token_id FROM tokens WHERE address_id = :addressId AND token_name = :tokenName")
    suspend fun getTokenIdByAddressIdAndTokenName(
        addressId: Long,
        tokenName: String,
    ): Long
}
