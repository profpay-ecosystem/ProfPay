package com.example.walletcore.blockchain.tron

import android.text.format.DateUtils
import com.example.walletcore.ConfirmParams
import com.example.walletcore.blockchain.IBlockchainSigner
import com.example.walletcore.math.decodeHex
import com.example.walletcore.model.ChainSignData
import com.example.walletcore.primitives.Chain
import com.example.walletcore.primitives.FeePriority
import com.google.protobuf.ByteString
import java.math.BigInteger

import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Tron
import wallet.core.jni.proto.Tron.TransferContract
import wallet.core.jni.proto.Tron.TransferTRC20Contract

class TronSigner(private val chain: Chain) : IBlockchainSigner {
    override fun supported(chain: Chain): Boolean = this.chain == chain

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        amount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = chainData as TronSigningContextBuilder.TronChainData
        val contract = TransferContract.newBuilder().apply {
            this.amount = amount.toLong()
            this.ownerAddress = params.from.address
            this.toAddress = params.destination().address
        }.build()
        return signTransfer(chainData, contract, privateKey)
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        amount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = chainData as TronSigningContextBuilder.TronChainData
        val contract = TransferTRC20Contract.newBuilder().apply {
            this.contractAddress = params.assetId.tokenId!!
            this.ownerAddress = params.from.address
            this.toAddress = params.destination().address
            this.amount = ByteString.copyFrom(amount.toByteArray())
        }.build()
        return signTransfer(chainData, contract, privateKey)
    }

    private fun signTransfer(
        chainData: TronSigningContextBuilder.TronChainData,
        contract: Any,
        privateKey: ByteArray,
    ): List<ByteArray> {
        val transaction = Tron.Transaction.newBuilder().apply {
            this.blockHeader = Tron.BlockHeader.newBuilder().apply {
                this.number = chainData.number
                this.parentHash = ByteString.copyFrom(chainData.parentHash.decodeHex())
                this.timestamp = chainData.timestamp
                this.version = chainData.version.toInt()
                this.witnessAddress = ByteString.copyFrom(chainData.witnessAddress.decodeHex())
                this.txTrieRoot = ByteString.copyFrom(chainData.txTrieRoot.decodeHex())
            }.build()
            when (contract) {
                is TransferTRC20Contract -> this.transferTrc20Contract = contract
                is TransferContract -> this.transfer = contract
            }
            this.expiration = chainData.timestamp + 10 * DateUtils.HOUR_IN_MILLIS
            this.timestamp = chainData.timestamp
            this.feeLimit = chainData.fee().amount.toLong()
        }
        val signInput = Tron.SigningInput.newBuilder().apply {
            this.transaction = transaction.build()
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val signingOutput = AnySigner.sign(signInput, CoinType.TRON, Tron.SigningOutput.parser())
        return listOf(signingOutput.json.toByteArray())
    }
}