package com.profpay.wallet.bridge.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.repository.flow.BlockingAppRepo
import com.profpay.wallet.data.repository.flow.BlockingAppRepoState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockingAppViewModel
    @Inject
    constructor(
        private val blockingAppRepo: BlockingAppRepo,
    ) : ViewModel() {
        // Триггер на обновление состояния блокировки
        fun getBlockedAppState() {
            viewModelScope.launch { blockingAppRepo.getBlockedAppState() }
        }

        private val _state: MutableStateFlow<BlockingAppState> =
            MutableStateFlow(BlockingAppState.Loading)

        // Состояния блокировки true\false
        val state: StateFlow<BlockingAppState> = _state.asStateFlow()

        init {
            getBlockedAppState()
            viewModelScope.launch {
                blockingAppRepo.isBlockedApp.collect { _state.value = BlockingAppState.Success(it) }
            }
        }
    }

sealed interface BlockingAppState {
    data object Loading : BlockingAppState

    data class Success(
        val value: BlockingAppRepoState,
    ) : BlockingAppState
}
