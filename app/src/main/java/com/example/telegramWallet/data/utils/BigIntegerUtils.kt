package com.example.telegramWallet.data.utils

import com.google.protobuf.ByteString
import java.math.BigInteger

fun ByteString.toBigInteger(): BigInteger {
    return BigInteger(1, this.toByteArray()) // 1 означает положительное число
}

fun BigInteger.toByteString(): ByteString = ByteString.copyFrom(this.toByteArray())
