package com.example.telegramWallet.ui.shared

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.telegramWallet.R

@Composable
fun sharedPref(): SharedPreferences =
    LocalContext.current.getSharedPreferences(
        ContextCompat.getString(LocalContext.current, R.string.preference_file_key),
        Context.MODE_PRIVATE,
    )
