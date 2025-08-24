package com.example.walletcore.blockchain.tron.signer

import android.text.format.DateUtils
import com.example.walletcore.ConfirmParams
import com.example.walletcore.blockchain.BlockchainSigner
import com.example.walletcore.blockchain.tron.manager.TronSigningContextBuilder
import com.example.walletcore.math.decodeHex
import com.example.walletcore.model.ChainSignData
import com.example.walletcore.primitives.Chain
import com.example.walletcore.primitives.FeePriority
import com.google.protobuf.ByteString
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Tron
import java.math.BigInteger

class TronTransactionSigner(
    private val chain: Chain
) : BlockchainSigner {
    override fun supported(chain: Chain): Boolean = this.chain == chain

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        amount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val tronData = chainData as TronSigningContextBuilder.TronChainData
        val contract = Tron.TransferContract.newBuilder()
            .setAmount(amount.toLong())
            .setOwnerAddress(params.from.address)
            .setToAddress(params.destination().address)
            .build()

        return sign(tronData, contract, privateKey)
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        amount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val tronData = chainData as TronSigningContextBuilder.TronChainData
        val contract = Tron.TransferTRC20Contract.newBuilder()
            .setContractAddress(params.assetId.tokenId!!)
            .setOwnerAddress(params.from.address)
            .setToAddress(params.destination().address)
            .setAmount(ByteString.copyFrom(amount.toByteArray()))
            .build()

        return sign(tronData, contract, privateKey)
    }

    private fun sign(
        chainData: TronSigningContextBuilder.TronChainData,
        contract: Any,
        privateKey: ByteArray,
    ): List<ByteArray> {
        val transaction = buildTransaction(chainData, contract)
        val input = Tron.SigningInput.newBuilder()
            .setTransaction(transaction)
            .setPrivateKey(ByteString.copyFrom(privateKey))
            .build()

        val output = AnySigner.sign(input, CoinType.TRON, Tron.SigningOutput.parser())
        return listOf(output.json.toByteArray())
    }

    private fun buildTransaction(
        chainData: TronSigningContextBuilder.TronChainData,
        contract: Any
    ): Tron.Transaction {
        val header = Tron.BlockHeader.newBuilder()
            .setNumber(chainData.number)
            .setParentHash(ByteString.copyFrom(chainData.parentHash.decodeHex()))
            .setTimestamp(chainData.timestamp)
            .setVersion(chainData.version.toInt())
            .setWitnessAddress(ByteString.copyFrom(chainData.witnessAddress.decodeHex()))
            .setTxTrieRoot(ByteString.copyFrom(chainData.txTrieRoot.decodeHex()))
            .build()

        return Tron.Transaction.newBuilder().apply {
            blockHeader = header
            expiration = chainData.timestamp + 10 * DateUtils.HOUR_IN_MILLIS
            timestamp = chainData.timestamp
            feeLimit = chainData.fee().amount.toLong()

            when (contract) {
                is Tron.TransferContract -> transfer = contract
                is Tron.TransferTRC20Contract -> transferTrc20Contract = contract
                else -> throw IllegalArgumentException("Unsupported contract type: ${contract::class}")
            }
        }.build()
    }
}