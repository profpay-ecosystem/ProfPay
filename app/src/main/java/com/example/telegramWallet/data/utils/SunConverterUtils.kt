package com.example.telegramWallet.data.utils

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

private val SUN_DIVISOR = BigDecimal("1000000")

// BigInteger (SUN) → BigDecimal (Token Amount)
fun BigInteger.toTokenAmount(): BigDecimal {
    val bd =
        this
            .toBigDecimal()
            .divide(SUN_DIVISOR, 6, RoundingMode.DOWN)
            .stripTrailingZeros()

    return if (bd.scale() < 0) bd.setScale(0, RoundingMode.UNNECESSARY) else bd
}

// BigDecimal (Token Amount) → BigInteger (SUN)
fun BigDecimal.toSunAmount(): BigInteger =
    this
        .multiply(SUN_DIVISOR)
        .setScale(0, RoundingMode.DOWN)
        .toBigInteger()
