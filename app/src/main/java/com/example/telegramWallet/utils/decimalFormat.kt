package com.example.telegramWallet.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

fun decimalFormat(value: BigDecimal): String {
    val symbols = DecimalFormatSymbols().apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val decimalFormat = DecimalFormat("###,##0.00", symbols).apply {
        roundingMode = RoundingMode.DOWN
    }
    return decimalFormat.format(value)
}