package com.profpay.wallet.tron.smart_contract

import com.profpay.wallet.AppConstants
import com.google.protobuf.ByteString
import org.tron.trident.abi.TypeReference
import org.tron.trident.abi.datatypes.Address
import org.tron.trident.abi.datatypes.Bool
import org.tron.trident.abi.datatypes.Function
import org.tron.trident.abi.datatypes.Type
import org.tron.trident.abi.datatypes.generated.Uint256
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.transaction.TransactionBuilder
import org.tron.trident.proto.Chain
import java.math.BigInteger

data class DealData(
    val sellerAddress: String,
    val buyerAddress: String,
    val amount: Long,
    val admin1Address: String,
    val admin2Address: String,
    val admin3Address: String,
    val adminFee1: Long,
    val adminFee2: Long,
    val adminFee3: Long,
)

class MultiSigWrite {
    fun executeDisputed(
        id: Long,
        ownerAddress: String,
        privateKey: String,
        contractAddress: String,
    ): ByteString {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)

        val executeDisputed =
            Function(
                "executeDisputed",
                listOf(Uint256(id)),
                emptyList<TypeReference<*>>(),
            )
        val builder: TransactionBuilder = wrapper.triggerCall(ownerAddress, contractAddress, executeDisputed).setFeeLimit(140_000_000)
        val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)

        wrapper.close()
        return signedTransaction.toByteString()
    }

    /**
     * Позволяет покупателю внести сумму токенов для конкретной сделки.
     */
    fun depositDeal(
        id: Long,
        ownerAddress: String,
        privateKey: String,
        contractAddress: String,
    ): ByteString {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)

        val depositDeal =
            Function(
                "depositDeal",
                listOf(Uint256(id)),
                emptyList<TypeReference<*>>(),
            )
        val builder: TransactionBuilder = wrapper.triggerCall(ownerAddress, contractAddress, depositDeal).setFeeLimit(150_000_000)
        val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)

        wrapper.close()
        return signedTransaction.toByteString()
    }

    /**
     * Позволяет участникам голосовать за завершение сделки.
     */
    fun voteDeal(
        id: Long,
        ownerAddress: String,
        privateKey: String,
        contractAddress: String,
    ): ByteString {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)

        val voteDeal =
            Function(
                "voteDeal",
                listOf(Uint256(id)),
                emptyList<TypeReference<*>>(),
            )
        val builder: TransactionBuilder = wrapper.triggerCall(ownerAddress, contractAddress, voteDeal).setFeeLimit(140_000_000)
        val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)

        wrapper.close()
        return signedTransaction.toByteString()
    }

    /**
     * Позволяет закрыть контракт на ранних этапах, вернуть деньги покупателю и отдать комиссии админам.
     */
    fun cancelDeal(
        id: Long,
        ownerAddress: String,
        privateKey: String,
        contractAddress: String,
    ): ByteString {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)

        val cancelDeal =
            Function(
                "cancelDeal",
                listOf(Uint256(id)),
                emptyList<TypeReference<*>>(),
            )
        val builder: TransactionBuilder = wrapper.triggerCall(ownerAddress, contractAddress, cancelDeal).setFeeLimit(140_000_000)
        val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)

        wrapper.close()
        return signedTransaction.toByteString()
    }

    fun paySellerExpertFee(
        id: Long,
        ownerAddress: String,
        privateKey: String,
        contractAddress: String,
    ): ByteString {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)

        val paySellerExpertFee =
            Function(
                "paySellerExpertFee",
                listOf(Uint256(id)),
                emptyList<TypeReference<*>>(),
            )
        val builder: TransactionBuilder = wrapper.triggerCall(ownerAddress, contractAddress, paySellerExpertFee).setFeeLimit(140_000_000)
        val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)

        wrapper.close()
        return signedTransaction.toByteString()
    }

    /**
     * Дает доступ контракту для перевода USDT с адреса указанного в контракте.
     */
    fun approve(
        ownerAddress: String,
        privateKey: String,
        contractAddress: String,
    ): ByteString {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)

        val builder =
            wrapper.triggerCall(
                ownerAddress,
                "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
                Function(
                    "approve",
                    listOf(
                        Address(contractAddress),
                        Uint256(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(10L).pow(6))),
                    ),
                    emptyList<TypeReference<Bool>>(),
                ),
            )
        builder.setFeeLimit(55_000_000)
        builder.setMemo("")

        val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.build())

        wrapper.close()
        return signedTransaction.toByteString()
    }

    fun assignDecisionAdminAndSetAmounts(
        id: Long,
        ownerAddress: String,
        privateKey: String,
        contractAddress: String,
        sellerValue: BigInteger,
        buyerValue: BigInteger,
    ): ByteString {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)

        val assignDecisionAdminAndSetAmounts =
            Function(
                "assignDecisionAdminAndSetAmounts",
                listOf(
                    Uint256(id),
                    Uint256(sellerValue),
                    Uint256(buyerValue),
                ),
                emptyList<TypeReference<*>>(),
            )
        val builder: TransactionBuilder =
            wrapper
                .triggerCall(
                    ownerAddress,
                    contractAddress,
                    assignDecisionAdminAndSetAmounts,
                ).setFeeLimit(140_000_000)
        val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)

        wrapper.close()
        return signedTransaction.toByteString()
    }

    fun voteOnDisputeResolution(
        id: Long,
        ownerAddress: String,
        privateKey: String,
        contractAddress: String,
    ): ByteString {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)

        val voteOnDisputeResolution =
            Function(
                "voteOnDisputeResolution",
                listOf(Uint256(id)),
                emptyList<TypeReference<*>>(),
            )
        val builder: TransactionBuilder =
            wrapper
                .triggerCall(
                    ownerAddress,
                    contractAddress,
                    voteOnDisputeResolution,
                ).setFeeLimit(140_000_000)
        val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)

        wrapper.close()
        return signedTransaction.toByteString()
    }

    fun declineDisputeResolution(
        id: Long,
        ownerAddress: String,
        privateKey: String,
        contractAddress: String,
    ): ByteString {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)

        val declineDisputeResolution =
            Function(
                "declineDisputeResolution",
                listOf(Uint256(id)),
                emptyList<TypeReference<*>>(),
            )
        val builder: TransactionBuilder =
            wrapper
                .triggerCall(
                    ownerAddress,
                    contractAddress,
                    declineDisputeResolution,
                ).setFeeLimit(140_000_000)
        val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)

        wrapper.close()
        return signedTransaction.toByteString()
    }

    fun createDeal(
        ownerAddress: String,
        contractAddress: String,
        privateKey: String,
        params: MutableList<Type<*>> = ArrayList(),
    ): ByteString {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)

        val createDeal =
            Function(
                "createDeal",
                params,
                listOf(object : TypeReference<Uint256?>() {}),
            )

        val builder = wrapper.triggerCall(ownerAddress, contractAddress, createDeal).setFeeLimit(140_000_000)
        val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)
        return signedTransaction.toByteString()
    }
}
