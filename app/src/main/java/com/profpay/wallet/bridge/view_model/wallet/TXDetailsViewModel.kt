package com.profpay.wallet.bridge.view_model.wallet

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.protobuf.ByteString
import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.backend.grpc.ProfPayServerGrpcClient
import com.profpay.wallet.backend.http.aml.DownloadAmlPdfApi
import com.profpay.wallet.backend.http.aml.DownloadAmlPdfRequestCallback
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.profpay.wallet.data.database.repositories.wallet.PendingAmlTransactionRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import com.profpay.wallet.data.flow_db.repo.AmlResult
import com.profpay.wallet.data.flow_db.repo.TXDetailsRepo
import com.profpay.wallet.data.services.AmlProcessorService
import com.profpay.wallet.tron.Tron
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject

sealed class AmlReleaseUiEvent {
    data object Idle : AmlReleaseUiEvent()
    data class Success(val message: String) : AmlReleaseUiEvent()
    data class Error(val title: String, val message: String) : AmlReleaseUiEvent()
}

@HiltViewModel
class TXDetailsViewModel @Inject constructor(
    private val txDetailsRepo: TXDetailsRepo,
    private val walletRepo: WalletProfileRepo,
    private val profileRepo: ProfileRepo,
    val transactionsRepo: TransactionsRepo,
    val addressRepo: AddressRepo,
    val exchangeRatesRepo: ExchangeRatesRepo,
    val tron: Tron,
    val centralAddressRepo: CentralAddressRepo,
    private val amlProcessorService: AmlProcessorService,
    private val pendingAmlTransactionRepo: PendingAmlTransactionRepo,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    grpcClientFactory: GrpcClientFactory,
) : ViewModel() {
    private val _state = MutableStateFlow<AmlResult>(AmlResult.Empty)
    val state: StateFlow<AmlResult> = _state.asStateFlow()

    private val _amlFeeResult = MutableStateFlow<ByteString?>(null)
    val amlFeeResult: StateFlow<ByteString?> = _amlFeeResult.asStateFlow()

    private val _amlIsPending = MutableStateFlow(false)
    val amlIsPending: StateFlow<Boolean> = _amlIsPending

    private val _amlReleaseUiEvent = MutableStateFlow<AmlReleaseUiEvent>(AmlReleaseUiEvent.Idle)
    val amlReleaseUiEvent: StateFlow<AmlReleaseUiEvent> = _amlReleaseUiEvent.asStateFlow()

    private val _walletName = MutableStateFlow<String?>(null)
    val walletName: StateFlow<String?> = _walletName.asStateFlow()

    private val profPayServerGrpcClient: ProfPayServerGrpcClient =
        grpcClientFactory.getGrpcClient(
            ProfPayServerGrpcClient::class.java,
            AppConstants.Network.GRPC_ENDPOINT,
            AppConstants.Network.GRPC_PORT,
        )

    fun getAmlFeeResult() {
        viewModelScope.launch {
            val result =
                withContext(ioDispatcher) {
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

    fun processedAmlReport(
        receiverAddress: String,
        txId: String,
    ) = viewModelScope.launch(ioDispatcher) {
        val (status, message) = amlProcessorService.processAmlReport(address = receiverAddress, txid = txId)

        if (status) {
            _amlReleaseUiEvent.emit(AmlReleaseUiEvent.Success(message))
        } else {
            _amlReleaseUiEvent.emit(AmlReleaseUiEvent.Error("Ошибка запроса", message))
        }
    }

    fun getAmlFromTransactionId(
        address: String,
        tx: String,
        tokenName: String,
    ) = viewModelScope.launch(ioDispatcher) {
        txDetailsRepo.getAmlFromTransactionId(address = address, tx = tx, tokenName = tokenName)
        txDetailsRepo.aml.collect { aml ->
            _state.value = aml
        }
    }

    fun getTransactionLiveDataById(transactionId: Long): LiveData<TransactionEntity> =
        liveData(ioDispatcher) {
            emitSource(transactionsRepo.getTransactionFlowById(transactionId).asLiveData())
        }

    fun getWalletNameById(walletId: Long) = viewModelScope.launch(ioDispatcher) {
        val name = walletRepo.getWalletNameById(walletId)
        _walletName.emit(name)
    }

    fun downloadPdfFile(
        transactionEntity: TransactionEntity,
        context: Context,
    ) = viewModelScope.launch(ioDispatcher) {
        val userId = profileRepo.getProfileUserId()
        DownloadAmlPdfApi.downloadAmlPdfService.makeRequest(
            object : DownloadAmlPdfRequestCallback {
                @RequiresApi(Build.VERSION_CODES.S)
                override fun onSuccess(inputStream: InputStream?) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, "aml_${transactionEntity.txId}.pdf")
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            inputStream?.copyTo(outputStream)
                        }
                    }
                }

                override fun onFailure(error: String) {
                    Sentry.captureException(Exception(error))
                }
            },
            userId = userId,
            txId = transactionEntity.txId,
        )
    }
}
