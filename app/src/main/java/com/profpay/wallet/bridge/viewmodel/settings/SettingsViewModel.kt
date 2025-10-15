package com.profpay.wallet.bridge.viewmodel.settings

import androidx.lifecycle.ViewModel
import com.profpay.wallet.data.database.repositories.ProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        val profileRepo: ProfileRepo,
    ) : ViewModel()
