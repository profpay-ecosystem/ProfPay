package com.profpay.wallet.data.repository

import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import com.profpay.wallet.AppConstants
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.backend.grpc.CryptoAddressGrpcClient
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.backend.grpc.UserGrpcClient
import com.profpay.wallet.bridge.viewmodel.dto.BlockchainName
import com.profpay.wallet.data.database.AppDatabase
import com.profpay.wallet.data.database.entities.ProfileEntity
import com.profpay.wallet.data.database.entities.wallet.AddressEntity
import com.profpay.wallet.data.database.entities.wallet.WalletProfileEntity
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.exceptions.grpc.GrpcRequestException
import com.profpay.wallet.exceptions.grpc.GrpcResponseException
import com.profpay.wallet.security.KeystoreCryptoManager
import com.profpay.wallet.security.KeystoreEncryptionUtils
import com.profpay.wallet.tron.AddressesWithKeysForM
import io.sentry.Sentry
import org.example.protobuf.address.CryptoAddressProto
import java.util.UUID
import javax.inject.Inject

interface WalletAddedRepo {
    fun getWalletAlias(addressesWithKeys: AddressesWithKeysForM): String?

    suspend fun insertNewCryptoAddresses(addressesWithKeysForM: AddressesWithKeysForM): Long

    suspend fun createCryptoAddresses(addressesWithKeysForM: AddressesWithKeysForM)

    suspend fun registerUserAccount(
        deviceToken: String,
        sharedPref: SharedPreferences,
    )

    suspend fun registerUserDevice(
        userId: Long,
        deviceToken: String,
        sharedPref: SharedPreferences,
    )
}

