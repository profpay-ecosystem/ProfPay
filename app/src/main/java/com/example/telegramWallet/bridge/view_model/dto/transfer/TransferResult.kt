package com.example.telegramWallet.bridge.view_model.dto.transfer

sealed class TransferResult {
    object Success : TransferResult()

    data class Failure(
        val error: Throwable,
    ) : TransferResult()
}
