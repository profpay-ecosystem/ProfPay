package com.example.walletcore.blockchain

import com.example.walletcore.blockchain.tron.client.BroadcastClient
import com.example.walletcore.primitives.Account
import com.example.walletcore.primitives.Chain
import com.example.walletcore.primitives.TransactionType

class BroadcastClientDispatcher(
    private val clients: List<BroadcastClient>,
) : BroadcastClient {

    override suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): String {
        return clients.getClient(account.chain)?.send(account, signedMessage, type)
            ?: throw Exception("Chain isn't support")
    }

    override fun supported(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }
}