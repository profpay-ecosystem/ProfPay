package com.profpay.wallet.backend.http.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthUserRequest(
    @SerialName("telegram_id") val telegramId: Long,
    @SerialName("access_token") val accessToken: String,
    @SerialName("android_unique") val androidUnique: String,
)

@Serializable
data class AuthUserResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_at") val expiresAt: Long,
)
