package com.example.telegramWallet

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.telegramWallet.bridge.view_model.pin_lock.PinLockViewModel
import com.example.telegramWallet.bridge.view_model.settings.ThemeState
import com.example.telegramWallet.bridge.view_model.settings.ThemeViewModel
import com.example.telegramWallet.data.services.AppLockManager
import com.example.telegramWallet.data.services.NetworkMonitor
import com.example.telegramWallet.ui.app.navigation.MyApp
import com.example.telegramWallet.ui.app.navigation.graphs.navGraph.WalletInfo
import com.example.telegramWallet.ui.app.theme.WalletNavigationBottomBarTheme
import com.example.telegramWallet.ui.screens.NotNetworkScreen
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.launch
import me.pushy.sdk.Pushy
import me.pushy.sdk.util.exceptions.PushyNetworkException
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject lateinit var appInitializer: AppInitializer
    private val pinLockViewModel: PinLockViewModel by viewModels()
    private lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Pushy.listen(this)

        SentryAndroid.init(this) { options ->
            options.isEnableUserInteractionTracing = true
            options.isEnableUserInteractionBreadcrumbs = true
            options.isAttachScreenshot = false
        }

        val sharedPrefs = getSharedPreferences(
            ContextCompat.getString(this, R.string.preference_file_key),
            MODE_PRIVATE
        )

        networkMonitor = NetworkMonitor(this, sharedPrefs).also { it.register() }
        enableEdgeToEdge()

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            AppLifecycleObserver(
                onAppForegrounded = { pinLockViewModel.checkPinState() },
                onAppBackgrounded = { AppLockManager.lock() }
            )
        )

        lifecycleScope.launch {
            try {
                appInitializer.initialize(sharedPrefs, this@MainActivity)
                launchContent(sharedPrefs)
            } catch (e: PushyNetworkException) {
                Sentry.captureException(e)
                launchEthLostContent(sharedPrefs)
            }
        }
    }

    private fun launchEthLostContent(sharedPrefs: SharedPreferences) {
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val state by themeViewModel.state.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()

            when (val themeState = state) {
                is ThemeState.Loading -> {
                    themeViewModel.getThemeApp(sharedPrefs)
                }
                is ThemeState.Success -> {
                    val isDarkTheme = themeViewModel.isDarkTheme(
                        themeState.themeStateResult,
                        isSystemDark
                    )
                    WalletNavigationBottomBarTheme(activity = this, isDarkTheme = isDarkTheme) {
                        NotNetworkScreen()
                    }
                }
            }
        }
    }

    private fun launchContent(sharedPrefs: SharedPreferences) {
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val state by themeViewModel.state.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()

            when (val themeState = state) {
                is ThemeState.Loading -> {
                    themeViewModel.getThemeApp(sharedPrefs)
                }
                is ThemeState.Success -> {
                    val isDarkTheme = themeViewModel.isDarkTheme(
                        themeState.themeStateResult,
                        isSystemDark
                    )
                    WalletNavigationBottomBarTheme(activity = this, isDarkTheme = isDarkTheme) {
                        val navController = rememberNavController()
                        MyApp(navController, networkMonitor)
                    }
                }
            }
        }
    }
}