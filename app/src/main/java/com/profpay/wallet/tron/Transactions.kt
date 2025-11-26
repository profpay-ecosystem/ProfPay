package com.profpay.wallet.tron

import android.util.Log
import com.google.protobuf.ByteString
import com.profpay.wallet.data.utils.toTokenAmount
import org.bitcoinj.base.internal.ByteUtils
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
import java.security.MessageDigest


data class SignedTransactionData(
    val txid: String,
    val signedTxn: ByteString?,
)

// Обработка Tron API касаемо раздела транзакций.
class Transactions {
    // Перевод USDT валюты на другой адрес.
    fun trc20Transfer(
        fromAddress: String?,
        toAddress: String?,
        privateKey: ByteArray,
        amount: Long,
    ): String {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            val contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
            try {
                val contract: Contract = wrapper.getContract(contractAddress)
                val token = Trc20Contract(contract, fromAddress, wrapper)
                token.transfer(toAddress, amount, 0, "", 45_000_000)
            } finally {
                try {
                    wrapper.close()
                } catch (e: Exception) {
                    Log.e("wrapper.close()", "Warning: failed to close wrapper: $e")
                }
            }
        }
    }

    // Перевод TRX валюты на другой адрес.
    fun trxTransfer(
        fromAddress: String?,
        toAddress: String?,
        privateKey: String,
        amount: Long,
    ): String {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                privateKey
            )

            try {
                val txnExt: TransactionExtention = wrapper.transfer(fromAddress, toAddress, amount)
                if (Response.TransactionReturn.response_code.SUCCESS !== txnExt.result.code) {
                    throw IllegalException(txnExt.result.message.toStringUtf8())
                }

                val signedTransaction: Chain.Transaction = wrapper.signTransaction(txnExt)
                val broadcast = wrapper.broadcastTransaction(signedTransaction)
                broadcast
            } finally {
                try {
                    wrapper.close()
                } catch (e: Exception) {
                    Log.e("wrapper.close()", "Warning: failed to close wrapper: $e")
                }
            }
        }
    }

    // Расчет комиссии для перевода в TRX.
    fun estimateEnergy(
        fromAddress: String?,
        toAddress: String?,
        privateKey: ByteArray,
        amount: BigInteger,
    ): EstimateEnergyData {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            val contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
            try {
                val transfer =
                    Function(
                        "transfer",
                        listOf<Type<*>>(
                            Address(toAddress),
                            Uint256(amount),
                        ),
                        listOf<TypeReference<*>>(object : TypeReference<Bool?>() {}),
                    )

                val energyRequired =
                    wrapper
                        .estimateEnergy(
                            fromAddress,
                            contractAddress,
                            transfer,
                        ).energyRequired

                val estimateEnergyData =
                    EstimateEnergyData(
                        energyRequired,
                        ((energyRequired.toBigInteger() * getEnergyFee(wrapper)).toTokenAmount()).toBigInteger() + BigInteger.ONE,
                    )
                estimateEnergyData
            } finally {
                try {
                    wrapper.close()
                } catch (e: Exception) {
                    Log.e("wrapper.close()", "Warning: failed to close wrapper: $e")
                }
            }
        }
    }

    fun getSignedTrxTransaction(
        fromAddress: String,
        toAddress: String,
        privateKey: ByteArray,
        amount: BigInteger,
    ): SignedTransactionData {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            try {
                val txnExt: TransactionExtention = wrapper.transfer(fromAddress, toAddress, amount.toLong())
                if (Response.TransactionReturn.response_code.SUCCESS !== txnExt.result.code) {
                    throw IllegalException(txnExt.result.message.toStringUtf8())
                }

                val signedTransaction: Chain.Transaction = wrapper.signTransaction(txnExt)
                val rawDataBytes = signedTransaction.rawData.toByteArray()
                val txidBytes = MessageDigest.getInstance("SHA-256").digest(rawDataBytes)
                val txidHex = txidBytes.joinToString("") { "%02x".format(it) }

                SignedTransactionData(
                    txid = txidHex,
                    signedTxn = signedTransaction.toByteString(),
                )
            } finally {
                try {
                    wrapper.close()
                } catch (e: Exception) {
                    Log.e("wrapper.close()", "Warning: failed to close wrapper: $e")
                }
            }
        }
    }

    fun getSignedUsdtTransaction(
        fromAddress: String,
        toAddress: String,
        privateKey: ByteArray,
        amount: BigInteger,
    ): SignedTransactionData {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )
            val contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"

            try {
                val transfer =
                    Function(
                        "transfer",
                        listOf<Type<*>>(
                            Address(toAddress),
                            Uint256(amount),
                        ),
                        listOf<TypeReference<*>>(object : TypeReference<Bool?>() {}),
                    )
                val builder: TransactionBuilder =
                    wrapper.triggerCall(
                        fromAddress,
                        contractAddress,
                        transfer,
                    )
                builder.setFeeLimit(45_000_000)
                builder.setMemo("")

                val signedTxn: Chain.Transaction = wrapper.signTransaction(builder.build())

                val rawDataBytes = signedTxn.rawData.toByteArray()
                val txidBytes = MessageDigest.getInstance("SHA-256").digest(rawDataBytes)
                val txidHex = txidBytes.joinToString("") { "%02x".format(it) }

                SignedTransactionData(
                    txid = txidHex,
                    signedTxn = signedTxn.toByteString(),
                )
            } finally {
                try {
                    wrapper.close()
                } catch (e: Exception) {
                    Log.e("wrapper.close()", "Warning: failed to close wrapper: $e")
                }
            }
        }
    }

    fun estimateBandwidthTrxTransaction(
        fromAddress: String,
        toAddress: String,
        privateKey: ByteArray,
        amount: BigInteger,
    ): EstimateBandwidthData {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            try {
                val txnExt: TransactionExtention =
                    wrapper.transfer(
                        fromAddress,
                        toAddress,
                        amount.toLong(),
                    )
                val bandwidthRequired = wrapper.estimateBandwidth(txnExt.transaction)
                val costPerByteInTrx = 0.001 // Стоимость одного байта в TRX

                EstimateBandwidthData(
                    bandwidthRequired + 50,
                    (bandwidthRequired + 50) * costPerByteInTrx,
                )
            } finally {
                try {
                    wrapper.close()
                } catch (e: Exception) {
                    Log.e("wrapper.close()", "Warning: failed to close wrapper: $e")
                }
            }
        }
    }

    fun estimateBandwidth(
        fromAddress: String,
        toAddress: String,
        privateKey: ByteArray,
        amount: BigInteger,
    ): EstimateBandwidthData {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            val contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
            try {
                val transfer =
                    Function(
                        "transfer",
                        listOf<Type<*>>(
                            Address(toAddress),
                            Uint256(amount),
                        ),
                        listOf<TypeReference<*>>(object : TypeReference<Bool?>() {}),
                    )
                val builder: TransactionBuilder =
                    wrapper.triggerCall(
                        fromAddress,
                        contractAddress,
                        transfer,
                    )
                builder.setFeeLimit(45_000_000)
                builder.setMemo("")

                val signedTxn: Chain.Transaction = wrapper.signTransaction(builder.build())
                val bandwidthRequired = wrapper.estimateBandwidth(signedTxn)
                val costPerByteInTrx = 0.001 // Стоимость одного байта в TRX

                EstimateBandwidthData(
                    bandwidthRequired + 50,
                    (bandwidthRequired + 50) * costPerByteInTrx,
                )
            } finally {
                try {
                    wrapper.close()
                } catch (e: Exception) {
                    Log.e("wrapper.close()", "Warning: failed to close wrapper: $e")
                }
            }
        }
    }

    fun estimateBandwidth(
        function: Function,
        contractAddress: String,
        address: String,
        privateKey: ByteArray,
    ): EstimateBandwidthData {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            try {
                val func = FunctionEncoder.encode(function)

                val builder: TransactionBuilder =
                    wrapper.triggerCallV2(
                        address,
                        contractAddress,
                        func,
                    )
                builder.setFeeLimit(45_000_000)
                builder.setMemo("")

                val signedTxn: Chain.Transaction = wrapper.signTransaction(builder.build())
                val bandwidthRequired = wrapper.estimateBandwidth(signedTxn)
                val costPerByteInTrx = 0.001 // Стоимость одного байта в TRX

                EstimateBandwidthData(
                    bandwidthRequired + 50,
                    (bandwidthRequired + 50) * costPerByteInTrx,
                )
            } finally {
                try {
                    wrapper.close()
                } catch (e: Exception) {
                    Log.e("wrapper.close()", "Warning: failed to close wrapper: $e")
                }
            }
        }
    }

    fun estimateEnergy(
        function: Function,
        contractAddress: String,
        address: String,
        privateKey: ByteArray,
    ): EstimateEnergyData {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            try {
                val func = FunctionEncoder.encode(function)
                val energyRequired =
                    wrapper.estimateEnergyV2(
                        address,
                        contractAddress,
                        func,
                    )

                val estimateEnergyData =
                    EstimateEnergyData(
                        energyRequired.energyRequired,
                        energyRequired.energyRequired.toBigInteger() * getEnergyFee(wrapper),
                    )

                estimateEnergyData
            } finally {
                try {
                    wrapper.close()
                } catch (e: Exception) {
                    Log.e("wrapper.close()", "Warning: failed to close wrapper: $e")
                }
            }
        }
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
    val energyInTrx: BigInteger,
)

data class EstimateBandwidthData(
    val bandwidth: Long,
    val bandwidthInTrx: Double,
)
