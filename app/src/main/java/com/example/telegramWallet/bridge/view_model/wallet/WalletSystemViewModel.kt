package com.example.telegramWallet.bridge.view_model.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import cash.z.ecc.android.bip39.Mnemonics
import com.example.telegramWallet.data.database.dao.wallet.WalletProfileModel
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.WalletProfileRepo
import com.example.telegramWallet.tron.Tron
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class WalletSystemViewModel @Inject constructor(
    private val walletProfileRepo: WalletProfileRepo,
    private val addressRepo: AddressRepo,
    private val tron: Tron
) : ViewModel() {

    fun getListAllWallets(): LiveData<List<WalletProfileModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(walletProfileRepo.getListAllWallets())
        }
    }

    suspend fun updateNameWalletById(id: Long, newName: String) {
        walletProfileRepo.updateNameById(id, newName)
    }

    suspend fun getSeedPhrase(walletId: Long): String? {
        val decryptedEntropy = walletProfileRepo.getWalletDecryptedEntropy(walletId) ?: return null
        return tron.addressUtilities.getSeedPhraseByEntropy(decryptedEntropy)
    }

    suspend fun deleteWalletProfile(walletId: Long){
        walletProfileRepo.deleteWalletProfile(walletId)
    }
}