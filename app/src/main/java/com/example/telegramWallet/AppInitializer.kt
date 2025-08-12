package com.example.telegramWallet

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.telegramWallet.backend.grpc.CryptoAddressGrpcClient
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.UserGrpcClient
import com.example.telegramWallet.backend.http.binance.BinancePriceConverterApi.binancePriceConverterService
import com.example.telegramWallet.backend.http.binance.BinancePriceConverterRequestCallback
import com.example.telegramWallet.backend.http.coingecko.Tron24hChangeApi.tron24hChangeService
import com.example.telegramWallet.backend.http.coingecko.Tron24hChangeRequestCallback
import com.example.telegramWallet.backend.http.models.binance.BinancePriceConverterResponse
import com.example.telegramWallet.backend.http.models.binance.BinanceSymbolEnum
import com.example.telegramWallet.backend.http.models.coingecko.CoinSymbolEnum
import com.example.telegramWallet.backend.http.models.coingecko.Tron24hChangeResponse
import com.example.telegramWallet.data.database.entities.wallet.ExchangeRatesEntity
import com.example.telegramWallet.data.database.entities.wallet.TradingInsightsEntity
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.example.telegramWallet.data.database.repositories.wallet.TradingInsightsRepo
import com.example.telegramWallet.data.services.foreground.PusherService
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.pushy.sdk.Pushy
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class AppInitializer @Inject constructor(
    private val exchangeRatesRepo: ExchangeRatesRepo,
    private val tradingInsightsRepo: TradingInsightsRepo,
    private val profileRepo: ProfileRepo,
    private val addressRepo: AddressRepo,
    grpcClientFactory: GrpcClientFactory
) {
    private val userGrpcClient: UserGrpcClient = grpcClientFactory.getGrpcClient(
        UserGrpcClient::class.java,
        "grpc.wallet-services-srv.com",
        8443
    )
    private val cryptoAddressGrpcClient: CryptoAddressGrpcClient = grpcClientFactory.getGrpcClient(
        CryptoAddressGrpcClient::class.java,
        "grpc.wallet-services-srv.com",
        8443
    )

    suspend fun initialize(sharedPrefs: SharedPreferences, context: Context) {
        val firstStarted = sharedPrefs.getBoolean("FIRST_STARTED", true)
        if (PusherService.isRunning) return

        // TODO: Переделать
        if (firstStarted) {
            withContext(Dispatchers.IO) {
                val deviceToken = Pushy.register(context)
                sharedPrefs.edit { putString("device_token", deviceToken) }
            }
//            sharedPrefs.edit() { putBoolean("is_blocked_app", false) }
//            sharedPrefs.edit() { putBoolean("isEthDisable", false) }
        } else {
            val isProfileExists = profileRepo.isProfileExists()
            val profileDeviceToken = profileRepo.getDeviceToken()

            // TODO: Костыль для безотказной обновы, вырезать
            if (isProfileExists) {
                val userId = profileRepo.getProfileUserId()
                val appId = profileRepo.getProfileAppId()
                val isUserExistsResult = userGrpcClient.isUserExists(userId = userId)
                isUserExistsResult.fold(
                    onSuccess = {
                        val deviceCredentials = Pushy.getDeviceCredentials(context)
                        val generalAddresses = addressRepo.getGeneralAddresses()
                        Sentry.captureMessage("1 deviceToken: ${deviceCredentials.token} | appId: $appId")
                        if (!it.exists) {
                            Sentry.captureMessage("2 deviceToken: ${deviceCredentials.token} | appId: $appId")
                            val registerUserResult = userGrpcClient.registerUser(appId = appId, deviceToken = deviceCredentials.token)

                            registerUserResult.fold(
                                onSuccess = { response ->
                                    profileRepo.updateUserId(response.userId)
                                    profileRepo.updateDeviceToken(deviceCredentials.token)

                                    sharedPrefs.edit(commit = true) {
                                        putString("access_token", response.accessToken)
                                        putString("refresh_token", response.refreshToken)
                                    }

                                    userGrpcClient.registerUserDevice(
                                        userId = response.userId,
                                        appId = appId,
                                        deviceToken = deviceCredentials.token
                                    )

                                    for (address in generalAddresses) {
                                        val derivationIndices = addressRepo.getSortedDerivationIndices(address.walletId)
                                        cryptoAddressGrpcClient.addCryptoAddress(
                                            appId = appId,
                                            address = address.address,
                                            pubKey = address.publicKey,
                                            derivedIndices = derivationIndices
                                        )
                                    }
                                    sharedPrefs.edit() { putString("device_token", deviceCredentials.token) }
                                },
                                onFailure = { exception ->
                                    Sentry.captureException(exception)
                                    Log.e("gRPC ERROR", "Error during gRPC call: ${exception.message}")
                                }
                            )
                        } else if (it.exists && profileDeviceToken == null) {
                            sharedPrefs.edit() { putString("device_token", deviceCredentials.token) }

                            profileRepo.updateDeviceToken(deviceCredentials.token)
                            userGrpcClient.registerUserDevice(
                                userId = userId,
                                appId = appId,
                                deviceToken = deviceCredentials.token
                            )

                            for (address in generalAddresses) {
                                try {
                                    val derivationIndices = addressRepo.getSortedDerivationIndices(address.walletId)
                                    cryptoAddressGrpcClient.setDerivedIndex(
                                        userId = userId,
                                        generalAddress = address.address,
                                        derivedIndices = derivationIndices
                                    )
                                } catch (e: Exception) {
                                    Sentry.captureException(e)
                                }
                            }
                        }
                        Unit
                    },
                    onFailure = { exception ->
                        Sentry.captureException(exception)
                        Unit
                    }
                )
            }
        }

        syncExchangeRatesAndTrends()
        startPusherService(context)
    }

    private suspend fun syncExchangeRatesAndTrends() {
        val binanceSymbolsList = BinanceSymbolEnum.entries.map { it }
        binanceSymbolsList.forEach {
            val price = try {
                suspendCoroutine { continuation ->
                    binancePriceConverterService.makeRequest(object : BinancePriceConverterRequestCallback {
                        override fun onSuccess(response: BinancePriceConverterResponse) {
                            continuation.resume(response.price)
                        }

                        override fun onFailure(e: String) {
                            Sentry.captureException(Exception(e))
                            continuation.resumeWithException(Exception("Failed to get price: $e"))
                        }
                    }, it)
                }
            } catch (e: Exception) {
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
            val priceChangePercentage24h = try {
                suspendCoroutine { continuation ->
                    tron24hChangeService.makeRequest(object : Tron24hChangeRequestCallback {
                        override fun onSuccess(response: Tron24hChangeResponse) {
                            continuation.resume(response.marketData.priceChangePercentage24h)
                        }

                        override fun onFailure(e: String) {
                            Sentry.captureException(Exception(e))
                            continuation.resumeWithException(Exception("Failed to get price: $e"))
                        }
                    }, it)
                }
            } catch (e: Exception) {
                0.0
            }

            val isSymbolExist = tradingInsightsRepo.doesSymbolExist(it.symbol)
            if (isSymbolExist) {
                tradingInsightsRepo.updatePriceChangePercentage24h(
                    symbol = it.symbol, priceChangePercentage24h = priceChangePercentage24h
                )
            } else {
                tradingInsightsRepo.insert(
                    TradingInsightsEntity(
                        symbol = it.symbol, priceChangePercentage24h = priceChangePercentage24h
                    )
                )
            }
        }
    }

    private fun startPusherService(context: Context) {
        if (PusherService.isRunning) return
        val intent = Intent(context, PusherService::class.java)
        context.startService(intent)
    }
}
