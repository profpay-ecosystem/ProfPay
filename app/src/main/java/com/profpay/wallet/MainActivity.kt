package com.profpay.wallet

import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
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
import com.profpay.wallet.bridge.view_model.pin_lock.PinLockViewModel
import com.profpay.wallet.bridge.view_model.settings.ThemeState
import com.profpay.wallet.bridge.view_model.settings.ThemeViewModel
import com.profpay.wallet.data.services.AppLockManager
import com.profpay.wallet.data.services.NetworkMonitor
import com.profpay.wallet.ui.app.navigation.MyApp
import com.profpay.wallet.ui.app.theme.WalletNavigationBottomBarTheme
import com.profpay.wallet.ui.screens.NotNetworkScreen
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

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        Pushy.listen(this)

        SentryAndroid.init(this) { options ->
            options.isEnableUserInteractionTracing = true
            options.isEnableUserInteractionBreadcrumbs = true
            options.isAttachScreenshot = false
        }

        val sharedPrefs =
            getSharedPreferences(
                ContextCompat.getString(this, R.string.preference_file_key),
                MODE_PRIVATE,
            )

        networkMonitor = NetworkMonitor(this).also { it.register() }
        enableEdgeToEdge()

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            AppLifecycleObserver(
                onAppForegrounded = { pinLockViewModel.checkPinState() },
                onAppBackgrounded = { AppLockManager.lock() },
            ),
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
                    val isDarkTheme =
                        themeViewModel.isDarkTheme(
                            themeState.themeStateResult,
                            isSystemDark,
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
                    val isDarkTheme =
                        themeViewModel.isDarkTheme(
                            themeState.themeStateResult,
                            isSystemDark,
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
