package com.profpay.wallet.data.services

object AppLockManager {
    private var isLocked = false

    fun lock() {
        isLocked = true
    }

    fun unlock() {
        isLocked = false
    }

    fun isAppLocked(): Boolean = isLocked
}
