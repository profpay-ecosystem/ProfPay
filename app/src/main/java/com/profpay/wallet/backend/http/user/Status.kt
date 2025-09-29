package com.profpay.wallet.backend.http.user

import com.profpay.wallet.backend.http.models.StatusJwtResponse
import com.profpay.wallet.backend.http.models.UserErrorResponse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

interface StatusRequestCallback {
    fun onSuccess(data: StatusJwtResponse)

    fun onFailure(error: UserErrorResponse)
}

// Создание GET-запроса к Api на получение статуса User-a о: последнем платеже, цене подписки, состоянии активности(блокировки) аккаунта
class StatusService {
    private val client = OkHttpClient()
    private val localJson = Json { ignoreUnknownKeys = false }

    fun makeRequest(
        callback: StatusRequestCallback,
        accessToken: String,
    ) {
        // TODO: Rebase to https
        val http =
            HttpUrl
                .Builder()
                .scheme("http")
                .host("38.180.97.72")
                .port(59153)
                .addPathSegments("user/status")
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
                    callback.onFailure(UserErrorResponse(false, e.toString()))
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    response.use {
                        if (!response.isSuccessful) {
                            throw IOException("что то другое")
                        }

                        val responseBody = response.body!!.string()

                        try {
                            val obj =
                                localJson
                                    .decodeFromString<StatusJwtResponse>(responseBody)

                            if (obj.jwtData.message == "Timing is everything") {
                                callback.onFailure(
                                    UserErrorResponse(
                                        status = false,
                                        message = obj.jwtData.message,
                                    ),
                                )
                            } else {
                                callback.onSuccess(obj)
                            }
                        } catch (e: SerializationException) {
                            val obj =
                                localJson
                                    .decodeFromString<UserErrorResponse>(responseBody)
                            callback.onFailure(obj)
                        } catch (e: Exception) {
                            callback.onFailure(UserErrorResponse(false, e.toString()))
                        }
                    }
                }
            },
        )
    }
}

object StatusApi {
    val statusService: StatusService by lazy {
        StatusService()
    }
}
