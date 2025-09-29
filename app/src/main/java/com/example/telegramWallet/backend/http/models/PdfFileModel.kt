package com.example.telegramWallet.backend.http.models

import kotlinx.serialization.Serializable

@Serializable
data class PdfFileModelResponse(
    val status: Boolean,
    val url: String,
)
