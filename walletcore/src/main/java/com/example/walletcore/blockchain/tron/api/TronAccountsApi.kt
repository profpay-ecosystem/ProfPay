package com.example.walletcore.blockchain.tron.api

import com.example.walletcore.blockchain.tron.models.TronAccount
import com.example.walletcore.blockchain.tron.models.TronAccountRequest
import com.example.walletcore.blockchain.tron.models.TronAccountUsage
import retrofit2.http.Body
import retrofit2.http.POST

interface TronAccountsApi {
    @POST("/wallet/getaccount")
    suspend fun getAccount(@Body addressRequest: TronAccountRequest): Result<TronAccount>

    @POST("/wallet/getaccountnet")
    suspend fun getAccountUsage(@Body addressRequest: TronAccountRequest): Result<TronAccountUsage>
}