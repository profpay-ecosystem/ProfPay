package com.profpay.wallet.data.repository.flow

import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.CryptoAddressGrpcClient
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import com.profpay.wallet.tron.AddressGenerateFromSeedPhr
import com.profpay.wallet.tron.AddressGenerateResult
import com.profpay.wallet.tron.Tron
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AddressAndMnemonicRepo {
    suspend fun generateNewAddressAndMnemonic()

    val addressAndMnemonic: Flow<AddressGenerateResult>
    val addressFromMnemonic: Flow<RecoveryResult>

    suspend fun generateAddressFromMnemonic(mnemonic: String)

    suspend fun recoveryWallet(
        address: String,
        mnemonic: String,
    )

    suspend fun clearAddressFromMnemonic()
}

class AddressAndMnemonicRepoImpl
    @Inject
    constructor(
        val profileRepo: ProfileRepo,
        val addressRepo: AddressRepo,
        private val tron: Tron,
        grpcClientFactory: GrpcClientFactory,
        @IoDispatcher private val dispatcher: CoroutineDispatcher,
    ) : AddressAndMnemonicRepo {
        private val cryptoAddressGrpcClient: CryptoAddressGrpcClient =
            grpcClientFactory.getGrpcClient(
                CryptoAddressGrpcClient::class.java,
                AppConstants.Network.GRPC_ENDPOINT,
                AppConstants.Network.GRPC_PORT,
            )
        private val _addressAndMnemonic = MutableSharedFlow<AddressGenerateResult>(replay = 1)

        // Получение данных нового кошелька
        override val addressAndMnemonic: Flow<AddressGenerateResult> =
            _addressAndMnemonic.asSharedFlow()

        // Триггер на обновление данных нового кошелька
        override suspend fun generateNewAddressAndMnemonic() {
            withContext(dispatcher) {
                val addressAndMnemonic = tron.addressUtilities.generateAddressAndMnemonic()
                _addressAndMnemonic.emit(addressAndMnemonic)
            }
        }

        private val _addressFromMnemonic = MutableSharedFlow<RecoveryResult>(replay = 1)

        // Получение данных восстановленного кошелька по мнемонике(сид-фразе)
        override val addressFromMnemonic: Flow<RecoveryResult> =
            _addressFromMnemonic.asSharedFlow()

        override suspend fun clearAddressFromMnemonic() {
            _addressFromMnemonic.emit(RecoveryResult.Empty)
        }

        // Триггер на обновление данных восстановленного кошелька по мнемонике(сид-фразе)
        override suspend fun generateAddressFromMnemonic(mnemonic: String) {
            withContext(dispatcher) {
                try {
                    val generalAddress = tron.addressUtilities.getGeneralAddressBySeedPhrase(mnemonic)

                    val byAddressOrNull = addressRepo.getAddressEntityByAddress(generalAddress)
                    if (byAddressOrNull == null) {
                        recoveryWallet(generalAddress, mnemonic)
                    } else {
                        _addressFromMnemonic.emit(RecoveryResult.RepeatingMnemonic)
                    }
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    _addressFromMnemonic.emit(RecoveryResult.InvalidMnemonic)
                }
            }
        }

        override suspend fun recoveryWallet(
            gAddress: String,
            mnemonic: String,
        ) {
            try {
                val result = cryptoAddressGrpcClient.getWalletData(address = gAddress)

                result.fold(
                    onSuccess = { walletData ->
                        val recoveryResult =
                            try {
                                val addressGenerateFromSeedPhr =
                                    tron.addressUtilities.recoveryKeysAndAddressBySeedPhrase(
                                        mnemonic,
                                        walletData.derivedIndicesList,
                                    )
                                RecoveryResult.Success(
                                    address = addressGenerateFromSeedPhr,
                                    accountWasFound = true,
                                    userId = walletData.userId,
                                )
                            } catch (_: Exception) {
                                RecoveryResult.InvalidMnemonic
                            }

                        _addressFromMnemonic.emit(recoveryResult)
                    },
                    onFailure = { error ->
                        if (error.message == "INTERNAL: Address not found in database") {
                            val address =
                                try {
                                    tron.addressUtilities.generateKeysAndAddressBySeedPhrase(mnemonic)
                                } catch (_: Exception) {
                                    _addressFromMnemonic.emit(RecoveryResult.InvalidMnemonic)
                                    return
                                }
                            _addressFromMnemonic.emit(RecoveryResult.Success(address = address, accountWasFound = false))
                        } else {
                            Sentry.captureException(error)
                            _addressFromMnemonic.emit(RecoveryResult.Error(RuntimeException(error)))
                        }
                    },
                )
            } catch (e: Exception) {
                Sentry.captureException(e)
                throw RuntimeException("Failed to fetch smart contracts", e)
            }
        }
    }

sealed class RecoveryResult {
    data class Success(
        val address: AddressGenerateFromSeedPhr,
        val accountWasFound: Boolean,
        val userId: Long? = null,
    ) : RecoveryResult()

    data object InvalidMnemonic : RecoveryResult()

    data object RepeatingMnemonic : RecoveryResult()

    data object AddressNotFound : RecoveryResult()

    data class Error(
        val throwable: Throwable,
    ) : RecoveryResult()

    object Empty : RecoveryResult()
}