class WalletAddedRepoImpl
    @Inject
    constructor(
        private val profileRepo: ProfileRepo,
        private val centralAddressRepo: CentralAddressRepo,
        private val keystoreCryptoManager: KeystoreCryptoManager,
        private val database: AppDatabase,
        grpcClientFactory: GrpcClientFactory,
    ) : WalletAddedRepo {
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
                AppConstants.Network.GRPC_PORT,
            )

        // Получение alias главного адреса
        override fun getWalletAlias(addressesWithKeys: AddressesWithKeysForM): String? {
            val mainAddress =
                addressesWithKeys.addresses
                    .firstOrNull { it.indexDerivationSot == 0 }

            return mainAddress?.address // используем адрес главного кошелька как alias
        }

        override suspend fun insertNewCryptoAddresses(addressesWithKeysForM: AddressesWithKeysForM): Long {
            val walletAlias = getWalletAlias(addressesWithKeysForM) ?: throw IllegalStateException("Главный адрес кошелька не найден")

            keystoreCryptoManager.createAesKey(walletAlias)
            val (iv, cipherText) = keystoreCryptoManager.encrypt(walletAlias, addressesWithKeysForM.entropy)

            val addressList = mutableListOf<AddressEntity>()

            BlockchainName.entries.forEach { blockchain ->
                addressesWithKeysForM.addresses.forEach { currentAddress ->
                    val addressEntity =
                        AddressEntity(
                            walletId = 0, // временно 0, будет заменён в DAO
                            blockchainName = blockchain.blockchainName,
                            address = currentAddress.address,
                            publicKey = currentAddress.publicKey,
                            isGeneralAddress = currentAddress.indexDerivationSot == 0,
                            sotIndex = currentAddress.indexSot,
                            sotDerivationIndex = currentAddress.indexDerivationSot,
                        )
                    addressList.add(addressEntity)
                }
            }

            val walletId = database.insertWalletWithAddressesAndTokens(
                walletProfile =
                    WalletProfileEntity(
                        name = "",
                        iv = iv,
                        cipherText = cipherText,
                    ),
                addresses = addressList,
            )
            return walletId
        }

        override suspend fun createCryptoAddresses(addressesWithKeysForM: AddressesWithKeysForM) {
            val generalAddressData = addressesWithKeysForM.addresses.firstOrNull()
            val centralAddress = centralAddressRepo.getCentralAddress()
            if (generalAddressData == null) {
                Log.e("createCryptoAddresses", "No addresses found in AddressesWithKeysForM")
                return
            }

            runCatching {
                val result = addressesWithKeysForM.addresses
                    .drop(1)
                    .map {
                        CryptoAddressProto.SotAddressData.newBuilder()
                            .setAddress(it.address)
                            .setPubKey(it.publicKey)
                            .setIndex(it.indexSot.toInt())
                            .setDerivationIndex(it.indexDerivationSot)
                            .build()
                    }

                try {
                    cryptoAddressGrpcClient.addCentralAddress(
                        CryptoAddressProto.AddCentralAddressRequest.newBuilder()
                            .setAppId(profileRepo.getProfileAppId())
                            .setAddress(centralAddress!!.address)
                            .setPubKey(centralAddress.publicKey)
                            .build()
                    )
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    Log.e("createCryptoAddresses", "Failed to add central address", e)
                }

                cryptoAddressGrpcClient.addWallet(
                    CryptoAddressProto.AddWalletRequest.newBuilder()
                        .setAppId(profileRepo.getProfileAppId())
                        .setGeneralAddress(
                            CryptoAddressProto.GeneralAddressData.newBuilder()
                                .setAddress(generalAddressData.address)
                                .setPubKey(generalAddressData.publicKey)
                                .addAllDerivedIndices(addressesWithKeysForM.derivedIndices)
                        )
                        .addAllSotAddresses(result)
                        .build()
                )
            }.onSuccess { result ->
                result.fold(
                    onSuccess = { _ -> },
                    onFailure = { e ->
                        throw GrpcResponseException("Failed to perform gRPC response", e)
                            .also {
                                Log.e("Failed to perform gRPC response", it.message, e)
                                Sentry.captureException(it)
                            }
                    },
                )
            }.onFailure { e ->
                throw GrpcRequestException("Failed to perform gRPC request", e)
                    .also {
                        Log.e("Failed to perform gRPC request", it.message, e)
                        Sentry.captureException(it)
                    }
            }
        }

        override suspend fun registerUserAccount(
            deviceToken: String,
            sharedPref: SharedPreferences,
        ) {
            val uuid = UUID.randomUUID().toString()

            runCatching {
                val result = userGrpcClient.registerUser(uuid, deviceToken)

                result.fold(
                    onSuccess = { response ->
                        // Сохраняем профиль
                        profileRepo.insertNewProfile(
                            ProfileEntity(
                                userId = response.userId,
                                appId = uuid,
                                deviceToken = deviceToken,
                            ),
                        )

                        // Сохраняем токены
                        sharedPref.edit(commit = true) {
                            putEncryptedToken(PrefKeys.JWT_ACCESS_TOKEN, response.accessToken)
                            putEncryptedToken(PrefKeys.JWT_REFRESH_TOKEN, response.refreshToken)
                        }

                        Log.i("registerUserAccount", "User registered successfully: ${response.userId}")
                        true
                    },
                    onFailure = { e ->
                        throw GrpcResponseException("Failed to registerUserAccount", e)
                            .also {
                                Log.e("Failed to registerUserAccount", it.message, e)
                                Sentry.captureException(it)
                            }
                    },
                )
            }.getOrElse { e ->
                throw GrpcRequestException("Unexpected error while registerUserAccount", e)
                    .also {
                        Log.e("Unexpected error while registerUserAccount", it.message, e)
                        Sentry.captureException(it)
                    }
            }
        }

        override suspend fun registerUserDevice(
            userId: Long,
            deviceToken: String,
            sharedPref: SharedPreferences,
        ) {
            val uuid = UUID.randomUUID().toString()

            runCatching {
                val result = userGrpcClient.registerUserDevice(userId, uuid, deviceToken)

                result.fold(
                    onSuccess = { response ->
                        // Сохраняем профиль
                        profileRepo.insertNewProfile(
                            ProfileEntity(
                                userId = userId,
                                appId = uuid,
                                deviceToken = deviceToken,
                            ),
                        )

                        // Сохраняем токены
                        sharedPref.edit(commit = true) {
                            putEncryptedToken(PrefKeys.JWT_ACCESS_TOKEN, response.accessToken)
                            putEncryptedToken(PrefKeys.JWT_REFRESH_TOKEN, response.refreshToken)
                        }

                        Log.i("registerUserDevice", "Device successfully registered for userId=$userId")
                    },
                    onFailure = { e ->
                        throw GrpcResponseException("Failed to registerUserDevice", e)
                            .also {
                                Log.e("Failed to registerUserDevice", it.message, e)
                                Sentry.captureException(it)
                            }
                    },
                )
            }.onFailure { e ->
                throw GrpcRequestException("Unexpected error while registerUserDevice", e)
                    .also {
                        Log.e("Unexpected error while registerUserDevice", it.message, e)
                        Sentry.captureException(it)
                    }
            }
        }

        private fun SharedPreferences.Editor.putEncryptedToken(
            key: String,
            token: String,
        ) {
            val encrypted = keystore.encrypt(token.toByteArray(Charsets.UTF_8))
            putString(key, Base64.encodeToString(encrypted, Base64.NO_WRAP))
        }
    }
