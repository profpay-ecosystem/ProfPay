package com.profpay.wallet.data.services.pushy

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.R
import com.profpay.wallet.data.database.entities.wallet.SmartContractEntity
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.database.entities.wallet.TransactionStatusCode
import com.profpay.wallet.data.database.entities.wallet.assignTransactionType
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.database.repositories.wallet.PendingAmlTransactionRepo
import com.profpay.wallet.data.database.repositories.wallet.PendingTransactionRepo
import com.profpay.wallet.data.database.repositories.wallet.SmartContractRepo
import com.profpay.wallet.data.database.repositories.wallet.TokenRepo
import com.profpay.wallet.data.services.AmlProcessorService
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.tron.Tron
import com.profpay.wallet.utils.NotificationUtils.showNotification
import io.sentry.Sentry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigInteger
import javax.inject.Inject

class PushEventHandler @Inject constructor(
    private var amlProcessorService: AmlProcessorService,
    private val pendingAml: PendingAmlTransactionRepo,
    private val pendingTx: PendingTransactionRepo,
    private val txRepo: TransactionsRepo,
    private val addressRepo: AddressRepo,
    private val smartRepo: SmartContractRepo,
    private var tron: Tron,
    private var tokenRepo: TokenRepo,
    private var centralAddressRepo: CentralAddressRepo
) {
    suspend fun handle(context: Context, event: PushEvent, intent: Intent) {
        val notificationTitle = intent.getStringExtra("title") ?: "Уведомление"
        val notificationText = intent.getStringExtra("message") ?: "Сообщение"

        when (event) {
            is PushEvent.AmlPaymentSuccess -> {
                pendingAml.markAsSuccessful(event.transactionId)
                showNotification(context, notificationTitle, notificationText)
            }

            is PushEvent.AmlPaymentError -> {
                pendingAml.markAsError(event.transactionId)
                showNotification(context, notificationTitle, notificationText)
            }

            is PushEvent.TransferError -> {
                handleTransferError(event)
                showNotification(context, notificationTitle, notificationText)
            }

            is PushEvent.TransferSuccess -> {
                showNotification(context, notificationTitle, notificationText)
            }

            is PushEvent.DeployContractSuccess -> {
                handleDeploy(event)
                showNotification(context, notificationTitle, notificationText)
            }

            is PushEvent.NewTransaction -> {
                if (event.type == TransactionType.CentralAddress) {
                    handleCentralAddressTransaction(context, event)
                } else {
                    handleNewTransaction(context, event)
                }
            }
        }
    }

    private suspend fun handleTransferError(event: PushEvent.TransferError) {
        val address = addressRepo.getAddressEntityByAddress(event.senderAddress)
        pendingTx.deletePendingTransactionByTxId(event.transactionId)
        txRepo.deleteTransactionByTxId(event.transactionId)

        address?.addressId?.let {
            txRepo.transactionSetProcessedUpdateFalseByTxId(event.transactionId)
        }
    }

    private suspend fun handleDeploy(event: PushEvent.DeployContractSuccess) {
        if (smartRepo.getSmartContract() == null) {
            smartRepo.insert(
                SmartContractEntity(
                    contractAddress = event.contractAddress,
                    ownerAddress = event.address
                )
            )
        } else {
            smartRepo.restoreSmartContract(event.contractAddress)
        }
    }

    private suspend fun handleCentralAddressTransaction(context: Context, event: PushEvent.NewTransaction) = coroutineScope {
        val (senderAddressEntity, receiverAddressEntity) =
            listOf(
                async { addressRepo.getAddressEntityByAddress(event.from) },
                async { addressRepo.getAddressEntityByAddress(event.to) },
            ).awaitAll()

        val senderAddressId = senderAddressEntity?.addressId
        val receiverAddressId = receiverAddressEntity?.addressId

        val amount = BigInteger(event.amount)
        try {
            txRepo.insertNewTransaction(
                TransactionEntity(
                    txId = event.txid,
                    senderAddressId = senderAddressId,
                    receiverAddressId = receiverAddressId,
                    senderAddress = event.from,
                    receiverAddress = event.to,
                    walletId = 0,
                    tokenName = event.token.symbol,
                    amount = amount,
                    timestamp = event.blockTimestamp,
                    status = "Success",
                    type = assignTransactionType(idSend = senderAddressId, idReceive = receiverAddressId, isCentralAddress = true),
                    statusCode = TransactionStatusCode.SUCCESS.index,
                ),
            )
        } catch (_: SQLiteConstraintException) {
            return@coroutineScope
        }

        val centralAddress = centralAddressRepo.getCentralAddress()
        val balance = tron.addressUtilities.getTrxBalance(centralAddress!!.address)
        centralAddressRepo.updateTrxBalance(balance)

        if (centralAddress.address == event.to) {
            pendingTx.deletePendingTransactionByTxId(event.txid)
            showNotification(
                context,
                "\uD83D\uDCB0 Пополнение центрального адреса",
                "Получено: ${amount.toTokenAmount()} ${event.token.symbol}\n" +
                    "От ${event.from.take(6)}...${event.from.takeLast(4)}",
            )
        }

        val addresses = addressRepo.getAddressesSotsWithTokensByBlockchain("Tron")
        if (balance >= BigInteger.valueOf(1_500_000)) {
            for (addressData in addresses) {
                val newBalance = tron.addressUtilities.getTrxBalance(centralAddress.address)
                if (newBalance < BigInteger.valueOf(1_000_000)) break
                if (!tron.addressUtilities.isAddressActivated(addressData.addressEntity.address)) {
                    tron.transactions.trxTransfer(
                        fromAddress = centralAddress.address,
                        toAddress = addressData.addressEntity.address,
                        privateKey = centralAddress.privateKey,
                        amount = 1_000,
                    )
                }
            }
        }
    }

    private suspend fun handleNewTransaction(context: Context, event: PushEvent.NewTransaction) {
        val sharedPrefs = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            MODE_PRIVATE
        )

        val autoCheckAml = sharedPrefs.getBoolean(PrefKeys.AUTO_CHECK_AML, true)

        val senderAddressEntity = addressRepo.getAddressEntityByAddress(event.from)
        val receiverAddressEntity = addressRepo.getAddressEntityByAddress(event.to)

        val (addressEntity, isSender) =
            when (event.targetAddress) {
                senderAddressEntity?.address -> Pair(senderAddressEntity, true)
                receiverAddressEntity?.address -> Pair(receiverAddressEntity, false)
                else -> return
            }

        val amount = BigInteger(event.amount)

        val isTransactionPending = txRepo.isTransactionPending(event.txid)

        if (isTransactionPending && isSender) {
            txRepo.updateStatusAndTimestampByTxId(
                statusCode = TransactionStatusCode.SUCCESS.index,
                timestamp = event.blockTimestamp,
                txid = event.txid,
            )
        } else {
            try {
                txRepo.insertNewTransaction(
                    TransactionEntity(
                        txId = event.txid,
                        senderAddressId = senderAddressEntity?.addressId,
                        receiverAddressId = receiverAddressEntity?.addressId,
                        senderAddress = event.from,
                        receiverAddress = event.to,
                        walletId = addressEntity.walletId,
                        tokenName = event.token.symbol,
                        amount = amount,
                        timestamp = event.blockTimestamp,
                        status = "Success",
                        type = assignTransactionType(
                            idSend = senderAddressEntity?.addressId,
                            idReceive = receiverAddressEntity?.addressId
                        ),
                        statusCode = TransactionStatusCode.SUCCESS.index,
                    ),
                )
            } catch (_: SQLiteConstraintException) {
                return
            }
        }

        if (senderAddressEntity != null) {
            if (event.token == TransactionToken.TRX) {
                val balance = tron.addressUtilities.getTrxBalance(senderAddressEntity.address)
                tokenRepo.updateTronBalanceViaId(balance, senderAddressEntity.addressId!!, event.token.symbol)
            } else {
                val balance = tron.addressUtilities.getUsdtBalance(senderAddressEntity.address)
                tokenRepo.updateTronBalanceViaId(balance, senderAddressEntity.addressId!!, event.token.symbol)
            }
        }

        if (receiverAddressEntity != null) {
            if (autoCheckAml && senderAddressEntity == null) {
                runCatching {
                    val (isSuccessful, message) =
                        amlProcessorService.processAmlReport(
                            address = event.to,
                            txid = event.txid,
                        )

                    if (!isSuccessful) {
                        showNotification(context, "Ошибка авто. проверки AML", message)
                    }
                }.onFailure { ex ->
                    Sentry.captureException(ex)
                }
            }

            if (event.token == TransactionToken.TRX) {
                val balance = tron.addressUtilities.getTrxBalance(receiverAddressEntity.address)
                tokenRepo.updateTronBalanceViaId(balance, receiverAddressEntity.addressId!!, event.token.symbol)
            } else {
                val balance = tron.addressUtilities.getUsdtBalance(receiverAddressEntity.address)
                tokenRepo.updateTronBalanceViaId(balance, receiverAddressEntity.addressId!!, event.token.symbol)
            }
        }

        if (isSender) {
            pendingTx.deletePendingTransactionByTxId(event.txid)
            showNotification(
                context,
                "\uD83D\uDCB8 Отправлено: ${amount.toTokenAmount()} ${event.token.symbol}",
                "На ${event.to.take(6)}...${event.to.takeLast(4)}",
            )
        } else {
            showNotification(
                context,
                "\uD83D\uDCB0 Получено: ${amount.toTokenAmount()} ${event.token.symbol}",
                "От ${event.from.take(6)}...${event.from.takeLast(4)}",
            )
        }
    }
}

