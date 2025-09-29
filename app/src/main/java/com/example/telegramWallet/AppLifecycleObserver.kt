package com.example.telegramWallet

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver(
    private val onAppForegrounded: () -> Unit,
    private val onAppBackgrounded: () -> Unit,
) : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        onAppForegrounded()
    }

    override fun onStop(owner: LifecycleOwner) {
        onAppBackgrounded()
    }
}
