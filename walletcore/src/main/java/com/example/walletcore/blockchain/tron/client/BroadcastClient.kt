package com.example.walletcore.blockchain.tron.client

import com.example.walletcore.blockchain.ChainClient
import com.example.walletcore.primitives.Account
import com.example.walletcore.primitives.TransactionType

interface BroadcastClient : ChainClient {
    suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): String
}