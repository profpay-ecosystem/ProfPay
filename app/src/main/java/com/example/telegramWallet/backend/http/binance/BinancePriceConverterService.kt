package com.example.telegramWallet.backend.http.binance

import com.example.telegramWallet.backend.http.models.binance.BinancePriceConverterResponse
import com.example.telegramWallet.backend.http.models.binance.BinanceSymbolEnum
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

interface BinancePriceConverterRequestCallback {
    fun onSuccess(response: BinancePriceConverterResponse)

    fun onFailure(e: String)
}

class BinancePriceConverterService {
    private val client = OkHttpClient()
    private val localJson = Json { ignoreUnknownKeys = false }

    fun makeRequest(
        callback: BinancePriceConverterRequestCallback,
        symbol: BinanceSymbolEnum,
    ) {
        val http =
            HttpUrl
                .Builder()
                .scheme("https")
                .host("api.binance.com")
                .addPathSegments("api/v3/ticker/price")
                .addQueryParameter("symbol", symbol.symbol)
                .build()

        val request =
            Request
                .Builder()
                .url(http)
                .build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    callback.onFailure(e.toString())
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    response.use {
                        try {
                            val responseBody = response.body!!.string()
                            val obj =
                                localJson
                                    .decodeFromString<BinancePriceConverterResponse>(responseBody)
                            callback.onSuccess(obj)
                        } catch (e: Exception) {
                            callback.onFailure(e.toString())
                        }
                    }
                }
            },
        )
    }
}

object BinancePriceConverterApi {
    val binancePriceConverterService: BinancePriceConverterService by lazy {
        BinancePriceConverterService()
    }
}
