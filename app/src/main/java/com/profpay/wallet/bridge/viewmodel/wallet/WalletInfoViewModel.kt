package com.profpay.wallet.bridge.viewmodel.wallet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.backend.http.models.binance.BinanceSymbolEnum
import com.profpay.wallet.backend.http.models.coingecko.CoinSymbolEnum
import com.profpay.wallet.bridge.viewmodel.dto.TokenName
import com.profpay.wallet.data.database.entities.wallet.TokenEntity
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.profpay.wallet.data.database.repositories.wallet.TokenRepo
import com.profpay.wallet.data.database.repositories.wallet.TradingInsightsRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import com.profpay.wallet.data.utils.toSunAmount
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.tron.Tron
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
        private val tron: Tron,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _walletName = MutableStateFlow<String?>(null)
        val walletName: StateFlow<String?> = _walletName.asStateFlow()

        private val _tokensWithTotalBalance = MutableStateFlow<List<TokenEntity>>(emptyList())
        val tokensWithTotalBalance: StateFlow<List<TokenEntity>> = _tokensWithTotalBalance.asStateFlow()

        private val _totalBalance = MutableStateFlow<BigInteger>(BigInteger.ZERO)
        val totalBalance: StateFlow<BigInteger> = _totalBalance.asStateFlow()

        private val _totalPercentage24h = MutableStateFlow(0.0)
        val totalPercentage24h: StateFlow<Double> = _totalPercentage24h.asStateFlow()

        private val _transactionsByDate = MutableStateFlow<List<List<TransactionModel>>>(emptyList())
        val transactionsByDate: StateFlow<List<List<TransactionModel>>> = _transactionsByDate.asStateFlow()

        fun getWalletNameById(walletId: Long) =
            viewModelScope.launch(ioDispatcher) {
                val name = walletProfileRepo.getWalletNameById(walletId)
                _walletName.emit(name)
            }

        fun getAddressesSotsWithTokens(walletId: Long): LiveData<List<AddressWithTokens>> =
            liveData(ioDispatcher) {
                emitSource(addressRepo.getAddressesSotsWithTokensFlow(walletId).asLiveData())
            }

        fun groupTransactionsByDate(listTransactions: List<TransactionModel>) =
            viewModelScope.launch(ioDispatcher) {
                if (listTransactions.isEmpty()) {
                    _transactionsByDate.value = emptyList()
                    return@launch
                }

                val grouped =
                    listTransactions
                        .sortedByDescending { it.timestamp }
                        .groupBy { it.transactionDate }
                        .values
                        .toList()

                _transactionsByDate.value = grouped
            }

        fun getAllRelatedTransactions(walletId: Long): LiveData<List<TransactionModel>> =
            liveData(ioDispatcher) {
                emitSource(transactionsRepo.getAllRelatedTransactionsFlow(walletId).asLiveData())
            }

        fun updateTokenBalances(listAddressWithTokens: List<AddressWithTokens>) =
            viewModelScope.launch(ioDispatcher) {
                if (listAddressWithTokens.isEmpty()) return@launch

                TokenName.entries
                    .flatMap { token ->
                        listAddressWithTokens.map { addressWithTokens ->
                            async {
                                val addressId = addressRepo.getAddressEntityByAddress(addressWithTokens.addressEntity.address)?.addressId

                                val balance =
                                    if (token == TokenName.USDT) {
                                        tron.addressUtilities.getUsdtBalance(addressWithTokens.addressEntity.address)
                                    } else {
                                        tron.addressUtilities.getTrxBalance(addressWithTokens.addressEntity.address)
                                    }
                                tokenRepo.updateTronBalanceViaId(balance, addressId!!, token.shortName)
                            }
                        }
                    }.awaitAll()
            }

        fun loadTokensWithTotalBalance(listAddressWithTokens: List<AddressWithTokens>) =
            viewModelScope.launch(ioDispatcher) {
                if (listAddressWithTokens.isEmpty()) return@launch

                val tokensWithBalance =
                    TokenName.entries.map { token ->
                        val generalAddress =
                            listAddressWithTokens
                                .firstOrNull { address ->
                                    address.addressEntity.isGeneralAddress &&
                                        address.tokens.any { it.token.tokenName == token.tokenName }
                                } ?: listAddressWithTokens.firstOrNull() // безопаснее, чем [1]

                        val totalBalance =
                            listAddressWithTokens
                                .flatMap { it.tokens }
                                .filter { it.token.tokenName == token.tokenName }
                                .sumOf { it.balanceWithoutFrozen }

                        TokenEntity(
                            addressId = generalAddress?.addressEntity?.addressId ?: 0,
                            tokenName = token.tokenName,
                            balance = totalBalance,
                        )
                    }

                _tokensWithTotalBalance.emit(tokensWithBalance)
            }

        fun calculateTotalBalance(listTokensWithTotalBalance: List<TokenEntity>) =
            viewModelScope.launch(ioDispatcher) {
                if (listTokensWithTotalBalance.isEmpty()) {
                    _totalBalance.value = BigInteger.ZERO
                    return@launch
                }

                try {
                    val trxToUsdtRate =
                        exchangeRatesRepo.getExchangeRateValue(BinanceSymbolEnum.TRX_USDT.symbol)

                    val total =
                        listTokensWithTotalBalance.sumOf { token ->
                            if (token.tokenName == "TRX") {
                                val balanceInSun = token.balance.toTokenAmount()
                                val totalValue = balanceInSun.multiply(trxToUsdtRate.toBigDecimal())
                                totalValue.toSunAmount()
                            } else {
                                token.balance
                            }
                        }

                    _totalBalance.value = total
                } catch (e: Exception) {
                    // Лучше не падать, а выставить 0
                    _totalBalance.value = BigInteger.ZERO
                    Log.e("WalletViewModel", "Failed to calculate total balance", e)
                }
            }

        fun calculateTotalPercentage24h(listTokensWithTotalBalance: List<TokenEntity>) =
            viewModelScope.launch(ioDispatcher) {
                if (listTokensWithTotalBalance.isEmpty()) {
                    _totalPercentage24h.value = 0.0
                    return@launch
                }

                try {
                    val trxToUsdtRate =
                        exchangeRatesRepo
                            .getExchangeRateValue(BinanceSymbolEnum.TRX_USDT.symbol)
                            .toBigDecimal()

                    val priceChangeUsdt =
                        tradingInsightsRepo.getPriceChangePercentage24h(CoinSymbolEnum.USDT_TRC20.symbol)
                    val priceChangeTrx =
                        tradingInsightsRepo.getPriceChangePercentage24h(CoinSymbolEnum.TRON.symbol)

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
                                        .multiply(priceChangeTrx.toBigDecimal())
                                "USDT" ->
                                    token.balance
                                        .toBigDecimal()
                                        .multiply(priceChangeUsdt.toBigDecimal())
                                else -> BigDecimal.ZERO
                            }
                        }

                    val result =
                        if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
                            BigDecimal.ZERO
                        } else {
                            weightedSum.divide(totalValue, 8, RoundingMode.HALF_UP)
                        }

                    _totalPercentage24h.value = result.toDouble().coerceIn(-100.0, 100.0)
                } catch (e: Exception) {
                    Log.e("WalletViewModel", "Failed to calculate 24h percentage", e)
                    _totalPercentage24h.value = 0.0
                }
            }
    }
