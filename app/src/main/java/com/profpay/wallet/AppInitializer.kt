package com.profpay.wallet

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.profpay.wallet.backend.http.binance.BinancePriceConverterApi.binancePriceConverterService
import com.profpay.wallet.backend.http.binance.BinancePriceConverterRequestCallback
import com.profpay.wallet.backend.http.coingecko.Tron24hChangeApi.tron24hChangeService
import com.profpay.wallet.backend.http.coingecko.Tron24hChangeRequestCallback
import com.profpay.wallet.backend.http.models.binance.BinancePriceConverterResponse
import com.profpay.wallet.backend.http.models.binance.BinanceSymbolEnum
import com.profpay.wallet.backend.http.models.coingecko.CoinSymbolEnum
import com.profpay.wallet.backend.http.models.coingecko.Tron24hChangeResponse
import com.profpay.wallet.data.database.entities.wallet.CentralAddressEntity
import com.profpay.wallet.data.database.entities.wallet.ExchangeRatesEntity
import com.profpay.wallet.data.database.entities.wallet.TradingInsightsEntity
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.profpay.wallet.data.database.repositories.wallet.TradingInsightsRepo
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import com.profpay.wallet.data.services.foreground.PusherService
import com.profpay.wallet.tron.Tron
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.pushy.sdk.Pushy
import me.pushy.sdk.util.exceptions.PushyNetworkException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class AppInitializer
    @Inject
    constructor(
        private val exchangeRatesRepo: ExchangeRatesRepo,
        private val tradingInsightsRepo: TradingInsightsRepo,
        private val tron: Tron,
        private val centralAddressRepo: CentralAddressRepo,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
    ) {
        suspend fun initialize(
            sharedPrefs: SharedPreferences,
            context: Context,
        ) {
            val firstStarted = sharedPrefs.getBoolean(PrefKeys.FIRST_STARTED, true)
            if (PusherService.isRunning) return

            if (firstStarted) {
                val deviceToken =
                    try {
                        withContext(ioDispatcher) {
                            Pushy.register(context)
                        }
                    } catch (e: PushyNetworkException) {
                        Log.e("Init", "Pushy registration failed", e)
                        throw e
                    }

                val address = tron.addressUtilities.generateSingleAddress()
                centralAddressRepo.insertNewCentralAddress(
                    CentralAddressEntity(
                        address = address.address,
                        publicKey = address.publicKey,
                        privateKey = address.privateKey
                    )
                )

                sharedPrefs.edit { putString(PrefKeys.DEVICE_TOKEN, deviceToken) }
            }

            syncExchangeRatesAndTrends()
            startPusherService(context)
        }

        private suspend fun syncExchangeRatesAndTrends() {
            val binanceSymbolsList = BinanceSymbolEnum.entries.map { it }
            binanceSymbolsList.forEach {
                val price =
                    try {
                        suspendCoroutine { continuation ->
                            binancePriceConverterService.makeRequest(
                                object : BinancePriceConverterRequestCallback {
                                    override fun onSuccess(response: BinancePriceConverterResponse) {
                                        continuation.resume(response.price)
                                    }

                                    override fun onFailure(e: String) {
                                        Sentry.captureException(Exception(e))
                                        continuation.resumeWithException(Exception("Failed to get price: $e"))
                                    }
                                },
                                it,
                            )
                        }
                    } catch (_: Exception) {
                        1.0
                    }

                val isSymbolExist = exchangeRatesRepo.doesSymbolExist(it.symbol)
                if (isSymbolExist) {
                    exchangeRatesRepo.updateExchangeRate(symbol = it.symbol, value = price)
                } else {
                    exchangeRatesRepo.insert(ExchangeRatesEntity(symbol = it.symbol, value = price))
                }
            }

            val coingeckoSymbolList = CoinSymbolEnum.entries.map { it }
            coingeckoSymbolList.forEach {
                val priceChangePercentage24h =
                    try {
                        suspendCoroutine { continuation ->
                            tron24hChangeService.makeRequest(
                                object : Tron24hChangeRequestCallback {
                                    override fun onSuccess(response: Tron24hChangeResponse) {
                                        continuation.resume(response.marketData.priceChangePercentage24h)
                                    }

                                    override fun onFailure(e: String) {
                                        Sentry.captureException(Exception(e))
                                        continuation.resumeWithException(Exception("Failed to get price: $e"))
                                    }
                                },
                                it,
                            )
                        }
                    } catch (_: Exception) {
                        0.0
                    }

                val isSymbolExist = tradingInsightsRepo.doesSymbolExist(it.symbol)
                if (isSymbolExist) {
                    tradingInsightsRepo.updatePriceChangePercentage24h(
                        symbol = it.symbol,
                        priceChangePercentage24h = priceChangePercentage24h,
                    )
                } else {
                    tradingInsightsRepo.insert(
                        TradingInsightsEntity(
                            symbol = it.symbol,
                            priceChangePercentage24h = priceChangePercentage24h,
                        ),
                    )
                }
            }
        }

        private fun startPusherService(context: Context) {
            if (PusherService.isRunning) return
            val intent = Intent(context, PusherService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
    }
