package com.profpay.wallet.utils

import io.sentry.Sentry

inline fun <T> safeRun(block: () -> T): T? =
    try { block() } catch (e: Exception) {
        Sentry.captureException(e)
        null
    }
