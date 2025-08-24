package com.example.walletcore.blockchain

import com.example.walletcore.ConfirmParams
import com.example.walletcore.model.SignerParams
import com.example.walletcore.primitives.Chain
import com.example.walletcore.primitives.FeePriority

class BlockchainSignManager(private val blockchains: List<BlockchainSigner>) {
    suspend fun signTransaction(
        params: SignerParams,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chain = params.input.asset.id.chain
        val client = blockchains.getClient(chain) ?: throw Exception("Chain isn't support")
        val input = params.input
        return when (input) {
            is ConfirmParams.TransferParams.Generic -> client.signGenericTransfer(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.TransferParams.Native -> client.signNativeTransfer(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.TransferParams.Token -> client.signTokenTransfer(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.Activate -> client.signActivate(input, params.chainData, params.finalAmount, feePriority, privateKey)
        }
    }

    fun supported(chain: Chain): Boolean {
        return blockchains.getClient(chain) != null
    }
}