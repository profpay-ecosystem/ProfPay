package com.example.telegramWallet.backend.http.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val old_data: UpdateUserModel,
    val new_data: UpdateUserModel,
)

@Serializable
data class UpdateUserModel(
    val telegram_id: Long,
    val username: String,
)

@Serializable
data class UpdateUserResponse(
    val status: Boolean,
    val message: String,
)
