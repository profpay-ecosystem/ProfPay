package com.profpay.wallet.ui.shared.utils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun convertTimestampToDateTime(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
    return sdf.format(date)
}
