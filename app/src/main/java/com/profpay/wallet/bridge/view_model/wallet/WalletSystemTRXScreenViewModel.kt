package com.profpay.wallet.bridge.view_model.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.database.entities.wallet.CentralAddressEntity
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import com.profpay.wallet.tron.Tron
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletSystemTRXScreenViewModel @Inject constructor(
    val centralAddressRepo: CentralAddressRepo,
    private val tron: Tron,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    init {
        viewModelScope.launch(ioDispatcher) {
            val isCentralAddressExists = centralAddressRepo.isCentralAddressExists()
            if (!isCentralAddressExists) {
                val address = tron.addressUtilities.generateSingleAddress()
                centralAddressRepo.insertNewCentralAddress(
                    CentralAddressEntity(
                        address = address.address,
                        publicKey = address.publicKey,
                        privateKey = address.privateKey
                    ),
                )
            }
        }
    }

    fun getCentralAddressLiveData(): LiveData<CentralAddressEntity?> =
        liveData(ioDispatcher) {
            emitSource(centralAddressRepo.getCentralAddressFlow().asLiveData())
        }
}
