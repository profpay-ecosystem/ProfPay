package com.profpay.wallet.ui.app.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

private val DarkColorPalette =
    darkColorScheme(
        primary = BackgroundDark,
        secondary = BackgroundLight,
        onPrimary = BackgroundLight,
        surface = BackgroundDark,
        onSurface = BackgroundLight,
        surfaceVariant = BackgroundDark,
        surfaceContainer = BackgroundContainerButtonDark,
        onSecondaryContainer = PubAddressDark,
        surfaceBright = BackgroundContainerButtonDark,
    )

private val LightColorPalette =
    lightColorScheme(
        primary = BackgroundLight,
        secondary = BackgroundDark,
        onPrimary = BackgroundDark,
        surface = BackgroundLight,
        onSurface = BackgroundDark,
        surfaceVariant = BackgroundLight,
        surfaceContainer = BackgroundContainerButtonLight,
        onSecondaryContainer = PubAddressLight,
        surfaceBright = BackgroundIcon,
    )

@Composable
fun WalletNavigationBottomBarTheme(
    activity: ComponentActivity,
    isDarkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    DisposableEffect(isDarkTheme) {
        activity.enableEdgeToEdge(
            statusBarStyle =
                if (isDarkTheme) {
                    SystemBarStyle.dark(Color.Transparent.toArgb())
                } else {
                    SystemBarStyle.light(Color.Transparent.toArgb(), Color.Transparent.toArgb())
                },
            navigationBarStyle =
                if (isDarkTheme) {
                    SystemBarStyle.dark(BackgroundDark.toArgb())
                } else {
                    SystemBarStyle.light(BackgroundLight.toArgb(), BackgroundLight.toArgb())
                },
        )
        onDispose { }
    }

    MaterialTheme(
        colorScheme = if (isDarkTheme) DarkColorPalette else LightColorPalette,
        typography = rememberTypography("Manrope"),
        shapes = Shapes,
        content = content,
    )
}
