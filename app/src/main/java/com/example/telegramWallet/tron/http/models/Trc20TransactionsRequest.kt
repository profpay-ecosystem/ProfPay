package com.example.telegramWallet.tron.http.models

data class Trc20TransactionsRequest (
    val only_confirmed: Boolean,
    val contract_address: String,
    val limit: Int
)