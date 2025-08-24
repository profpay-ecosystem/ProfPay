package com.example.walletcore.blockchain.tron.services

import com.example.walletcore.blockchain.tron.api.TronCallApi
import com.example.walletcore.blockchain.tron.models.TronSmartContractResult

suspend fun TronCallApi.triggerSmartContract(
    contractAddress: String,
    functionSelector: String,
    parameter: String? = null,
    feeLimit: Long? = null,
    callValue: Long? = null,
    ownerAddress: String,
    visible: Boolean? = null,
): Result<TronSmartContractResult> {
    val call = mapOf(
        "contract_address" to contractAddress,
        "function_selector" to functionSelector,
        "parameter" to parameter,
        "fee_limit" to feeLimit,
        "call_value" to callValue,
        "owner_address" to ownerAddress,
        "visible" to visible,
    )
    return triggerSmartContract(call)
}