package com.example.telegramWallet.backend.http.aml

import com.example.telegramWallet.backend.http.models.CrystalMonitorTxAddRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

interface GetAmlStatisticsRequestCallback {
    fun onSuccess()

    fun onFailure(error: String)
}

class CrystalMonitorTxAddService {
    private val client = OkHttpClient()
    private val localJson = Json { ignoreUnknownKeys = true }

    fun makeRequest(
        callback: GetAmlStatisticsRequestCallback,
        amlData: CrystalMonitorTxAddRequest,
        appKey: String,
    ) {
        val jsonRequest = localJson.encodeToString(amlData)
        val json = "application/json".toMediaType()

        val body: RequestBody = jsonRequest.toRequestBody(json)
        val request =
            Request
                .Builder()
                .url("https://api.wallet-services-srv.com/api/crystal/monitor/tx/add")
                .post(body)
                .addHeader("X-Auth-Appkey", appKey)
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
                        if (!response.isSuccessful) {
                            throw IOException(response.message)
                        }
                        callback.onSuccess()
                    }
                }
            },
        )
    }
}

object GetCrystalMonitorTxAddApi {
    val getCrystalMonitorTxAddService: CrystalMonitorTxAddService by lazy {
        CrystalMonitorTxAddService()
    }
}
