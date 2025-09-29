package com.example.telegramWallet.ui.shared.utils

fun checkingValuesInList(
    listValue: List<Any>,
    listIndex: List<Int>,
    listMain: List<CharArray>,
): Boolean {
    var counter = 0
    for (index in listIndex) {
        if (listValue.contains(String(listMain[index]))) {
            counter++
        }
    }
    return counter == listValue.size
}
