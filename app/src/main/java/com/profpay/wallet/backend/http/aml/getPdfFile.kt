package com.profpay.wallet.backend.http.aml

import com.profpay.wallet.backend.http.models.PdfFileModelResponse
import io.sentry.Sentry
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

interface GetPdfFileRequestCallback {
    fun onSuccess(data: PdfFileModelResponse)

    fun onFailure(error: String)
}

//  Создание GET-запроса к Api на получение AML-статистики в PDF формате по checkId
class GetPdfFileService {
    private val client =
        OkHttpClient()
            .newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

    private val localJson = Json { ignoreUnknownKeys = true }

    fun makeRequest(
        callback: GetPdfFileRequestCallback,
        checkId: Long,
        accessToken: String,
    ) {
        val http =
            HttpUrl
                .Builder()
                .scheme("http")
                .host("38.180.97.72")
                .port(59153)
                .addPathSegments("aml/get-pdf-file")
                .addQueryParameter("check_id", checkId.toString())
                .build()

        val request =
            Request
                .Builder()
                .url(http)
                .addHeader("Authorization", accessToken)
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
                            throw IOException("Error receiving TRX transactions")
                        }

                        try {
                            val obj =
                                localJson
                                    .decodeFromString<PdfFileModelResponse>(response.body!!.string())
                            callback.onSuccess(obj)
                        } catch (e: Exception) {
                            Sentry.captureException(e)
                            callback.onFailure(e.toString())
                        }
                    }
                }
            },
        )
    }
}

object GetPdfFileApi {
    val getPdfFileService: GetPdfFileService by lazy {
        GetPdfFileService()
    }
}
