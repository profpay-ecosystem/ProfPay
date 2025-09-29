package com.example.telegramWallet.backend.http.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatusJwtStruct(
    val result: Boolean,
    val message: String,
)

@Serializable
data class StatusAccountStruct(
    val last_payment: String? = null,
    val subscribe_price: Long,
    val active: Boolean,
)

@Serializable
data class StatusJwtResponse(
    @SerialName("jwt_data") val jwtData: StatusJwtStruct,
    @SerialName("account_data") val accountData: StatusAccountStruct,
)
