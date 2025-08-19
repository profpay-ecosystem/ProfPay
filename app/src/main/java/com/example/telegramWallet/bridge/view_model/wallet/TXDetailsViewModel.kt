package com.example.telegramWallet.bridge.view_model.wallet

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.AppConstants
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.ProfPayServerGrpcClient
import com.example.telegramWallet.backend.http.aml.DownloadAmlPdfApi
import com.example.telegramWallet.backend.http.aml.DownloadAmlPdfRequestCallback
import com.example.telegramWallet.bridge.view_model.dto.transfer.TransferResult
import com.example.telegramWallet.data.database.entities.wallet.AddressEntity
import com.example.telegramWallet.data.database.entities.wallet.PendingTransactionEntity
import com.example.telegramWallet.data.database.models.HasTronCredentials
import com.example.telegramWallet.data.database.entities.wallet.TransactionEntity
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.CentralAddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.example.telegramWallet.data.database.repositories.wallet.PendingTransactionRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.database.repositories.wallet.WalletProfileRepo
import com.example.telegramWallet.data.flow_db.repo.AmlResult
import com.example.telegramWallet.data.flow_db.repo.EstimateCommissionResult
import com.example.telegramWallet.data.flow_db.repo.TXDetailsRepo
import com.example.telegramWallet.data.flow_db.repo.TransactionStatusResult
import com.example.telegramWallet.data.utils.toBigInteger
import com.example.telegramWallet.data.utils.toByteString
import com.example.telegramWallet.data.utils.toSunAmount
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.exceptions.payments.GrpcClientErrorSendTransactionExcpetion
import com.example.telegramWallet.exceptions.payments.GrpcServerErrorSendTransactionExcpetion
import com.example.telegramWallet.tron.EstimateBandwidthData
import com.example.telegramWallet.tron.EstimateEnergyData
import com.example.telegramWallet.tron.SignedTransactionData
import com.example.telegramWallet.tron.Tron
import com.google.protobuf.ByteString
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.protobuf.transfer.TransferProto
import org.example.protobuf.transfer.TransferProto.TransactionData
import org.example.protobuf.transfer.TransferProto.TransferNetwork
import org.example.protobuf.transfer.TransferProto.TransferToken
import org.server.protobuf.aml.AmlProto.AmlPaymentRequest
import org.server.protobuf.aml.AmlProto.AmlTransactionDetails
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class TXDetailsViewModel @Inject constructor(
    private val txDetailsRepo: TXDetailsRepo,
    private val walletRepo: WalletProfileRepo,
    private val profileRepo: ProfileRepo,
    val transactionsRepo: TransactionsRepo,
    val addressRepo: AddressRepo,
    private val tokenRepo: TokenRepo,
    val exchangeRatesRepo: ExchangeRatesRepo,
    val tron: Tron,
    val centralAddressRepo: CentralAddressRepo,
    grpcClientFactory: GrpcClientFactory
) : ViewModel() {
    private val _state = MutableStateFlow<AmlResult>(AmlResult.Empty)
    val state: StateFlow<AmlResult> = _state.asStateFlow()

    private val _isActivated = MutableStateFlow<Boolean>(false)
    val isActivated: StateFlow<Boolean> = _isActivated

    private val _amlFeeResult = MutableStateFlow<ByteString?>(null)
    val amlFeeResult: StateFlow<ByteString?> = _amlFeeResult.asStateFlow()

    private val profPayServerGrpcClient: ProfPayServerGrpcClient = grpcClientFactory.getGrpcClient(
        ProfPayServerGrpcClient::class.java,
        AppConstants.Network.GRPC_ENDPOINT,
        AppConstants.Network.GRPC_PORT
    )

    fun getAmlFeeResult() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                profPayServerGrpcClient.getServerParameters()
            }

            result.fold(
                onSuccess = { _amlFeeResult.emit(it.amlFee) },
                onFailure = { Sentry.captureException(it) }
            )
        }
    }

    init {
        getAmlFeeResult()
    }

    fun checkActivation(address: String) {
        viewModelScope.launch {
            _isActivated.value = withContext(Dispatchers.IO) {
                tron.addressUtilities.isAddressActivated(address)
            }
        }
    }

    suspend fun processedAmlReport(
        walletId: Long,
        receiverAddress: String,
        txId: String
    ): Pair<Boolean, String> {
        val generalAddress = addressRepo.getGeneralAddressEntityByWalletId(walletId)
        val balance = tron.addressUtilities.getTrxBalance(generalAddress.address)
        val userId = profileRepo.getProfileUserId()

        val serverParameters = profPayServerGrpcClient.getServerParameters().fold(
            onSuccess = { it },
            onFailure = {
                Sentry.captureException(it)
                return Pair(false, "Сервер недоступен")
            }
        )

        val amlFeeValue = serverParameters.amlFee
        val trxFeeAddress = serverParameters.trxFeeAddress

        if (balance.toTokenAmount() < amlFeeValue.toBigInteger().toTokenAmount()) {
            return Pair(false, "Недостаточно средств на балансе.")
        }

        val signedTxnBytes = tron.transactions.getSignedTrxTransaction(
            fromAddress = generalAddress.address,
            toAddress = trxFeeAddress,
            privateKey = generalAddress.privateKey,
            amount = amlFeeValue.toBigInteger().toTokenAmount().toSunAmount()
        )

        val estimateBandwidth = tron.transactions.estimateBandwidth(
            fromAddress = generalAddress.address,
            toAddress = trxFeeAddress,
            privateKey = generalAddress.privateKey,
            amount = amlFeeValue.toBigInteger().toTokenAmount().toSunAmount()
        )

        txDetailsRepo.processAmlPayment(
            AmlPaymentRequest.newBuilder()
                .setUserId(userId)
                .setTx(txId)
                .setAddress(receiverAddress)
                .setTransaction(
                    AmlTransactionDetails.newBuilder()
                        .setAddress(generalAddress.address)
                        .setBandwidthRequired(estimateBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytes.signedTxn)
                        .build()
                )
                .build()
        )
        return Pair(true, "Успешное действие, ожидайте уведомление.")
    }

    suspend fun getAmlFromTransactionId(address: String, tx: String, tokenName: String) {
        val data =
            txDetailsRepo.getAmlFromTransactionId(address = address, tx = tx, tokenName = tokenName)
        txDetailsRepo.aml.collect { aml ->
            _state.value = aml
        }
        return data
    }

    fun getTransactionLiveDataById(transactionId: Long): LiveData<TransactionEntity> {
        return liveData(Dispatchers.IO) {
            emitSource(transactionsRepo.getTransactionLiveDataById(transactionId))
        }
    }

    suspend fun isGeneralAddress(address: String): Boolean {
        return addressRepo.isGeneralAddress(address)
    }

    suspend fun getWalletNameById(walletId: Long): String? {
        return walletRepo.getWalletNameById(walletId)
    }

    suspend fun downloadPdfFile(txId: String, destinationFile: File) {
        val userId = profileRepo.getProfileUserId()
        DownloadAmlPdfApi.downloadAmlPdfService.makeRequest(
            object : DownloadAmlPdfRequestCallback {
                @RequiresApi(Build.VERSION_CODES.S)
                override fun onSuccess(inputStream: InputStream?) {
                    val outputStream = FileOutputStream(destinationFile)
                    inputStream?.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.d(
                        "FileDownload",
                        "File saved successfully: ${destinationFile.absolutePath}"
                    )
                }

                override fun onFailure(error: String) {
                    Sentry.captureException(Exception(error))
                }
            }, userId = userId, txId = txId
        )
    }
}