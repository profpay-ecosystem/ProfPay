package com.example.telegramWallet

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
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
    lateinit var networkMonitor: NetworkMonitor
    private var navController: NavHostController? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Intent>
    @Inject lateinit var appInitializer: AppInitializer
    lateinit var viewModel: ThemeViewModel
    private val pinLockViewModel: PinLockViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Pushy.listen(this)

        val sharedPrefs = this.getSharedPreferences(
            ContextCompat.getString(this, R.string.preference_file_key),
            MODE_PRIVATE
        )

        networkMonitor = NetworkMonitor(context = this, sharedPref = sharedPrefs)
        networkMonitor.register()

        enableEdgeToEdge()

        val lifecycleObserver = AppLifecycleObserver(
            onAppForegrounded = { pinLockViewModel.checkPinState() },
            onAppBackgrounded = { AppLockManager.lock() }
        )

        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        SentryAndroid.init(this) { options ->
            options.isEnableUserInteractionTracing = true
            options.isEnableUserInteractionBreadcrumbs = true
        }

        lifecycleScope.launch { appInitializer.initialize(sharedPrefs, this@MainActivity) }

        // Инициализация ActivityResultLauncher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                setContent {
                    MyAppContent(sharedPrefs, networkMonitor)
                }
            }
        }

        setContent {
            MyAppContent(sharedPrefs, networkMonitor)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    private fun MyAppContent(sharedPrefs: SharedPreferences, networkMonitor: NetworkMonitor) {
        viewModel = hiltViewModel()
        navController = rememberNavController()
        val isDarkTheme: Boolean

        val state by viewModel.state.collectAsStateWithLifecycle()
        val isSystemInDarkTheme = isSystemInDarkTheme()
        when (state) {
            is ThemeState.Loading -> viewModel.getThemeApp(sharedPrefs)
            is ThemeState.Success -> {
                viewModel.getThemeApp(sharedPrefs)
                isDarkTheme = viewModel.isDarkTheme(
                    (state as ThemeState.Success).themeStateResult,
                    isSystemInDarkTheme
                )
                WalletNavigationBottomBarTheme(isDarkTheme = isDarkTheme) {
                    MyApp(navController = navController!!, networkMonitor = networkMonitor)
                }
            }
        }
    }
}