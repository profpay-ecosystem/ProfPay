package com.example.telegramWallet.backend.http.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthUserRequest(
    val telegram_id: Long,
    val access_token: String,
    val android_unique: String,
)

@Serializable
data class AuthUserResponse(
    val access_token: String,
    val expires_at: Long,
)
