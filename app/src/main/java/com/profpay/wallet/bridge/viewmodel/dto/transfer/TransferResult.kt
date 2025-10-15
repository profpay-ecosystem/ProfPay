package com.profpay.wallet.bridge.viewmodel.dto.transfer

sealed class TransferResult {
    object Success : TransferResult()

    data class Failure(
        val error: Throwable,
    ) : TransferResult()
}
