package com.profpay.wallet.bridge.viewmodel.smartcontract

import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.DealActionResult

data class CompleteReturnData(
    val status: CompleteStatusesEnum,
    val result: DealActionResult? = null,
)
