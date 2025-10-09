package com.profpay.wallet.backend.http.aml

import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream

interface DownloadAmlPdfRequestCallback {
    fun onSuccess(inputStream: InputStream?)

    fun onFailure(error: String)
}

class DownloadAmlPdfService {
    private val client = OkHttpClient()

    fun makeRequest(
        callback: DownloadAmlPdfRequestCallback,
        userId: Long,
        txId: String,
    ) {
        val http =
            HttpUrl
                .Builder()
                .scheme("https")
                .host("api.wallet-services-srv.com")
                .addPathSegments("api/aml/download")
                .addQueryParameter("user_id", userId.toString())
                .addQueryParameter("tx_id", txId)
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
                        val inputStream = response.body?.byteStream()
                        callback.onSuccess(inputStream)
                    }
                }
            },
        )
    }
}

object DownloadAmlPdfApi {
    val downloadAmlPdfService: DownloadAmlPdfService by lazy {
        DownloadAmlPdfService()
    }
}
