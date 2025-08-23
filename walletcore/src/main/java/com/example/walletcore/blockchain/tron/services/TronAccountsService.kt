package com.example.walletcore.blockchain.tron.services

import android.util.Log
import com.example.walletcore.blockchain.tron.models.TronAccount
import com.example.walletcore.blockchain.tron.models.TronAccountRequest
import com.example.walletcore.blockchain.tron.models.TronAccountUsage
import com.example.walletcore.math.toHexString
import retrofit2.http.Body
import retrofit2.http.POST
import wallet.core.jni.Base58

interface TronAccountsService {
    @POST("/wallet/getaccount")
    suspend fun getAccount(@Body addressRequest: TronAccountRequest): Result<TronAccount>

    @POST("/wallet/getaccountnet")
    suspend fun getAccountUsage(@Body addressRequest: TronAccountRequest): Result<TronAccountUsage>
}

suspend fun TronAccountsService.getAccount(address: String, visible: Boolean = false): TronAccount? {
    return try {
        val result = getAccount(
            TronAccountRequest(
                address = address,
                visible = visible
            )
        )
        result.getOrThrow()
    } catch (err: Throwable) {
        Log.d("TRON-ACCOUNT", "Error: ", err)
        null
    }
}

suspend fun TronAccountsService.getAccountUsage(address: String): TronAccountUsage? {
    return getAccountUsage(
        TronAccountRequest(
            address = Base58.decode(address).toHexString(""),
            visible = false
        )
    ).getOrNull()
}