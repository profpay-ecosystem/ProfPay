package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.TokenDao
import com.profpay.wallet.data.database.entities.wallet.TokenEntity
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

interface TokenRepo {
    suspend fun insert(tokenEntity: TokenEntity): Long

    suspend fun updateTronBalanceViaId(
        amount: BigInteger,
        addressId: Long,
        tokenName: String,
    )

    suspend fun getTokenIdByAddressIdAndTokenName(
        addressId: Long,
        tokenName: String,
    ): Long
}

@Singleton
class TokenRepoImpl @Inject constructor(
    private val tokenDao: TokenDao,
) : TokenRepo {
    override suspend fun insert(tokenEntity: TokenEntity): Long =
        tokenDao.insert(tokenEntity)

    override suspend fun updateTronBalanceViaId(
        amount: BigInteger,
        addressId: Long,
        tokenName: String,
    ) = tokenDao.updateTronBalanceViaId(amount, addressId, tokenName)

    override suspend fun getTokenIdByAddressIdAndTokenName(
        addressId: Long,
        tokenName: String,
    ): Long = tokenDao.getTokenIdByAddressIdAndTokenName(addressId, tokenName)
}
