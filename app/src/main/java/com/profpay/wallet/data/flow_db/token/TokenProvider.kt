package com.profpay.wallet.data.flow_db.token

interface TokenProvider {
    fun getAccessToken(): String

    fun getRefreshToken(): String

    suspend fun refreshTokensIfNeeded(): Unit

    fun saveTokens(
        access: String,
        refresh: String,
    )

    fun clearTokens()
}
