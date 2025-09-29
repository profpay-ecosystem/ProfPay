package com.example.telegramWallet.tron.http

import com.example.telegramWallet.tron.http.models.TrxTransactionDataResponse
import com.example.telegramWallet.tron.http.models.TrxTransactionResponse
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.suspendCoroutine

// API запрос для получения TRX транзакций.
class TrxTransactionsService {
    private val client = OkHttpClient()
    private val localJson = Json { ignoreUnknownKeys = true }

    suspend fun makeRequest(address: String): List<TrxTransactionDataResponse> =
        suspendCoroutine { continuation ->
            val http =
                HttpUrl
                    .Builder()
                    .scheme("https")
                    .host("api.trongrid.io")
                    .addPathSegments("v1/accounts/$address/transactions")
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
                                throw IOException("Error receiving TRX transactions")
                            }

                            try {
                                val obj =
                                    localJson
                                        .decodeFromString<TrxTransactionResponse>(response.body.string())
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

object TrxTransactionsApi {
    val trxTransactionsService: TrxTransactionsService by lazy {
        TrxTransactionsService()
    }
}
