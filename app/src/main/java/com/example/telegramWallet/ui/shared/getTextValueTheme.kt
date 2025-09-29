package com.example.telegramWallet.ui.shared

fun getTextValueTheme(themeSharedInt: Int): String {
    var colorTheme = ""
    when (themeSharedInt) {
        0 -> colorTheme = "Светлая"
        1 -> colorTheme = "Тёмная"
        2 -> colorTheme = "Системная"
    }
    return colorTheme
}
