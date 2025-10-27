package com.profpay.wallet.ui.shared.utils

fun formatAddress(address: String, quantitySymbols: Int = 7): String {
    return "${address.take(quantitySymbols)}...${address.takeLast(quantitySymbols)}"
}
