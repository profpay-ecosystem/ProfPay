package com.example.telegramWallet.bridge.view_model.wallet

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.navigation.NavController
import com.example.telegramWallet.backend.http.models.binance.BinanceSymbolEnum
import com.example.telegramWallet.backend.http.models.coingecko.CoinSymbolEnum
import com.example.telegramWallet.bridge.view_model.dto.TokenName
import com.example.telegramWallet.data.database.entities.wallet.TokenEntity
import com.example.telegramWallet.data.database.models.AddressWithTokens
import com.example.telegramWallet.data.database.models.TransactionModel
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.database.repositories.wallet.TradingInsightsRepo
import com.example.telegramWallet.data.database.repositories.wallet.WalletProfileRepo
import com.example.telegramWallet.data.flow_db.repo.WalletInfoRepo
import com.example.telegramWallet.data.utils.toSunAmount
import com.example.telegramWallet.data.utils.toTokenAmount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class WalletInfoViewModel
    @Inject
    constructor(
        private val walletProfileRepo: WalletProfileRepo,
        private val transactionsRepo: TransactionsRepo,
        private val addressRepo: AddressRepo,
        private val tokenRepo: TokenRepo,
        val exchangeRatesRepo: ExchangeRatesRepo,
        val tradingInsightsRepo: TradingInsightsRepo,
        private val profileRepo: ProfileRepo,
        private val walletInfoRepo: WalletInfoRepo,
    ) : ViewModel() {
        suspend fun getProfileTelegramId(): Long? = profileRepo.getProfileTelegramId()

        suspend fun getWalletNameById(walletId: Long): String? = walletProfileRepo.getWalletNameById(walletId)

        fun getAddressesSotsWithTokens(walletId: Long): LiveData<List<AddressWithTokens>> =
            liveData(Dispatchers.IO) {
                emitSource(addressRepo.getAddressesSotsWithTokensLD(walletId))
            }

        suspend fun getUserPermissions(
            sharedPrefs: SharedPreferences,
            navController: NavController,
        ) {
            try {
                walletInfoRepo.getUserPermissions(sharedPrefs, navController)
            } catch (e: Exception) {
                throw e
            }
        }

        suspend fun getListTransactionToTimestamp(listTransactions: List<TransactionModel>): List<List<TransactionModel?>> {
            var listListTransactions: List<List<TransactionModel>> = listOf(emptyList())

            withContext(Dispatchers.IO) {
                if (listTransactions.isEmpty()) return@withContext
                listListTransactions =
                    listTransactions
                        .sortedByDescending { it.timestamp }
                        .groupBy { it.transactionDate }
                        .values
                        .toList()
            }
            return listListTransactions
        }

        fun getAllRelatedTransactions(walletId: Long): LiveData<List<TransactionModel>> =
            liveData(Dispatchers.IO) {
                emitSource(transactionsRepo.getAllRelatedTransactions(walletId))
            }

        suspend fun updateTokenBalances(listAddressWithTokens: List<AddressWithTokens>) =
            coroutineScope {
                if (listAddressWithTokens.isEmpty()) return@coroutineScope

                TokenName.entries
                    .flatMap { token ->
                        listAddressWithTokens.map { addressWithTokens ->
                            async {
                                tokenRepo.updateTokenBalanceFromBlockchain(addressWithTokens.addressEntity.address, token)
                            }
                        }
                    }.awaitAll()
            }

        suspend fun getListTokensWithTotalBalance(listAddressWithTokens: List<AddressWithTokens>): List<TokenEntity> {
            val listTokensWithTotalBalance = mutableListOf<TokenEntity>()
            withContext(Dispatchers.IO) {
                if (listAddressWithTokens.isEmpty()) return@withContext
                TokenName.entries.forEach { token ->
                    val gAddressId =
                        listAddressWithTokens
                            .stream()
                            .filter { addressWithTokens ->
                                addressWithTokens.tokens.any { it.token.tokenName == token.tokenName }
                            }.filter { it.addressEntity.isGeneralAddress }
                            .findFirst()
                            .orElse(listAddressWithTokens[1])
                    val sumBalancesSotByToken =
                        listAddressWithTokens
                            .flatMap { addressWithTokens -> addressWithTokens.tokens }
                            .filter { it.token.tokenName == token.tokenName }
                            .sumOf { it.balanceWithoutFrozen }

                    listTokensWithTotalBalance.add(
                        TokenEntity(
                            addressId = gAddressId.addressEntity.addressId!!,
                            tokenName = token.tokenName,
                            balance = sumBalancesSotByToken,
                        ),
                    )
                }
            }
            return listTokensWithTotalBalance
        }

        suspend fun getTotalBalance(listTokensWithTotalBalance: List<TokenEntity>): BigInteger {
            val trxToUsdtRate =
                exchangeRatesRepo.getExchangeRateValue(BinanceSymbolEnum.TRX_USDT.symbol)

            return listTokensWithTotalBalance.sumOf {
                if (it.tokenName == "TRX") {
                    val balanceInSun = it.balance.toTokenAmount()
                    val totalValue = balanceInSun.multiply(trxToUsdtRate.toBigDecimal())
                    totalValue.toSunAmount()
                } else {
                    it.balance
                }
            }
        }

        suspend fun getTotalPPercentage24(listTokensWithTotalBalance: List<TokenEntity>): Double {
            val trxToUsdtRate = exchangeRatesRepo.getExchangeRateValue(BinanceSymbolEnum.TRX_USDT.symbol).toBigDecimal()
            val priceChangePercentage24hUsdt = tradingInsightsRepo.getPriceChangePercentage24h(CoinSymbolEnum.USDT_TRC20.symbol)
            val priceChangePercentage24hTrx = tradingInsightsRepo.getPriceChangePercentage24h(CoinSymbolEnum.TRON.symbol)

            val totalValue =
                listTokensWithTotalBalance.sumOf { token ->
                    when (token.tokenName) {
                        "TRX" -> token.balance.toTokenAmount().multiply(trxToUsdtRate)
                        "USDT" -> token.balance.toBigDecimal()
                        else -> BigDecimal.ZERO
                    }
                }

            val weightedSum =
                listTokensWithTotalBalance.sumOf { token ->
                    when (token.tokenName) {
                        "TRX" ->
                            token.balance
                                .toTokenAmount()
                                .multiply(trxToUsdtRate)
                                .multiply(priceChangePercentage24hTrx.toBigDecimal())
                        "USDT" ->
                            token.balance
                                .toBigDecimal()
                                .multiply(priceChangePercentage24hUsdt.toBigDecimal())
                        else -> BigDecimal.ZERO
                    }
                }

            val result =
                if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
                    BigDecimal.ZERO
                } else {
                    weightedSum.divide(totalValue, 8, RoundingMode.HALF_UP)
                }

            return result.toDouble().coerceIn(-100.0, 100.0)
        }
    }
