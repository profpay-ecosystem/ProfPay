package com.example.telegramWallet.data.scheduler.transfer.tron

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.telegramWallet.backend.grpc.AmlGrpcClient
import com.example.telegramWallet.data.database.entities.wallet.TransactionEntity
import com.example.telegramWallet.data.database.entities.wallet.TransactionType
import com.example.telegramWallet.data.database.entities.wallet.assignTransactionType
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.CentralAddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.PendingTransactionRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.flow_db.repo.AmlResult
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.tron.Tron
import com.example.telegramWallet.tron.http.Trc20TransactionsApi
import com.example.telegramWallet.tron.http.TrxTransactionsApi
import com.example.telegramWallet.tron.http.models.Trc20TransactionsDataResponse
import com.example.telegramWallet.tron.http.models.TrxTransactionDataResponse
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.time.Duration
import java.time.Instant

class UsdtTransferScheduler(
    var addressRepo: AddressRepo,
    var profileRepo: ProfileRepo,
    private var transactionsRepo: TransactionsRepo,
    private var tokenRepo: TokenRepo,
    private var centralAddressRepo: CentralAddressRepo,
    private var notificationFunction: (String, String) -> Unit,
    private var tron: Tron,
    private var pendingTransactionRepo: PendingTransactionRepo,
    private var amlClient: AmlGrpcClient
) {
    suspend fun scheduleAddresses() = withContext(Dispatchers.IO) {
        val addressList = addressRepo.getAddressesSotsWithTokensByBlockchain("Tron")
        val centralAddress = centralAddressRepo.getCentralAddress()
        for (address in addressList) {
            // Запрос к API на получение TRC20 транзакций.
            try {
                val trc20Data = Trc20TransactionsApi.trc20TransactionsService.makeRequest(address.addressEntity.address)
                for (transaction in trc20Data) {
                    if (transaction.type == "Transfer") {
                        transferUsdt(transaction, "USDT", address.addressEntity.address)
                    }
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
            }

            delay(1000)

            try {
                val trxData = TrxTransactionsApi.trxTransactionsService.makeRequest(address.addressEntity.address)
                for (transaction in trxData) {
                    val contract = transaction.raw_data.contract[0]
                    if (contract.type == "TransferContract") {
                        transferTrx(transaction, "TRX", address.addressEntity.address)
                    } else if (contract.type == "TriggerSmartContract") {
                        triggerSmartContract(transaction, "TRX", address.addressEntity.address)
                    }
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
            }

            delay(1000)

            if (centralAddress != null) {
                // Запрос к API на получение TRX транзакций.
                try {
                    val trxData = TrxTransactionsApi.trxTransactionsService.makeRequest(centralAddress.address)
                    for (transaction in trxData) {
                        val contract = transaction.raw_data.contract[0]
                        if (contract.type == "TransferContract") {
                            transferCentralTrx(centralAddress.address, transaction, "TRX")
                        }
                    }
                } catch (e: Exception) {
                    Sentry.captureException(e)
                }
            }

            delay(1000)
        }
    }

    suspend fun transferUsdt(transaction: Trc20TransactionsDataResponse, tokenName: String, address: String) = coroutineScope {
        if (transaction.type != "Transfer") return@coroutineScope

        val senderAddressEntity = addressRepo.getAddressEntityByAddress(transaction.from)
        val receiverAddressEntity = addressRepo.getAddressEntityByAddress(transaction.to)

        val (addressEntity, isSender) = when (address) {
            senderAddressEntity?.address -> Pair(senderAddressEntity, true)
            receiverAddressEntity?.address -> Pair(receiverAddressEntity, false)
            else -> return@coroutineScope
        }

        val amount = BigInteger(transaction.value)

        try {
             transactionsRepo.insertNewTransaction(
                TransactionEntity(
                    txId = transaction.transaction_id,
                    senderAddressId = senderAddressEntity?.addressId,
                    receiverAddressId = receiverAddressEntity?.addressId,
                    senderAddress = transaction.from,
                    receiverAddress = transaction.to,
                    walletId = addressEntity.walletId,
                    tokenName = tokenName, // TODO: Плоховато..
                    amount = amount,
                    timestamp = transaction.block_timestamp,
                    status = "Success",
                    type = assignTransactionType(idSend = senderAddressEntity?.addressId, idReceive = receiverAddressEntity?.addressId)
                )
            )
        } catch (_: SQLiteConstraintException) {
            return@coroutineScope
        }

        if (senderAddressEntity != null) {
            val balance = tron.addressUtilities.getUsdtBalance(senderAddressEntity.address)
            tokenRepo.updateTronBalanceViaId(balance, senderAddressEntity.addressId!!, tokenName)
        }

        if (receiverAddressEntity != null) {
            val blockTime = Instant.ofEpochMilli(transaction.block_timestamp)
            val now = Instant.now()

            val within10Minutes = Duration.between(blockTime, now).abs().toMinutes() <= 10

            if (within10Minutes) {
                runCatching {
                    val userId = profileRepo.getProfileUserId()
                    amlClient.getAmlFromTransactionId(
                        userId = userId,
                        address = transaction.from,
                        tx = transaction.transaction_id,
                        tokenName = tokenName
                    )
                }.onFailure { ex ->
                    Sentry.captureException(ex)
                }
            }

            val balance = tron.addressUtilities.getUsdtBalance(receiverAddressEntity.address)
            tokenRepo.updateTronBalanceViaId(balance, receiverAddressEntity.addressId!!, tokenName)
        }

        val transactionExists = pendingTransactionRepo.pendingTransactionIsExistsByTxId(transaction.transaction_id)
        if (transactionExists) {
            pendingTransactionRepo.deletePendingTransactionByTxId(transaction.transaction_id)
        }

        if (isSender) {
            notificationFunction("\uD83D\uDCB8 Отправлено: ${amount.toTokenAmount()} USDT", "На ${transaction.to.take(6)}...${transaction.to.takeLast(4)}")
        } else {
            notificationFunction("\uD83D\uDCB0 Получено: ${amount.toTokenAmount()} USDT", "От ${transaction.from.take(6)}...${transaction.from.takeLast(4)}")
        }
    }

    suspend fun transferTrx(transaction: TrxTransactionDataResponse, tokenName: String, address: String) = coroutineScope {
        if (transactionsRepo.transactionExistsViaTxid(transaction.txID) > 2) return@coroutineScope
        val contract = transaction.raw_data.contract[0]
        if (contract.parameter.value.to_address == null || contract.parameter.value.amount == null)
            return@coroutineScope

        val ownerAddress = tron
            .addressUtilities
            .hexToBase58CheckAddress(contract.parameter.value.owner_address)

        val toAddress = tron
            .addressUtilities
            .hexToBase58CheckAddress(contract.parameter.value.to_address)

        val senderAddressEntity = addressRepo.getAddressEntityByAddress(ownerAddress)
        val receiverAddressEntity = addressRepo.getAddressEntityByAddress(toAddress)

        val (addressEntity, isSender) = when (address) {
            senderAddressEntity?.address -> Pair(senderAddressEntity, true)
            receiverAddressEntity?.address -> Pair(receiverAddressEntity, false)
            else -> return@coroutineScope
        }
        val typeValue: Int = when (contract.type) {
            "TransferContract" -> {
                assignTransactionType(idSend = senderAddressEntity?.addressId, idReceive = receiverAddressEntity?.addressId)
            }
            "TriggerSmartContract" -> {
                TransactionType.TRIGGER_SMART_CONTRACT.index
            }
            else -> return@coroutineScope
        }

        try {
            transactionsRepo.insertNewTransaction(
                TransactionEntity(
                    txId = transaction.txID,
                    senderAddressId = senderAddressEntity?.addressId,
                    receiverAddressId = receiverAddressEntity?.addressId,
                    senderAddress = ownerAddress,
                    receiverAddress = toAddress,
                    walletId = addressEntity.walletId,
                    tokenName = tokenName, // TODO: Плоховато..
                    amount = BigInteger.valueOf(contract.parameter.value.amount),
                    timestamp = transaction.block_timestamp,
                    status = "Success",
                    type = typeValue
                )
            )
        } catch (_: SQLiteConstraintException) {
            return@coroutineScope
        }

        if (senderAddressEntity != null) {
            val balance = tron.addressUtilities.getTrxBalance(senderAddressEntity.address)
            tokenRepo.updateTronBalanceViaId(balance, senderAddressEntity.addressId!!, tokenName)
        }

        if (receiverAddressEntity != null) {
            val blockTime = Instant.ofEpochMilli(transaction.block_timestamp)
            val now = Instant.now()

            val within10Minutes = Duration.between(blockTime, now).abs().toMinutes() <= 10
            if (within10Minutes && contract.parameter.value.amount > 1000000) {
                runCatching {
                    val userId = profileRepo.getProfileUserId()
                    amlClient.getAmlFromTransactionId(
                        userId = userId,
                        address = ownerAddress,
                        tx = transaction.txID,
                        tokenName = tokenName
                    )
                }.onFailure { ex ->
                    Sentry.captureException(ex)
                }
            }

            val balance = tron.addressUtilities.getTrxBalance(receiverAddressEntity.address)
            tokenRepo.updateTronBalanceViaId(balance, receiverAddressEntity.addressId!!, tokenName)
        }

        val transactionExists = pendingTransactionRepo.pendingTransactionIsExistsByTxId(transaction.txID)
        if (transactionExists) {
            pendingTransactionRepo.deletePendingTransactionByTxId(transaction.txID)
        }

        if (typeValue != TransactionType.TRIGGER_SMART_CONTRACT.index) {
            if (isSender) {
                notificationFunction("\uD83D\uDCB8 Отправлено: ${contract.parameter.value.amount.toBigInteger().toTokenAmount()} TRX", "На ${toAddress.take(6)}...${toAddress.takeLast(4)}")
            } else {
                notificationFunction("\uD83D\uDCB0 Получено: ${contract.parameter.value.amount.toBigInteger().toTokenAmount()} TRX", "От ${ownerAddress.take(6)}...${ownerAddress.takeLast(4)}")
            }
        }
    }

    suspend fun triggerSmartContract(transaction: TrxTransactionDataResponse, tokenName: String, address: String) = coroutineScope {
        if (transactionsRepo.transactionExistsViaTxid(transaction.txID) > 2) return@coroutineScope
        val contract = transaction.raw_data.contract[0]

        val ownerAddress = tron
            .addressUtilities
            .hexToBase58CheckAddress(contract.parameter.value.owner_address)

        val senderAddressEntity = addressRepo.getAddressEntityByAddress(ownerAddress)

        val (addressEntity) = when (address) {
            senderAddressEntity?.address -> Pair(senderAddressEntity, true)
            else -> return@coroutineScope
        }

        try {
            transactionsRepo.insertNewTransaction(
                TransactionEntity(
                    txId = transaction.txID,
                    senderAddressId = senderAddressEntity.addressId,
                    receiverAddressId = null,
                    senderAddress = ownerAddress,
                    receiverAddress = "",
                    walletId = addressEntity.walletId,
                    tokenName = tokenName,
                    amount = BigInteger.ZERO,
                    timestamp = transaction.block_timestamp,
                    status = "Success",
                    type = TransactionType.TRIGGER_SMART_CONTRACT.index
                )
            )
        } catch (_: SQLiteConstraintException) {
            return@coroutineScope
        }

        val balance = tron.addressUtilities.getTrxBalance(senderAddressEntity.address)
        tokenRepo.updateTronBalanceViaId(balance, senderAddressEntity.addressId!!, tokenName)
    }

    suspend fun transferCentralTrx(address: String, transaction: TrxTransactionDataResponse, tokenName: String) = coroutineScope {
        if (transactionsRepo.transactionExistsViaTxid(transaction.txID) == 1) return@coroutineScope
        val contract = transaction.raw_data.contract[0]
        if (contract.parameter.value.to_address == null || contract.parameter.value.amount == null)
            return@coroutineScope

        val ownerAddress = tron
            .addressUtilities
            .hexToBase58CheckAddress(contract.parameter.value.owner_address)

        val toAddress = tron
            .addressUtilities
            .hexToBase58CheckAddress(contract.parameter.value.to_address)

        val (senderAddressEntity, receiverAddressEntity) = listOf(
            async { addressRepo.getAddressEntityByAddress(ownerAddress) },
            async { addressRepo.getAddressEntityByAddress(toAddress) }
        ).awaitAll()

        val senderAddressId = senderAddressEntity?.addressId
        val receiverAddressId = receiverAddressEntity?.addressId

        try {
            transactionsRepo.insertNewTransaction(
                TransactionEntity(
                    txId = transaction.txID,
                    senderAddressId = senderAddressId,
                    receiverAddressId = receiverAddressId,
                    senderAddress = ownerAddress,
                    receiverAddress = toAddress,
                    walletId = 0,
                    tokenName = tokenName,
                    amount = BigInteger.valueOf(contract.parameter.value.amount),
                    timestamp = transaction.block_timestamp,
                    status = "Success",
                    type = assignTransactionType(idSend = senderAddressId, idReceive = receiverAddressId, isCentralAddress = true)
                )
            )
        } catch (_: SQLiteConstraintException) {
            return@coroutineScope
        }

        val centralAddress = centralAddressRepo.getCentralAddress()
        val balance = tron.addressUtilities.getTrxBalance(centralAddress!!.address)

        if (address == ownerAddress) {
            centralAddressRepo.updateTrxBalance(balance)
            notificationFunction("\uD83D\uDCB8 Отправлено: ${contract.parameter.value.amount.toBigInteger().toTokenAmount()} TRX", "На ${toAddress.take(6)}...${toAddress.takeLast(4)}")
        } else {
            centralAddressRepo.updateTrxBalance(balance)
            notificationFunction("\uD83D\uDCB0 Получено: ${contract.parameter.value.amount.toBigInteger().toTokenAmount()} TRX", "От ${ownerAddress.take(6)}...${ownerAddress.takeLast(4)}")
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
                        amount = 1_000
                    )
                }
            }
        }
    }
}
