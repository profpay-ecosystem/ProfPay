package com.profpay.wallet.bridge.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class GetContextViewModel
    @Inject
    constructor(
        @ApplicationContext private val applicationContext: Context,
    ) : ViewModel() {
        // Получение контекста
        fun getAppContext(): Context = applicationContext
    }
