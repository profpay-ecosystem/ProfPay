package com.example.walletcore.primitives

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType

enum class ContentType(val mediaType: MediaType) {
    /** Обычный текст */
    TEXT("text/plain; charset=utf-8".toMediaType()),

    /** JSON */
    JSON("application/json; charset=utf-8".toMediaType()),

    /** Формы URL-encoded */
    FORM_URLENCODED("application/x-www-form-urlencoded; charset=utf-8".toMediaType()),

    /** Двоичные данные */
    BINARY("application/x-binary".toMediaType());

    companion object {
        fun fromString(value: String): ContentType? =
            entries.find { it.mediaType.toString() == value }
    }
}