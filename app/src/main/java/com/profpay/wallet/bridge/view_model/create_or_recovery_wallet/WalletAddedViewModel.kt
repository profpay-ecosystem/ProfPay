package com.profpay.wallet.bridge.view_model.create_or_recovery_wallet

import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.profpay.wallet.AppConstants
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.backend.grpc.CryptoAddressGrpcClient
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.backend.grpc.UserGrpcClient
import com.profpay.wallet.bridge.view_model.dto.BlockchainName
import com.profpay.wallet.data.database.entities.ProfileEntity
import com.profpay.wallet.data.database.entities.wallet.AddressEntity
import com.profpay.wallet.data.database.entities.wallet.TokenEntity
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.TokenRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import com.profpay.wallet.security.KeystoreCryptoManager
import com.profpay.wallet.security.KeystoreEncryptionUtils
import com.profpay.wallet.tron.AddressesWithKeysForM
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class WalletAddedViewModel
    @Inject
    constructor(
        private val walletProfileRepo: WalletProfileRepo,
        private val addressRepo: AddressRepo,
        private val tokenRepo: TokenRepo,
        private val profileRepo: ProfileRepo,
        private val keystoreCryptoManager: KeystoreCryptoManager,
        grpcClientFactory: GrpcClientFactory,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val keystore = KeystoreEncryptionUtils()
        private val cryptoAddressGrpcClient: CryptoAddressGrpcClient =
            grpcClientFactory.getGrpcClient(
                CryptoAddressGrpcClient::class.java,
                AppConstants.Network.GRPC_ENDPOINT,
                AppConstants.Network.GRPC_PORT,
            )

        private val userGrpcClient: UserGrpcClient =
            grpcClientFactory.getGrpcClient(
                UserGrpcClient::class.java,
                AppConstants.Network.GRPC_ENDPOINT,
                AppConstants.Network.GRPC_PORT
            )

        // Получение alias главного адреса
        fun getWalletAlias(addressesWithKeys: AddressesWithKeysForM): String? {
            val mainAddress = addressesWithKeys.addresses
                .firstOrNull { it.indexDerivationSot == 0 }

            return mainAddress?.address // используем адрес главного кошелька как alias
        }

        suspend fun insertNewCryptoAddresses(addressesWithKeysForM: AddressesWithKeysForM) {
            val walletAlias = getWalletAlias(addressesWithKeysForM) ?: throw IllegalStateException("Главный адрес кошелька не найден")

            keystoreCryptoManager.createAesKey(walletAlias)
            val (iv, cipherText) = keystoreCryptoManager.encrypt(walletAlias, addressesWithKeysForM.entropy)

            val walletId =
                withContext(ioDispatcher) {
                    val number = walletProfileRepo.getCountRecords() + 1
                    walletProfileRepo.insertNewWalletProfileEntity(name = "Wallet $number", iv = iv, cipherText = cipherText)
                }
            withContext(ioDispatcher) {
                try {
                    BlockchainName.entries.map { blockchain ->
                        // Проходим по списку блокчейнов
                        addressesWithKeysForM.addresses.map { currentAddress ->
                            // Проходим по списку адресов
                            val addressId =
                                addressRepo.insertNewAddress(
                                    AddressEntity(
                                        walletId = walletId,
                                        blockchainName = blockchain.blockchainName,
                                        address = currentAddress.address,
                                        publicKey = currentAddress.publicKey,
                                        isGeneralAddress = currentAddress.indexDerivationSot == 0,
                                        sotIndex = currentAddress.indexSot,
                                        sotDerivationIndex = currentAddress.indexDerivationSot,
                                    ),
                                )
                            // проходим по списку токенов текущего блокчейна
                            blockchain.tokens.forEach { token ->
                                tokenRepo.insertNewTokenEntity(
                                    TokenEntity(
                                        addressId = addressId,
                                        tokenName = token.tokenName,
                                        balance = BigInteger.ZERO,
                                    ),
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("exception_insert", e.message!!)
                    Sentry.captureException(e)
                }
            }
        }

        // Добавление нового кошелька в бд
        suspend fun createCryptoAddresses(addressesWithKeysForM: AddressesWithKeysForM) {
            try {
                // TODO: Обдумать.
                val result =
                    cryptoAddressGrpcClient.addCryptoAddress(
                        appId = profileRepo.getProfileAppId(),
                        address = addressesWithKeysForM.addresses[0].address,
                        pubKey = addressesWithKeysForM.addresses[0].publicKey,
                        derivedIndices = addressesWithKeysForM.derivedIndices,
                    )
                result.fold(
                    onSuccess = { response ->
                        Log.d("addCryptoAddress", response.toString())
                    },
                    onFailure = { exception ->
                        Sentry.captureException(exception)
                        Log.e("gRPC ERROR", "Error during gRPC call: ${exception.message}")
                    },
                )
            } catch (e: Exception) {
                Sentry.captureException(e)
                Log.e("gRPC Exception", "Error during gRPC call: ${e.message}")
            }
        }

        suspend fun registerUserAccount(
            deviceToken: String,
            sharedPref: SharedPreferences,
        ): Boolean =
            withContext(ioDispatcher) {
                try {
                    val uuidString =
                        java.util.UUID
                            .randomUUID()
                            .toString()
                    val result = userGrpcClient.registerUser(uuidString, deviceToken)
                    result.fold(
                        onSuccess = { response ->
                            profileRepo.insertNewProfile(
                                ProfileEntity(
                                    userId = response.userId,
                                    appId = uuidString,
                                    deviceToken = deviceToken,
                                ),
                            )
                            sharedPref.edit(commit = true) {
                                val encryptedAccessTokenBytes = keystore.encrypt(response.accessToken.toByteArray(Charsets.UTF_8))
                                val encryptedRefreshTokenBytes = keystore.encrypt(response.refreshToken.toByteArray(Charsets.UTF_8))

                                putString(PrefKeys.JWT_ACCESS_TOKEN, Base64.encodeToString(encryptedAccessTokenBytes, Base64.NO_WRAP))
                                putString(PrefKeys.JWT_REFRESH_TOKEN, Base64.encodeToString(encryptedRefreshTokenBytes, Base64.NO_WRAP))
                            }
                            true
                        },
                        onFailure = { exception ->
                            Sentry.captureException(exception)
                            Log.e("gRPC ERROR", "Error during gRPC call: ${exception.message}")
                            false
                        },
                    )
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    Log.e("gRPC ERROR", "Unexpected error: ${e.message}")
                    false
                }
            }

        suspend fun registerUserDevice(
            userId: Long,
            deviceToken: String,
            sharedPref: SharedPreferences,
        ) = withContext(ioDispatcher) {
            try {
                val uuidString =
                    java.util.UUID
                        .randomUUID()
                        .toString()
                val result = userGrpcClient.registerUserDevice(userId, uuidString, deviceToken)
                result.fold(
                    onSuccess = { response ->
                        profileRepo.insertNewProfile(
                            ProfileEntity(
                                userId = userId,
                                appId = uuidString,
                                deviceToken = deviceToken,
                            ),
                        )
                        sharedPref.edit(commit = true) {
                            val encryptedAccessTokenBytes = keystore.encrypt(response.accessToken.toByteArray(Charsets.UTF_8))
                            val encryptedRefreshTokenBytes = keystore.encrypt(response.refreshToken.toByteArray(Charsets.UTF_8))

                            putString(PrefKeys.JWT_ACCESS_TOKEN, Base64.encodeToString(encryptedAccessTokenBytes, Base64.NO_WRAP))
                            putString(PrefKeys.JWT_REFRESH_TOKEN, Base64.encodeToString(encryptedRefreshTokenBytes, Base64.NO_WRAP))
                        }
                        true
                    },
                    onFailure = { exception ->
                        Sentry.captureException(exception)
                        Log.e("gRPC ERROR", "Error during gRPC call: ${exception.message}")
                        false
                    },
                )
            } catch (e: Exception) {
                Sentry.captureException(e)
                Log.e("gRPC ERROR", "Error during gRPC call: ${e.message}")
                false
            }
        }
    }
