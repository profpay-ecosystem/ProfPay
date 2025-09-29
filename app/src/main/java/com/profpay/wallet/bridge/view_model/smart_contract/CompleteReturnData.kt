package com.profpay.wallet.bridge.view_model.smart_contract

import com.profpay.wallet.bridge.view_model.smart_contract.usecases.DealActionResult

data class CompleteReturnData(
    val status: CompleteStatusesEnum,
    val result: DealActionResult? = null,
)
