package com.example.walletcore.blockchain.tron

import com.example.walletcore.blockchain.IChainClient
import com.example.walletcore.primitives.Account
import com.example.walletcore.primitives.TransactionType

interface IBroadcastClient : IChainClient {
    suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): String
}