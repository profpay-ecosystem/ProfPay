package com.example.walletcore.blockchain.tron.services

import android.util.Log
import com.example.walletcore.blockchain.tron.api.TronAccountsApi
import com.example.walletcore.blockchain.tron.models.TronAccount
import com.example.walletcore.blockchain.tron.models.TronAccountRequest
import com.example.walletcore.blockchain.tron.models.TronAccountUsage
import com.example.walletcore.math.toHexString
import wallet.core.jni.Base58

suspend fun TronAccountsApi.getAccount(
    address: String,
    visible: Boolean = false
): TronAccount? =
    runCatching {
        getAccount(
            TronAccountRequest(
                address = address,
                visible = visible
            )
        ).getOrThrow()
    }.onFailure { err ->
        Log.e("TronAccountsService", "getAccount error", err)
    }.getOrNull()

suspend fun TronAccountsApi.getAccountUsage(address: String): TronAccountUsage? =
    runCatching {
        getAccountUsage(
            TronAccountRequest(
                address = Base58.decode(address).toHexString(""),
                visible = false
            )
        ).getOrThrow()
    }.onFailure { err ->
        Log.e("TronAccountsService", "getAccountUsage error", err)
    }.getOrNull()