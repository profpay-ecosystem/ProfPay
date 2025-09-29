package com.example.telegramWallet.data.database.models

interface HasTronCredentials {
    val address: String
    val privateKey: String
}
