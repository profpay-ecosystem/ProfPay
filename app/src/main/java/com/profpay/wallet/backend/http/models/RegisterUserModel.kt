package com.profpay.wallet.backend.http.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(
    val telegram_id: Long,
    val username: String,
    val android_unique: String,
)

@Serializable
data class RegisterUserResponse(
    val access_token: String,
    val expires_at: Long,
)

// Общий data-class ответа с ошибкой
@Serializable
data class UserErrorResponse(
    val status: Boolean,
    val message: String,
)
