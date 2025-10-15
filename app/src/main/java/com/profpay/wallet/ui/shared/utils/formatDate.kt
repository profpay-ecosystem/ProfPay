package com.profpay.wallet.ui.shared.utils
import java.text.SimpleDateFormat
import java.util.Locale

fun formatDate(inputDate: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat =
        SimpleDateFormat(
            "dd MMMM",
            Locale("ru", "RU"),
        ) // Устанавливаем локаль для русского языка

    val date = inputFormat.parse(inputDate)
    return outputFormat.format(date!!)
}
