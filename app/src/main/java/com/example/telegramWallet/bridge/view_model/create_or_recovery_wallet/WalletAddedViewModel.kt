package com.example.telegramWallet.bridge.view_model.create_or_recovery_wallet

import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.example.telegramWallet.PrefKeys
import com.example.telegramWallet.backend.grpc.CryptoAddressGrpcClient
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.UserGrpcClient
import com.example.telegramWallet.bridge.view_model.dto.BlockchainName
import com.example.telegramWallet.data.database.entities.ProfileEntity
import com.example.telegramWallet.data.database.entities.wallet.AddressEntity
import com.example.telegramWallet.data.database.entities.wallet.TokenEntity
import com.example.telegramWallet.data.database.entities.wallet.WalletProfileEntity
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.database.repositories.wallet.WalletProfileRepo
import com.example.telegramWallet.security.KeystoreEncryptionUtils
import com.example.telegramWallet.tron.AddressesWithKeysForM
import com.example.telegramWallet.ui.shared.sharedPref
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class WalletAddedViewModel @Inject constructor(
    private val walletProfileRepo: WalletProfileRepo,
    private val addressRepo: AddressRepo,
    private val tokenRepo: TokenRepo,
    private val profileRepo: ProfileRepo,
    grpcClientFactory: GrpcClientFactory
) : ViewModel() {
    private val keystore = KeystoreEncryptionUtils()
    private val cryptoAddressGrpcClient: CryptoAddressGrpcClient = grpcClientFactory.getGrpcClient(
        CryptoAddressGrpcClient::class.java,
        "grpc.wallet-services-srv.com",
        8443
    )

    private val userGrpcClient: UserGrpcClient = grpcClientFactory.getGrpcClient(
        UserGrpcClient::class.java,
        "grpc.wallet-services-srv.com",
        8443
    )

    suspend fun insertNewCryptoAddresses(addressesWithKeysForM: AddressesWithKeysForM) {
        val walletId = withContext(Dispatchers.IO) {
            val number = walletProfileRepo.getCountRecords() + 1
            walletProfileRepo.insertNewWalletProfileEntity(name = "Wallet $number", addressesWithKeysForM = addressesWithKeysForM)
        }
        withContext(Dispatchers.IO) {
            try {
                BlockchainName.entries.map { blockchain -> /* Проходим по списку блокчейнов*/
                    addressesWithKeysForM.addresses.map { currentAddress -> /* Проходим по списку адресов*/
                        val addressId = addressRepo.insertNewAddress(
                            AddressEntity(
                                walletId = walletId,
                                blockchainName = blockchain.blockchainName,
                                address = currentAddress.address,
                                publicKey = currentAddress.publicKey,
                                privateKey = currentAddress.privateKey,
                                isGeneralAddress = currentAddress.indexDerivationSot == 0,
                                sotIndex = currentAddress.indexSot,
                                sotDerivationIndex = currentAddress.indexDerivationSot,
                            )
                        )
                        // проходим по списку токенов текущего блокчейна
                        blockchain.tokens.forEach { token ->
                            tokenRepo.insertNewTokenEntity(
                                TokenEntity(
                                    addressId = addressId,
                                    tokenName = token.tokenName,
                                    balance = BigInteger.ZERO,
                                )
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
            val result = cryptoAddressGrpcClient.addCryptoAddress(
                appId = profileRepo.getProfileAppId(),
                address = addressesWithKeysForM.addresses[0].address,
                pubKey = addressesWithKeysForM.addresses[0].publicKey,
                derivedIndices = addressesWithKeysForM.derivedIndices
            )
            result.fold(
                onSuccess = { response ->
                    Log.d("addCryptoAddress", response.toString())
                },
                onFailure = { exception ->
                    Sentry.captureException(exception)
                    Log.e("gRPC ERROR", "Error during gRPC call: ${exception.message}")
                }
            )
        } catch (e: Exception) {
            Sentry.captureException(e)
            Log.e("gRPC Exception", "Error during gRPC call: ${e.message}")
        }
    }

    suspend fun registerUserAccount(deviceToken: String, sharedPref: SharedPreferences): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val uuidString = java.util.UUID.randomUUID().toString()
                val result = userGrpcClient.registerUser(uuidString, deviceToken)
                result.fold(
                    onSuccess = { response ->
                        profileRepo.insertNewProfile(ProfileEntity(
                            userId = response.userId,
                            appId = uuidString,
                            deviceToken = deviceToken
                        ))
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
                    }
                )
            } catch (e: Exception) {
                Sentry.captureException(e)
                Log.e("gRPC ERROR", "Unexpected error: ${e.message}")
                false
            }
        }
    }

    suspend fun registerUserDevice(userId: Long, deviceToken: String, sharedPref: SharedPreferences) {
        return withContext(Dispatchers.IO) {
            try {
                val uuidString = java.util.UUID.randomUUID().toString()
                val result = userGrpcClient.registerUserDevice(userId, uuidString, deviceToken)
                result.fold(
                    onSuccess = { response ->
                        profileRepo.insertNewProfile(ProfileEntity(
                            userId = userId,
                            appId = uuidString,
                            deviceToken = deviceToken
                        ))
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
                    }
                )
            } catch (e: Exception) {
                Sentry.captureException(e)
                Log.e("gRPC ERROR", "Error during gRPC call: ${e.message}")
                false
            }
        }
    }
}
