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
import com.example.telegramWallet.data.database.entities.wallet.TransactionEntity
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.CentralAddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.example.telegramWallet.data.database.repositories.wallet.PendingAmlTransactionRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.database.repositories.wallet.WalletProfileRepo
import com.example.telegramWallet.data.flow_db.repo.AmlResult
import com.example.telegramWallet.data.flow_db.repo.TXDetailsRepo
import com.example.telegramWallet.data.services.AmlProcessorService
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
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class TXDetailsViewModel
    @Inject
    constructor(
        private val txDetailsRepo: TXDetailsRepo,
        private val walletRepo: WalletProfileRepo,
        private val profileRepo: ProfileRepo,
        val transactionsRepo: TransactionsRepo,
        val addressRepo: AddressRepo,
        private val tokenRepo: TokenRepo,
        val exchangeRatesRepo: ExchangeRatesRepo,
        val tron: Tron,
        val centralAddressRepo: CentralAddressRepo,
        private val amlProcessorService: AmlProcessorService,
        private val pendingAmlTransactionRepo: PendingAmlTransactionRepo,
        grpcClientFactory: GrpcClientFactory,
    ) : ViewModel() {
        private val _state = MutableStateFlow<AmlResult>(AmlResult.Empty)
        val state: StateFlow<AmlResult> = _state.asStateFlow()

        private val _isActivated = MutableStateFlow<Boolean>(false)
        val isActivated: StateFlow<Boolean> = _isActivated

        private val _amlFeeResult = MutableStateFlow<ByteString?>(null)
        val amlFeeResult: StateFlow<ByteString?> = _amlFeeResult.asStateFlow()

        private val _amlIsPending = MutableStateFlow<Boolean>(false)
        val amlIsPending: StateFlow<Boolean> = _amlIsPending

        private val profPayServerGrpcClient: ProfPayServerGrpcClient =
            grpcClientFactory.getGrpcClient(
                ProfPayServerGrpcClient::class.java,
                AppConstants.Network.GRPC_ENDPOINT,
                AppConstants.Network.GRPC_PORT,
            )

        fun getAmlFeeResult() {
            viewModelScope.launch {
                val result =
                    withContext(Dispatchers.IO) {
                        profPayServerGrpcClient.getServerParameters()
                    }

                result.fold(
                    onSuccess = { _amlFeeResult.emit(it.amlFee) },
                    onFailure = { Sentry.captureException(it) },
                )
            }
        }

        fun getAmlIsPendingResult(txid: String) {
            viewModelScope.launch {
                val result = pendingAmlTransactionRepo.isPendingAmlTransactionExists(txid)
                _amlIsPending.emit(result)
            }
        }

        init {
            getAmlFeeResult()
        }

        fun checkActivation(address: String) {
            viewModelScope.launch {
                _isActivated.value =
                    withContext(Dispatchers.IO) {
                        tron.addressUtilities.isAddressActivated(address)
                    }
            }
        }

        suspend fun processedAmlReport(
            receiverAddress: String,
            txId: String,
        ): Pair<Boolean, String> = amlProcessorService.processAmlReport(address = receiverAddress, txid = txId)

        suspend fun getAmlFromTransactionId(
            address: String,
            tx: String,
            tokenName: String,
        ) {
            val data =
                txDetailsRepo.getAmlFromTransactionId(address = address, tx = tx, tokenName = tokenName)
            txDetailsRepo.aml.collect { aml ->
                _state.value = aml
            }
            return data
        }

        fun getTransactionLiveDataById(transactionId: Long): LiveData<TransactionEntity> =
            liveData(Dispatchers.IO) {
                emitSource(transactionsRepo.getTransactionLiveDataById(transactionId))
            }

        suspend fun isGeneralAddress(address: String): Boolean = addressRepo.isGeneralAddress(address)

        suspend fun getWalletNameById(walletId: Long): String? = walletRepo.getWalletNameById(walletId)

        suspend fun downloadPdfFile(
            txId: String,
            destinationFile: File,
        ) {
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
                            "File saved successfully: ${destinationFile.absolutePath}",
                        )
                    }

                    override fun onFailure(error: String) {
                        Sentry.captureException(Exception(error))
                    }
                },
                userId = userId,
                txId = txId,
            )
        }
    }
