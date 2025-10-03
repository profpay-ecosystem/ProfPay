package com.profpay.wallet.bridge.view_model.wallet.walletSot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.profpay.wallet.bridge.view_model.dto.BlockchainName
import com.profpay.wallet.data.database.entities.wallet.AddressEntity
import com.profpay.wallet.data.database.entities.wallet.TokenEntity
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.database.repositories.wallet.TokenRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import com.profpay.wallet.data.flow_db.repo.WalletSotRepo
import com.profpay.wallet.security.KeystoreCryptoManager
import com.profpay.wallet.tron.Tron
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class WalletSotViewModel
    @Inject
    constructor(
        private val addressRepo: AddressRepo,
        private val walletProfileRepo: WalletProfileRepo,
        private val tokenRepo: TokenRepo,
        private val walletSotRepo: WalletSotRepo,
        private val profileRepo: ProfileRepo,
        private val centralAddressRepo: CentralAddressRepo,
        private val tron: Tron,
        private val keystoreCryptoManager: KeystoreCryptoManager,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : ViewModel() {
        // Получение списка адресов и балансов в формате Flow
        fun getAddressesSotsWithTokensByBlockchainLD(
            walletId: Long,
            blockchainName: String,
        ): LiveData<List<AddressWithTokens>> =
            liveData(dispatcher) {
                emitSource(addressRepo.getAddressesSotsWithTokensByBlockchainLD(walletId, blockchainName))
            }

        suspend fun creationOfANewCell(
            walletId: Long,
            addressEntity: AddressEntity,
        ) {
            val generalAddress = addressRepo.getGeneralAddressByWalletId(walletId)
            val cipherData = walletProfileRepo.getWalletCipherData(walletId)

            val entropy = keystoreCryptoManager.decrypt(
                alias = generalAddress,
                iv = cipherData.iv,
                cipherText = cipherData.cipherText
            )

            val newSotDerivationIndex = addressRepo.getMaxSotDerivationIndex(walletId) + 1
            val userAppId = profileRepo.getProfileAppId()

            val result =
                tron.addressUtilities.creationOfANewCell(
                    entropy = entropy,
                    index = newSotDerivationIndex.toLong(),
                )

            val address =
                tron.addressUtilities.public2Address(result!!.pubKeyPoint.getEncoded(false))
                    ?: throw Exception("The public address has not been created!")

            try {
                walletSotRepo.updateDerivedIndex(
                    appId = userAppId,
                    oldIndex = addressEntity.sotDerivationIndex.toLong(),
                    newIndex = newSotDerivationIndex.toLong(),
                    generalAddress = generalAddress,
                )
            } catch (e: Exception) {
                Log.e("ERROR", e.message!!)
                return
            }

            addressRepo.updateSotIndexByAddressId(
                index = -1,
                addressId = addressEntity.addressId!!,
            )

            BlockchainName.entries.map { blockchain ->
                val addressId =
                    addressRepo.insertNewAddress(
                        AddressEntity(
                            walletId = walletId,
                            blockchainName = blockchain.blockchainName,
                            address = address,
                            publicKey = result.publicKeyAsHex,
                            isGeneralAddress = false,
                            sotIndex = addressEntity.sotIndex,
                            sotDerivationIndex = newSotDerivationIndex,
                        ),
                    )
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

            val centralAddress = centralAddressRepo.insertIfNotExists()
            if (centralAddress != null) {
                val balance = tron.addressUtilities.getTrxBalance(centralAddress.address)
                if (balance >= BigInteger.valueOf(1_500_000)) {
                    val newBalance = tron.addressUtilities.getTrxBalance(centralAddress.address)
                    if (newBalance < BigInteger.valueOf(1_000_000)) return
                    if (!tron.addressUtilities.isAddressActivated(address)) {
                        tron.transactions.trxTransfer(
                            fromAddress = centralAddress.address,
                            toAddress = address,
                            privateKey = centralAddress.privateKey,
                            amount = 1_000,
                        )
                    }
                }
            }
        }
    }
