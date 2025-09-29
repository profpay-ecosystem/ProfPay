package com.example.telegramWallet.bridge.view_model.smart_contract

import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.DealActionResult

data class CompleteReturnData(
    val status: CompleteStatusesEnum,
    val result: DealActionResult? = null,
)
