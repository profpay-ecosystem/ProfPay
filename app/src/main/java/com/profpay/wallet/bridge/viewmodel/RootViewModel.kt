package com.profpay.wallet.bridge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.repository.flow.AppAccessRepo
import com.profpay.wallet.data.repository.flow.AppAccessState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val appAccessRepo: AppAccessRepo,
) : ViewModel() {
    val accessState = appAccessRepo.accessStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppAccessState.Allowed
        )

    fun isAppRestricted() {
        viewModelScope.launch {
            appAccessRepo.isAppRestricted()
        }
    }
}
