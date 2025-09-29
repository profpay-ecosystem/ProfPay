package com.example.telegramWallet.backend.http

import okhttp3.Interceptor
import okhttp3.OkHttpClient

// TODO: Общий клиент, цель - проверка валидности токена перед выполнением основного запроса, позже доработать.
class HttpClient {
    private val client = OkHttpClient().newBuilder()

    init {
        client.addInterceptor { chain: Interceptor.Chain ->
            val original = chain.request()
            val requestBuilder =
                original
                    .newBuilder()
                    .header("Authorization", "MY_API_KEY") // <-- this is the important line

            val request = requestBuilder.build()
            chain.proceed(request)
        }
    }
}
