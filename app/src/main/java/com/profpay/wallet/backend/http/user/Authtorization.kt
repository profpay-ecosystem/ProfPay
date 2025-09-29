package com.profpay.wallet.backend.http.user

import com.profpay.wallet.backend.http.models.AuthUserRequest
import com.profpay.wallet.backend.http.models.AuthUserResponse
import com.profpay.wallet.backend.http.models.UserErrorResponse
import io.sentry.Sentry
import kotlinx.serialization.SerializationException
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

interface AuthRequestCallback {
    fun onSuccess(data: AuthUserResponse)

    fun onFailure(error: UserErrorResponse)
}

// Создание POST-запроса к Api на подтверждение доступа, получение нового access_token и времени его жизни
class AuthService {
    private val client = OkHttpClient()
    private val localJson = Json { ignoreUnknownKeys = false }

    fun makeRequest(
        callback: AuthRequestCallback,
        userData: AuthUserRequest,
    ) {
        val jsonRequest = localJson.encodeToString(userData)
        val json = "application/json; charset=utf-8".toMediaType()

        val body: RequestBody = jsonRequest.toRequestBody(json)
        // TODO: Rebase to https
        val request =
            Request
                .Builder()
                .url("http://38.180.97.72:59153/user/auth")
                .post(body)
                .addHeader("Authorization", userData.access_token)
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
                                    .decodeFromString<AuthUserResponse>(responseBody)
                            callback.onSuccess(obj)
                        } catch (e: SerializationException) {
                            val obj =
                                localJson
                                    .decodeFromString<UserErrorResponse>(responseBody)
                            Sentry.captureException(e)
                            callback.onFailure(obj)
                        } catch (e: Exception) {
                            Sentry.captureException(e)
                            callback.onFailure(UserErrorResponse(false, e.toString()))
                        }
                    }
                }
            },
        )
    }
}

object AuthApi {
    val authService: AuthService by lazy {
        AuthService()
    }
}
