package com.profpay.wallet.bridge.view_model.dto

data class ProfileDto(
    val telegram_id: Long,
    val active_tg_id: Boolean,
    val username: String? = null,
    val access_token: String? = null,
    val expires_at: Long? = null,
)
