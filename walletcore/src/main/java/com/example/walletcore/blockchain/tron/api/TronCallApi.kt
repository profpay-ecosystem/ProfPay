package com.example.walletcore.blockchain.tron.api

import com.example.walletcore.blockchain.tron.models.TronSmartContractResult
import retrofit2.http.Body
import retrofit2.http.POST

interface TronCallApi {
    @POST("/wallet/triggerconstantcontract")
    suspend fun triggerSmartContract(@Body addressRequest: Any): Result<TronSmartContractResult>
}
