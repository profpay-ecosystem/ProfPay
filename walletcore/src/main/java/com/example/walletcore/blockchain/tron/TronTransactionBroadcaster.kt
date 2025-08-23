package com.example.walletcore.blockchain.tron

import com.example.walletcore.blockchain.tron.services.TronBroadcastService
import com.example.walletcore.math.decodeHex
import com.example.walletcore.primitives.Account
import com.example.walletcore.primitives.Chain
import com.example.walletcore.primitives.ContentType
import com.example.walletcore.primitives.TransactionType
import com.example.walletcore.rpc.RpcError
import com.example.walletcore.rpc.ServiceError
import okhttp3.RequestBody.Companion.toRequestBody

class TronTransactionBroadcaster(
    private val chain: Chain,
    private val rpcClient: TronBroadcastService,
) : IBroadcastClient {
    override suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): String {
        val requestData = signedMessage.toRequestBody(ContentType.JSON.mediaType)

        val response = rpcClient.broadcast(requestData)
            .getOrNull()
            ?: throw ServiceError.NetworkError

        return when {
            response.result -> response.txid
            else -> throw RpcError.BroadcastFail(
                response.message.decodeHex().toString(Charsets.UTF_8)
            )
        }
    }

    override fun supported(chain: Chain): Boolean =
        this.chain == chain
}