package com.profpay.wallet.security

import androidx.datastore.preferences.core.stringPreferencesKey

object SecureDataStore {
    val PIN_CODE_KEY = stringPreferencesKey("pin_code_encrypted_base64")
}
