package com.profpay.wallet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import me.pushy.sdk.Pushy

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Pushy.toggleForegroundService(true, this)
    }
}
