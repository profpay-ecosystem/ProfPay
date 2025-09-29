package com.profpay.wallet.backend.http.models

import kotlinx.serialization.Serializable

@Serializable
data class PdfFileModelResponse(
    val status: Boolean,
    val url: String,
)
