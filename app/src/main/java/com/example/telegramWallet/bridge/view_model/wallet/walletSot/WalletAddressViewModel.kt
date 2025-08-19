package com.example.telegramWallet.bridge.view_model.wallet.walletSot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.AppConstants
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.ProfPayServerGrpcClient
import com.example.telegramWallet.bridge.view_model.dto.transfer.TransferResult
import com.example.telegramWallet.data.database.entities.wallet.PendingTransactionEntity
import com.example.telegramWallet.data.database.models.AddressWithTokens
import com.example.telegramWallet.data.database.models.HasTronCredentials
import com.example.telegramWallet.data.database.models.TokenWithPendingTransactions
import com.example.telegramWallet.data.database.models.TransactionModel
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.CentralAddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.PendingTransactionRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.flow_db.repo.EstimateCommissionResult
import com.example.telegramWallet.data.flow_db.repo.TransactionStatusResult
import com.example.telegramWallet.data.flow_db.repo.WalletAddressRepo
import com.example.telegramWallet.data.services.TransactionProcessorService
import com.example.telegramWallet.data.utils.toByteString
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.exceptions.payments.GrpcClientErrorSendTransactionExcpetion
import com.example.telegramWallet.exceptions.payments.GrpcServerErrorSendTransactionExcpetion
import com.example.telegramWallet.tron.EstimateBandwidthData
import com.example.telegramWallet.tron.EstimateEnergyData
import com.example.telegramWallet.tron.SignedTransactionData
import com.example.telegramWallet.tron.Tron
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
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class WalletAddressViewModel @Inject constructor(
    private val walletAddressRepo: WalletAddressRepo,
    val addressRepo: AddressRepo,
    val transactionsRepo: TransactionsRepo,
    private val centralAddressRepo: CentralAddressRepo,
    private val profileRepo: ProfileRepo,
    private val tokenRepo: TokenRepo,
    private val pendingTransactionRepo: PendingTransactionRepo,
    val tron: Tron,
    private val transactionProcessorService: TransactionProcessorService,
    grpcClientFactory: GrpcClientFactory
) : ViewModel() {
    private val profPayServerGrpcClient: ProfPayServerGrpcClient = grpcClientFactory.getGrpcClient(
        ProfPayServerGrpcClient::class.java,
        AppConstants.Network.GRPC_ENDPOINT,
        AppConstants.Network.GRPC_PORT
    )

    private val _isActivated = MutableStateFlow<Boolean>(false)
    val isActivated: StateFlow<Boolean> = _isActivated

    private val _stateCommission =
        MutableStateFlow<EstimateCommissionResult>(EstimateCommissionResult.Empty)
    val stateCommission: StateFlow<EstimateCommissionResult> = _stateCommission.asStateFlow()

    fun checkActivation(address: String) {
        viewModelScope.launch {
            _isActivated.value = withContext(Dispatchers.IO) {
                tron.addressUtilities.isAddressActivated(address)
            }
        }
    }

    suspend fun estimateCommission(address: String, bandwidth: Long, energy: Long) {
        walletAddressRepo.estimateCommission(address, bandwidth = bandwidth, energy = energy)
        walletAddressRepo.estimateCommission.collect { comission ->
            _stateCommission.value = comission
        }
    }

    fun getAddressWithTokensByAddressLD(address: String): LiveData<AddressWithTokens>{
        return liveData(Dispatchers.IO) {
            emitSource(addressRepo.getAddressWithTokensByAddressLD(address))
        }
    }

    suspend fun isGeneralAddress(address: String): Boolean {
        return addressRepo.isGeneralAddress(address)
    }

    fun getTransactionsByAddressAndTokenLD(
        walletId: Long,
        address: String,
        tokenName: String,
        isSender: Boolean,
        isCentralAddress: Boolean
    ): LiveData<List<TransactionModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(transactionsRepo.getTransactionsByAddressAndTokenLD(
                walletId = walletId,
                address = address,
                tokenName = tokenName,
                isSender = isSender,
                isCentralAddress = isCentralAddress
            ))
        }
    }

    suspend fun getListTransactionToTimestamp(listTransactions: List<TransactionModel>): List<List<TransactionModel?>> {
        var listListTransactions: List<List<TransactionModel>> = listOf(emptyList())

        withContext(Dispatchers.IO) {
            if (listTransactions.isEmpty()) return@withContext
            listListTransactions = listTransactions.sortedByDescending { it.timestamp }
                .groupBy { it.transactionDate }.values.toList()
        }
        return listListTransactions
    }

    suspend fun rejectTransaction(
        toAddress: String,
        addressWithTokens: AddressWithTokens,
        amount: BigInteger,
        commission: BigInteger,
        tokenEntity: TokenWithPendingTransactions?
    ): TransferResult {
        return transactionProcessorService.sendTransaction(
            sender = addressWithTokens.addressEntity.address,
            receiver = toAddress,
            amount = amount,
            commission = commission,
            tokenEntity = tokenEntity
        )
    }

    suspend fun acceptTransaction(
        addressWithTokens: AddressWithTokens,
        commission: BigInteger,
        walletId: Long,
        tokenEntity: TokenWithPendingTransactions?,
        amount: BigInteger
    ): TransferResult {
        val generalAddress = addressRepo.getGeneralAddressByWalletId(walletId)
        return transactionProcessorService.sendTransaction(
            sender = addressWithTokens.addressEntity.address,
            receiver = generalAddress,
            amount = amount,
            commission = commission,
            tokenEntity = tokenEntity
        )
    }
}