package com.profpay.wallet.bridge.viewmodel.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.database.dao.wallet.WalletProfileModel
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import com.profpay.wallet.security.KeystoreCryptoManager
import com.profpay.wallet.tron.Tron
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletSystemViewModel
    @Inject
    constructor(
        private val walletProfileRepo: WalletProfileRepo,
        private val addressRepo: AddressRepo,
        private val tron: Tron,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        private val keystoreCryptoManager: KeystoreCryptoManager,
    ) : ViewModel() {
        fun getListAllWallets(): LiveData<List<WalletProfileModel>> =
            liveData(ioDispatcher) {
                emitSource(walletProfileRepo.getListAllWalletsFlow().asLiveData())
            }

        fun updateNameWalletById(
            id: Long,
            newName: String,
        ) = viewModelScope.launch(ioDispatcher) {
            walletProfileRepo.updateNameById(id, newName)
        }

        suspend fun getSeedPhrase(walletId: Long): String? {
            val generalAddress = addressRepo.getGeneralAddressByWalletId(walletId)
            val cipherData = walletProfileRepo.getWalletCipherData(walletId)

            val entropy =
                keystoreCryptoManager.decrypt(
                    alias = generalAddress,
                    iv = cipherData.iv,
                    cipherText = cipherData.cipherText,
                )

            return tron.addressUtilities.getSeedPhraseByEntropy(entropy)
        }

        fun deleteWalletProfile(walletId: Long) =
            viewModelScope.launch(ioDispatcher) {
                walletProfileRepo.deleteWalletProfile(walletId)
            }
    }
