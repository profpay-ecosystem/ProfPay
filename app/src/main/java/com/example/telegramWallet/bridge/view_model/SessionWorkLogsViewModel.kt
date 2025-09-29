package com.example.telegramWallet.bridge.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class SessionWorkLogsViewModel
    @Inject
    constructor() : ViewModel() {
        // Используем val logs by viewModel.logCatOutput().observeAsState() в UI
        // Надо использовать второй параметр через который мы передаем тип лога, можем юзать Enum,
        // EWD по отдельности или EWD вместе это All, 5 тип лога без тегов это internal: Boolean
        // Получаем логи из logcat андроида, логи передаются по потоковому каналу нашему приложению
        // (реализация отключена, будет переделана)
        fun logCatOutput(internal: Int) =
            liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
                var stringExec = ""
                when (internal) {
                    0 -> stringExec = "logcat com.example.telegramWallet:E -s Tron, Bot"
                    1 -> stringExec = "logcat com.example.telegramWallet:W -s Tron, Bot"
                    2 -> stringExec = "logcat com.example.telegramWallet:D -s Tron, Bot"
                    3 -> stringExec = "logcat com.example.telegramWallet:E"
                }
                Runtime
                    .getRuntime()
                    .exec(stringExec)
                    .inputStream
                    .bufferedReader()
                    .useLines { lines -> lines.forEach { line -> emit(line) } }
            }
    }
