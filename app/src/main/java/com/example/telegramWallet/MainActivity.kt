package com.example.telegramWallet

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.app.NotificationManagerCompat
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
import com.example.telegramWallet.ui.app.theme.WalletNavigationBottomBarTheme
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.launch
import me.pushy.sdk.Pushy
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject lateinit var appInitializer: AppInitializer
    private val pinLockViewModel: PinLockViewModel by viewModels()
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Pushy.listen(this)

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

        SentryAndroid.init(this) { options ->
            options.isEnableUserInteractionTracing = true
            options.isEnableUserInteractionBreadcrumbs = true
        }

        lifecycleScope.launch {
            appInitializer.initialize(sharedPrefs, this@MainActivity)
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                launchContent(sharedPrefs)
            }
        }

        launchContent(sharedPrefs)
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
                    WalletNavigationBottomBarTheme(isDarkTheme) {
                        val navController = rememberNavController()
                        MyApp(navController, networkMonitor)
                    }
                }
            }
        }
    }
}