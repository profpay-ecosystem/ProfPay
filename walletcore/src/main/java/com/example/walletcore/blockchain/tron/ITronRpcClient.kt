package com.example.walletcore.blockchain.tron

import com.example.walletcore.blockchain.tron.models.TronTransactionReceipt
import com.example.walletcore.blockchain.tron.services.TronAccountsService
import com.example.walletcore.blockchain.tron.services.TronBroadcastService
import com.example.walletcore.blockchain.tron.services.TronCallService
import com.example.walletcore.blockchain.tron.services.TronNodeStatusService
import retrofit2.http.Body
import retrofit2.http.POST

interface ITronRpcClient :
    TronAccountsService,
    TronCallService,
    TronNodeStatusService,
    TronBroadcastService
{

    @POST("/wallet/gettransactioninfobyid")
    suspend fun transaction(@Body value: TronValue): Result<TronTransactionReceipt>

    class TronValue(val value: String)
}