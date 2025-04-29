package com.example.telegramWallet.tron

import com.example.telegramWallet.data.utils.toTokenAmount
import com.google.protobuf.ByteString
import org.tron.trident.abi.FunctionEncoder
import org.tron.trident.abi.TypeReference
import org.tron.trident.abi.datatypes.Address
import org.tron.trident.abi.datatypes.Bool
import org.tron.trident.abi.datatypes.Function
import org.tron.trident.abi.datatypes.Type
import org.tron.trident.abi.datatypes.generated.Uint256
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.contract.Contract
import org.tron.trident.core.contract.Trc20Contract
import org.tron.trident.core.exceptions.IllegalException
import org.tron.trident.core.transaction.TransactionBuilder
import org.tron.trident.proto.Chain
import org.tron.trident.proto.Response
import org.tron.trident.proto.Response.TransactionExtention
import java.math.BigInteger

// Обработка Tron API касаемо раздела транзакций.
class Transactions() {
    // Перевод USDT валюты на другой адрес.
    fun trc20Transfer(fromAddress: String?, toAddress: String?, privateKey: String, amount: Long): String {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)
        val contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"

        val contract: Contract = wrapper.getContract(contractAddress)
        val token = Trc20Contract(contract, fromAddress, wrapper)
        return token.transfer(toAddress, amount, 0, "", 45_000_000)
    }

    // Перевод TRX валюты на другой адрес.
    fun trxTransfer(fromAddress: String?, toAddress: String?, privateKey: String, amount: Long): String {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)

        val txnExt: TransactionExtention = wrapper.transfer(
            fromAddress, toAddress, amount
        )
        if (Response.TransactionReturn.response_code.SUCCESS !== txnExt.result.code) {
            throw IllegalException(txnExt.result.message.toStringUtf8())
        }

        val signedTransaction: Chain.Transaction = wrapper.signTransaction(txnExt)
        val broadcast = wrapper.broadcastTransaction(signedTransaction)
        wrapper.close()
        return broadcast
    }

    // Расчет комиссии для перевода в TRX.
    fun estimateEnergy(
        fromAddress: String?,
        toAddress: String?,
        privateKey: String,
        amount: BigInteger
    ): EstimateEnergyData {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)
        val contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"

        val transfer = Function(
            "transfer", listOf<Type<*>>(
                Address(toAddress), Uint256(amount)
            ), listOf<TypeReference<*>>(object : TypeReference<Bool?>() {})
        )

        val energyRequired = wrapper.estimateEnergy(
            fromAddress,
            contractAddress,
            transfer
        ).energyRequired

        val estimateEnergyData = EstimateEnergyData(
            energyRequired,
            ((energyRequired.toBigInteger() * getEnergyFee(wrapper)).toTokenAmount()).toBigInteger() + BigInteger.ONE
        )

        wrapper.close()
        return estimateEnergyData
    }

    fun getSignedTrxTransaction(
        fromAddress: String,
        toAddress: String,
        privateKey: String,
        amount: BigInteger
    ): ByteString {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)

        val txnExt: TransactionExtention = wrapper.transfer(fromAddress, toAddress, amount.toLong())
        if (Response.TransactionReturn.response_code.SUCCESS !== txnExt.result.code) {
            throw IllegalException(txnExt.result.message.toStringUtf8())
        }

        val signedTransaction: Chain.Transaction = wrapper.signTransaction(txnExt)

        wrapper.close()
        return signedTransaction.toByteString()
    }

    fun getSignedUsdtTransaction(
        fromAddress: String,
        toAddress: String,
        privateKey: String,
        amount: BigInteger
    ): ByteString? {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)
        val contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"

        val transfer = Function(
            "transfer", listOf<Type<*>>(
                Address(toAddress), Uint256(amount)
            ), listOf<TypeReference<*>>(object : TypeReference<Bool?>() {})
        )
        val builder: TransactionBuilder = wrapper.triggerCall(
            fromAddress, contractAddress, transfer
        )
        builder.setFeeLimit(45_000_000)
        builder.setMemo("")

        val signedTxn: Chain.Transaction = wrapper.signTransaction(builder.build())

        wrapper.close()
        return signedTxn.toByteString()
    }

    fun estimateBandwidthTrxTransaction(
        fromAddress: String,
        toAddress: String,
        privateKey: String,
        amount: BigInteger
    ): EstimateBandwidthData {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)

        val txnExt: TransactionExtention = wrapper.transfer(
            fromAddress, toAddress, amount.toLong()
        )
        val bandwidthRequired = wrapper.estimateBandwidth(txnExt.transaction)
        val costPerByteInTrx = 0.001 // Стоимость одного байта в TRX

        wrapper.close()
        return EstimateBandwidthData(
            bandwidthRequired + 50,
            (bandwidthRequired + 50) * costPerByteInTrx
        )
    }

    fun estimateBandwidth(
        fromAddress: String,
        toAddress: String,
        privateKey: String,
        amount: BigInteger
    ): EstimateBandwidthData {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)
        val contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"

        val transfer = Function(
            "transfer", listOf<Type<*>>(
                Address(toAddress), Uint256(amount)
            ), listOf<TypeReference<*>>(object : TypeReference<Bool?>() {})
        )
        val builder: TransactionBuilder = wrapper.triggerCall(
            fromAddress, contractAddress, transfer
        )
        builder.setFeeLimit(45_000_000)
        builder.setMemo("")

        val signedTxn: Chain.Transaction = wrapper.signTransaction(builder.build())
        val bandwidthRequired = wrapper.estimateBandwidth(signedTxn)
        val costPerByteInTrx = 0.001 // Стоимость одного байта в TRX

        wrapper.close()
        return EstimateBandwidthData(
            bandwidthRequired + 50,
            (bandwidthRequired + 50) * costPerByteInTrx
        )
    }

    fun estimateBandwidth(
        function: Function,
        contractAddress: String,
        address: String,
        privateKey: String
    ): EstimateBandwidthData {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)

        val func = FunctionEncoder.encode(function)

        val builder: TransactionBuilder = wrapper.triggerCallV2(
            address, contractAddress, func
        )
        builder.setFeeLimit(45_000_000)
        builder.setMemo("")

        val signedTxn: Chain.Transaction = wrapper.signTransaction(builder.build())
        val bandwidthRequired = wrapper.estimateBandwidth(signedTxn)
        val costPerByteInTrx = 0.001 // Стоимость одного байта в TRX

        wrapper.close()
        return EstimateBandwidthData(
            bandwidthRequired + 50,
            (bandwidthRequired + 50) * costPerByteInTrx
        )
    }

    fun estimateEnergy(function: Function, contractAddress: String, address: String, privateKey: String): EstimateEnergyData {
        val wrapper = ApiWrapper("5.39.223.8:59151", "5.39.223.8:50061", privateKey)

        val func = FunctionEncoder.encode(function)
        val energyRequired = wrapper.estimateEnergyV2(
            address,
            contractAddress,
            func
        )

        val estimateEnergyData = EstimateEnergyData(
            energyRequired.energyRequired,
            energyRequired.energyRequired.toBigInteger() * getEnergyFee(wrapper)
        )

        wrapper.close()
        return estimateEnergyData
    }

    // Получение параметра из ноды.
    private fun getEnergyFee(wrapper: ApiWrapper): BigInteger {
        for (value in wrapper.chainParameters.chainParameterList) {
            if (value.key == "getEnergyFee") {
                return value.value.toBigInteger()
            }
        }
        return BigInteger.ZERO
    }
}

data class EstimateEnergyData(
    val energy: Long,
    val energyInTrx: BigInteger
)

data class EstimateBandwidthData(
    val bandwidth: Long,
    val bandwidthInTrx: Double
)