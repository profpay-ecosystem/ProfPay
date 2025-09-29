package com.example.telegramWallet.tron.http

import com.example.telegramWallet.tron.http.models.Trc20TransactionsDataResponse
import com.example.telegramWallet.tron.http.models.Trc20TransactionsResponse
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.suspendCoroutine

// API запрос для получения USDT TRC20 транзакций.
class Trc20TransactionsService {
    private val client = OkHttpClient()
    private val localJson = Json { ignoreUnknownKeys = true }

    suspend fun makeRequest(address: String): List<Trc20TransactionsDataResponse> =
        suspendCoroutine { continuation ->
            val http =
                HttpUrl
                    .Builder()
                    .scheme("https")
                    .host("api.trongrid.io")
                    .addPathSegments("v1/accounts/$address/transactions/trc20")
                    .addQueryParameter("contract_address", "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t")
                    .addQueryParameter("limit", "200")
                    .build()

            val request =
                Request
                    .Builder()
                    .url(http)
                    .addHeader("Content-Type", "application/json")
                    .build()

            client.newCall(request).enqueue(
                object : Callback {
                    override fun onFailure(
                        call: Call,
                        e: IOException,
                    ) {
                        continuation.resumeWith(Result.failure(e))
                    }

                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) {
                        response.use {
                            if (!response.isSuccessful) {
                                continuation.resumeWith(
                                    Result.failure(IOException("Error receiving TRC20 transactions")),
                                )
                                return
                            }

                            try {
                                val body = response.body.string()
                                val obj = localJson.decodeFromString<Trc20TransactionsResponse>(body)
                                continuation.resumeWith(Result.success(obj.data))
                            } catch (e: Exception) {
                                continuation.resumeWith(Result.failure(e))
                            }
                        }
                    }
                },
            )
        }
}

object Trc20TransactionsApi {
    val trc20TransactionsService: Trc20TransactionsService by lazy {
        Trc20TransactionsService()
    }
}
