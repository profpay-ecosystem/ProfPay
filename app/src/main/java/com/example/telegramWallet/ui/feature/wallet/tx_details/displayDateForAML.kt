package com.example.telegramWallet.ui.feature.wallet.tx_details

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun displayDateForAML(timestamp: String): String {
    val outputFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())
    val timestampMillis = timestamp.toLong() * 1000
    val dateAML =
        Instant
            .ofEpochMilli(timestampMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    val currentDate = LocalDate.now()
    return when (val daysDifference = ChronoUnit.DAYS.between(dateAML, currentDate).toInt()) {
        1 -> "день назад"
        in 2..4 -> "$daysDifference дня назад"
        in 5..7 -> "$daysDifference дней назад"
        else -> dateAML.format(outputFormat)
    }
}
