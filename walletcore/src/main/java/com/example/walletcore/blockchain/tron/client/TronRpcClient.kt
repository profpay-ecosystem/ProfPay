package com.example.walletcore.blockchain.tron.client

import com.example.walletcore.blockchain.tron.models.TronTransactionReceipt
import com.example.walletcore.blockchain.tron.api.TronAccountsApi
import com.example.walletcore.blockchain.tron.api.TronBroadcastApi
import com.example.walletcore.blockchain.tron.api.TronCallApi
import com.example.walletcore.blockchain.tron.api.TronNodeStatusApi
import retrofit2.http.Body
import retrofit2.http.POST

interface TronRpcClient :
    TronAccountsApi,
    TronCallApi,
    TronNodeStatusApi,
    TronBroadcastApi {

    @POST("/wallet/gettransactioninfobyid")
    suspend fun getTransactionInfoById(
        @Body request: TransactionIdRequest
    ): Result<TronTransactionReceipt>
}

data class TransactionIdRequest(val value: String)