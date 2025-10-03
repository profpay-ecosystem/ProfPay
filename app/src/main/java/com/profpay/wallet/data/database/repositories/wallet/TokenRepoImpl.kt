package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.bridge.view_model.dto.TokenName
import com.profpay.wallet.data.database.dao.wallet.TokenDao
import com.profpay.wallet.data.database.entities.wallet.TokenEntity
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import com.profpay.wallet.tron.Tron
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface TokenRepo {
    suspend fun insertNewTokenEntity(tokenEntity: TokenEntity): Long

    suspend fun updateTronBalanceViaId(
        amount: BigInteger,
        addressId: Long,
        tokenName: String,
    )

    suspend fun updateTokenBalanceFromBlockchain(
        address: String,
        token: TokenName,
    )

    suspend fun getTokenIdByAddressIdAndTokenName(
        addressId: Long,
        tokenName: String,
    ): Long
}

@Singleton
class TokenRepoImpl
    @Inject
    constructor(
        private val tokenDao: TokenDao,
        private val tron: Tron,
        private val addressRepo: AddressRepo,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : TokenRepo {
        override suspend fun insertNewTokenEntity(tokenEntity: TokenEntity): Long = tokenDao.insertNewTokenEntity(tokenEntity)

        override suspend fun updateTronBalanceViaId(
            amount: BigInteger,
            addressId: Long,
            tokenName: String,
        ) = tokenDao.updateTronBalanceViaId(amount, addressId, tokenName)

        override suspend fun updateTokenBalanceFromBlockchain(
            address: String,
            token: TokenName,
        ) {
            return withContext(ioDispatcher) {
                val addressId = addressRepo.getAddressEntityByAddress(address)?.addressId ?: return@withContext
                val balance =
                    if (token == TokenName.USDT) {
                        tron.addressUtilities.getUsdtBalance(address)
                    } else {
                        tron.addressUtilities.getTrxBalance(address)
                    }
                // TODO: Обновления можно отменять если цифры блокчейна и БД сходятся
                updateTronBalanceViaId(balance, addressId, token.shortName)
            }
        }

        override suspend fun getTokenIdByAddressIdAndTokenName(
            addressId: Long,
            tokenName: String,
        ): Long {
            return withContext(ioDispatcher) {
                return@withContext tokenDao.getTokenIdByAddressIdAndTokenName(addressId, tokenName)
            }
        }
    }
