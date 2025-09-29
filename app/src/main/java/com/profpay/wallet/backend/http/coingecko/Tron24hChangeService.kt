package com.profpay.wallet.backend.http.coingecko

import com.profpay.wallet.backend.http.models.coingecko.CoinSymbolEnum
import com.profpay.wallet.backend.http.models.coingecko.Tron24hChangeResponse
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

interface Tron24hChangeRequestCallback {
    fun onSuccess(response: Tron24hChangeResponse)

    fun onFailure(e: String)
}

class Tron24hChangeService {
    private val client = OkHttpClient()
    private val localJson = Json { ignoreUnknownKeys = true }

    fun makeRequest(
        callback: Tron24hChangeRequestCallback,
        coin: CoinSymbolEnum,
    ) {
        val http =
            HttpUrl
                .Builder()
                .scheme("https")
                .host("api.coingecko.com")
                .addPathSegments("api/v3/coins/")
                .addPathSegment(coin.symbol)
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
                                    .decodeFromString<Tron24hChangeResponse>(responseBody)
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

object Tron24hChangeApi {
    val tron24hChangeService: Tron24hChangeService by lazy {
        Tron24hChangeService()
    }
}
