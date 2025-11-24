package com.profpay.wallet.data.services

import android.database.sqlite.SQLiteConstraintException
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.database.entities.wallet.TransactionStatusCode
import com.profpay.wallet.data.database.entities.wallet.TransactionType
import com.profpay.wallet.data.database.entities.wallet.assignTransactionType
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.PendingTransactionRepo
import com.profpay.wallet.data.database.repositories.wallet.TokenRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import com.profpay.wallet.tron.Tron
import com.profpay.wallet.tron.http.Trc20TransactionsApi
import com.profpay.wallet.tron.http.TrxTransactionsApi
import com.profpay.wallet.tron.http.models.Trc20TransactionsDataResponse
import com.profpay.wallet.tron.http.models.TrxTransactionDataResponse
import com.profpay.wallet.utils.safeRun
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionsRecoveryService @Inject constructor(
    private val addressRepo: AddressRepo,
    private val transactionsRepo: TransactionsRepo,
    private val tokenRepo: TokenRepo,
    private val tron: Tron,
    private val pendingTransactionRepo: PendingTransactionRepo,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun createTask(walletId: Long) = withContext(ioDispatcher) {
        val addresses = addressRepo.getAddressesSotsWithTokensByBlockchain("Tron", walletId)
        for (address in addresses) {
            coroutineScope {
                val trc20DataDeferred = async {
                    safeRun { Trc20TransactionsApi.trc20TransactionsService.makeRequest(address.addressEntity.address) } ?: emptyList()
                }
                delay(2000)

                val trxDataDeferred = async {
                    safeRun { TrxTransactionsApi.trxTransactionsService.makeRequest(address.addressEntity.address) } ?: emptyList()
                }
                delay(1000)

                val trc20Data = trc20DataDeferred.await()
                val trxData = trxDataDeferred.await()

                trc20Data.filter { it.type == "Transfer" }
                    .forEach { transferUsdt(it, "USDT", address.addressEntity.address) }

                trxData.forEach { trx ->
                    val contract = trx.raw_data.contract.firstOrNull() ?: return@forEach
                    when (trx.raw_data.contract[0].type) {
                        "TransferContract" -> {
                            val value = contract.parameter.value
                            if (value.to_address == null || value.amount == null) {
                                return@forEach
                            }

                            transferTrx(trx, "TRX", address.addressEntity.address)
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    suspend fun transferUsdt(
        transaction: Trc20TransactionsDataResponse,
        tokenName: String,
        address: String,
    ) = coroutineScope {
        val senderAddressEntity = addressRepo.getAddressEntityByAddress(transaction.from)
        val receiverAddressEntity = addressRepo.getAddressEntityByAddress(transaction.to)

        val (addressEntity, isSender) =
            when (address) {
                senderAddressEntity?.address -> Pair(senderAddressEntity, true)
                receiverAddressEntity?.address -> Pair(receiverAddressEntity, false)
                else -> return@coroutineScope
            }

        val amount = BigInteger(transaction.value)

        val isTransactionPending = transactionsRepo.isTransactionPending(transaction.transaction_id)

        if (isTransactionPending && isSender) {
            transactionsRepo.updateStatusAndTimestampByTxId(
                statusCode = TransactionStatusCode.SUCCESS.index,
                timestamp = transaction.block_timestamp,
                txid = transaction.transaction_id,
            )
        } else {
            try {
                transactionsRepo.insertNewTransaction(
                    TransactionEntity(
                        txId = transaction.transaction_id,
                        senderAddressId = senderAddressEntity?.addressId,
                        receiverAddressId = receiverAddressEntity?.addressId,
                        senderAddress = transaction.from,
                        receiverAddress = transaction.to,
                        walletId = addressEntity.walletId,
                        tokenName = tokenName,
                        amount = amount,
                        timestamp = transaction.block_timestamp,
                        status = "Success",
                        type = assignTransactionType(idSend = senderAddressEntity?.addressId, idReceive = receiverAddressEntity?.addressId),
                        statusCode = TransactionStatusCode.SUCCESS.index,
                    ),
                )
            } catch (_: SQLiteConstraintException) {
                return@coroutineScope
            }
        }

        if (senderAddressEntity != null) {
            val balance = tron.addressUtilities.getUsdtBalance(senderAddressEntity.address)
            tokenRepo.updateTronBalanceViaId(balance, senderAddressEntity.addressId!!, tokenName)
        }

        if (receiverAddressEntity != null) {
            val balance = tron.addressUtilities.getUsdtBalance(receiverAddressEntity.address)
            tokenRepo.updateTronBalanceViaId(balance, receiverAddressEntity.addressId!!, tokenName)
        }

        val transactionExists = pendingTransactionRepo.pendingTransactionIsExistsByTxId(transaction.transaction_id)
        if (transactionExists) {
            pendingTransactionRepo.deletePendingTransactionByTxId(transaction.transaction_id)
        }
    }

    suspend fun transferTrx(
        transaction: TrxTransactionDataResponse,
        tokenName: String,
        address: String,
    ) = coroutineScope {
        val contract = transaction.raw_data.contract[0]

        val ownerAddress =
            tron
                .addressUtilities
                .hexToBase58CheckAddress(contract.parameter.value.owner_address)

        val toAddress =
            tron
                .addressUtilities
                .hexToBase58CheckAddress(contract.parameter.value.to_address!!)

        val senderAddressEntity = addressRepo.getAddressEntityByAddress(ownerAddress)
        val receiverAddressEntity = addressRepo.getAddressEntityByAddress(toAddress)

        val (addressEntity, isSender) =
            when (address) {
                senderAddressEntity?.address -> Pair(senderAddressEntity, true)
                receiverAddressEntity?.address -> Pair(receiverAddressEntity, false)
                else -> return@coroutineScope
            }

        val isTransactionPending = transactionsRepo.isTransactionPending(transaction.txID)

        val typeValue: Int =
            when (contract.type) {
                "TransferContract" -> {
                    assignTransactionType(idSend = senderAddressEntity?.addressId, idReceive = receiverAddressEntity?.addressId)
                }
                "TriggerSmartContract" -> {
                    TransactionType.TRIGGER_SMART_CONTRACT.index
                }
                else -> return@coroutineScope
            }

        if (isTransactionPending && isSender) {
            transactionsRepo.updateStatusAndTimestampByTxId(
                statusCode = TransactionStatusCode.SUCCESS.index,
                timestamp = transaction.block_timestamp,
                txid = transaction.txID,
            )
        } else {
            try {
                transactionsRepo.insertNewTransaction(
                    TransactionEntity(
                        txId = transaction.txID,
                        senderAddressId = senderAddressEntity?.addressId,
                        receiverAddressId = receiverAddressEntity?.addressId,
                        senderAddress = ownerAddress,
                        receiverAddress = toAddress,
                        walletId = addressEntity.walletId,
                        tokenName = tokenName,
                        amount = BigInteger.valueOf(contract.parameter.value.amount!!),
                        timestamp = transaction.block_timestamp,
                        status = "Success",
                        type = typeValue,
                        statusCode = TransactionStatusCode.SUCCESS.index,
                    ),
                )
            } catch (_: SQLiteConstraintException) {
                return@coroutineScope
            }
        }

        if (senderAddressEntity != null) {
            val balance = tron.addressUtilities.getTrxBalance(senderAddressEntity.address)
            tokenRepo.updateTronBalanceViaId(balance, senderAddressEntity.addressId!!, tokenName)
        }

        if (receiverAddressEntity != null) {
            val balance = tron.addressUtilities.getTrxBalance(receiverAddressEntity.address)
            tokenRepo.updateTronBalanceViaId(balance, receiverAddressEntity.addressId!!, tokenName)
        }

        val transactionExists = pendingTransactionRepo.pendingTransactionIsExistsByTxId(transaction.txID)
        if (transactionExists) {
            pendingTransactionRepo.deletePendingTransactionByTxId(transaction.txID)
        }
    }
}
