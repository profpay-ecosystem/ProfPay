package com.example.telegramWallet.backend.http.user

import com.example.telegramWallet.backend.http.models.RegisterUserRequest
import com.example.telegramWallet.backend.http.models.RegisterUserResponse
import com.example.telegramWallet.backend.http.models.UserErrorResponse
import kotlinx.serialization.SerializationException
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

interface RegisterRequestCallback {
    fun onSuccess(data: RegisterUserResponse)

    fun onFailure(error: UserErrorResponse)
}

// Создание POST-запроса к Api на регистрацию, получение access_token и времени его жизни
class RegisterService {
    private val client = OkHttpClient()
    private val localJson = Json { ignoreUnknownKeys = false }

    fun makeRequest(
        callback: RegisterRequestCallback,
        userData: RegisterUserRequest,
    ) {
        val jsonRequest = localJson.encodeToString(userData)
        val json = "application/json; charset=utf-8".toMediaType()

        val body: RequestBody = jsonRequest.toRequestBody(json)
        // TODO: Rebase to https
        val request =
            Request
                .Builder()
                .url("http://38.180.97.72:59153/user/register")
                .post(body)
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
                                    .decodeFromString<RegisterUserResponse>(responseBody)
                            callback.onSuccess(obj)
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

object RegisterApi {
    val registerService: RegisterService by lazy {
        RegisterService()
    }
}
