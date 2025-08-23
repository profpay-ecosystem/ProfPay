package com.example.walletcore.blockchain

import com.example.walletcore.ConfirmParams
import com.example.walletcore.model.ChainSignData
import com.example.walletcore.primitives.Chain
import com.example.walletcore.primitives.FeePriority
import java.math.BigInteger

interface ITransactionSigner : IChainClient {
    suspend fun signMessage(
        chain: Chain,
        input: ByteArray,
        privateKey: ByteArray,
    ): ByteArray = byteArrayOf()

    suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        amount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray,
    ): List<ByteArray> = emptyList()
}