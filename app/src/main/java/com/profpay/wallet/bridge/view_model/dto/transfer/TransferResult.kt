package com.profpay.wallet.bridge.view_model.dto.transfer

sealed class TransferResult {
    object Success : TransferResult()

    data class Failure(
        val error: Throwable,
    ) : TransferResult()
}
