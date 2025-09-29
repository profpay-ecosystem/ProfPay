package com.profpay.wallet.ui.shared

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.profpay.wallet.R

@Composable
fun sharedPref(): SharedPreferences =
    LocalContext.current.getSharedPreferences(
        ContextCompat.getString(LocalContext.current, R.string.preference_file_key),
        Context.MODE_PRIVATE,
    )
