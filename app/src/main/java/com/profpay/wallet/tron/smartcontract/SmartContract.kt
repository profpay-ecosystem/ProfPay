package com.profpay.wallet.tron.smartcontract

import android.content.Context
import android.content.res.AssetManager
import com.google.protobuf.ByteString
import com.profpay.wallet.AppConstants
import com.profpay.wallet.utils.convertInputStreamToString
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.contract.Contract
import org.tron.trident.core.transaction.TransactionBuilder
import org.tron.trident.proto.Chain
import org.tron.trident.utils.Numeric

class SmartContract(
    val context: Context,
) {
    fun getSignedDeployMultiSigContract(
        ownerAddress: String,
        privateKey: String,
    ): ByteString? {
        val wrapper =
            ApiWrapper(
                AppConstants.Network.TRON_GRPC_ENDPOINT,
                AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY,
                privateKey,
            )
        val assetManager: AssetManager = context.assets
        val byteCode: String = convertInputStreamToString(assetManager.open("bytecode.txt")) // созданный байткод
        val abi: String = convertInputStreamToString(assetManager.open("abi.txt")) // созданный аби

        val cntr: Contract =
            Contract
                .Builder()
                .setName("MultiSigV3")
                .setOwnerAddr(ApiWrapper.parseAddress(ownerAddress))
                .setOriginAddr(ApiWrapper.parseAddress(ownerAddress))
                .setBytecode(ByteString.copyFrom(Numeric.hexStringToByteArray(byteCode)))
                .setAbi(abi)
                .setOriginEnergyLimit(10000000)
                .setConsumeUserResourcePercent(100)
                .build()
        cntr.wrapper = wrapper

        val builder: TransactionBuilder = cntr.deploy().setFeeLimit(2000000000)

        val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)

        wrapper.close() // TODO: try catch finally
        return signedTransaction.toByteString()
    }

    suspend fun estimateDeployingContract(privateKey: String): Pair<Int, Int> {
        val wrapper =
            ApiWrapper(
                AppConstants.Network.TRON_GRPC_ENDPOINT,
                AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY,
                privateKey,
            )

        val energy = AppConstants.SmartContract.PUBLISH_ENERGY_REQUIRED.toInt()
        val bandwidth = AppConstants.SmartContract.PUBLISH_BANDWIDTH_REQUIRED.toInt()

        // Энергия
        val entriesEnergy = wrapper.getEnergyPrices().prices.split(",")
        val lastEnergyEntry = entriesEnergy.last()
        val lastEnergyValue = lastEnergyEntry.split(":").last()
        val lastEnergyPrice = lastEnergyValue.toInt()

        // Сеть
        val entriesBandwith = wrapper.getBandwidthPrices().prices.split(",")
        val lastBandwithEntry = entriesBandwith.last()
        val lastBandwithValue = lastBandwithEntry.split(":").last()
        val lastBandwithPrice = lastBandwithValue.toInt()

        val energyResult = energy * lastEnergyPrice
        val bandwidthResult = bandwidth * lastBandwithPrice

        return Pair(energyResult, bandwidthResult)
    }

    fun ByteString.toHex(): String {
        val hexString = StringBuilder()
        for (byte in this) {
            val hex = String.format("%02x", byte)
            hexString.append(hex)
        }
        return hexString.toString()
    }

    val multiSigRead: MultiSigRead = MultiSigRead()
    val multiSigWrite: MultiSigWrite = MultiSigWrite()
}
