package com.profpay.wallet.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

fun decimalFormat(value: BigDecimal): String {
    val symbols =
        DecimalFormatSymbols().apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }

    val decimalFormat =
        DecimalFormat().apply {
            decimalFormatSymbols = symbols
            roundingMode = RoundingMode.DOWN

            if (value.abs() >= BigDecimal.ONE) {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            } else {
                minimumFractionDigits = 2
                maximumFractionDigits = 8
            }
        }

    return decimalFormat.format(value)
}
