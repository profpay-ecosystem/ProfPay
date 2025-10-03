package com.profpay.wallet.bridge.view_model.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.profpay.wallet.data.database.dao.wallet.WalletProfileModel
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import com.profpay.wallet.tron.Tron
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class WalletSystemViewModel
    @Inject
    constructor(
        private val walletProfileRepo: WalletProfileRepo,
        private val addressRepo: AddressRepo,
        private val tron: Tron,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : ViewModel() {
        fun getListAllWallets(): LiveData<List<WalletProfileModel>> =
            liveData(dispatcher) {
                emitSource(walletProfileRepo.getListAllWallets())
            }

        suspend fun updateNameWalletById(
            id: Long,
            newName: String,
        ) {
            walletProfileRepo.updateNameById(id, newName)
        }

        fun getSeedPhrase(walletId: Long): String? {
            return "empty11"
        }

        suspend fun deleteWalletProfile(walletId: Long) {
            walletProfileRepo.deleteWalletProfile(walletId)
        }

        suspend fun hasAnyWalletProfile(): Boolean = walletProfileRepo.hasAnyWalletProfile()
    }
