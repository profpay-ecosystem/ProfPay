package com.profpay.wallet.utils

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

fun convertInputStreamToString(inputStream: InputStream): String {
    val stringBuilder = StringBuilder()
    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
    var line: String?

    try {
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return stringBuilder.toString()
}
