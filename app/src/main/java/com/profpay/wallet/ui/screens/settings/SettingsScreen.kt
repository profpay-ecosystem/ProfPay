package com.profpay.wallet.ui.screens.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.R
import com.profpay.wallet.bridge.viewmodel.settings.ThemeViewModel
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.feature.settings.settings.CardForSettings
import com.profpay.wallet.ui.feature.settings.settings.ContentCardForSettingsTheme
import com.profpay.wallet.ui.shared.sharedPref

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(
    themeVM: ThemeViewModel = hiltViewModel(),
    goToSettingsNotifications: () -> Unit,
    goToSettingsAccount: () -> Unit,
    goToSettingsSecurity: () -> Unit,
    goToSettingsAml: () -> Unit,
) {
    var openThemeChoice by remember { mutableStateOf(false) }

    val shared = sharedPref()
    var themeSharedInt by remember { mutableIntStateOf(shared.getInt("valueTheme", 2)) }
    val themesNames = listOf("Светлая", "Тёмная", "Системная")

    CustomScaffoldWallet { bottomPadding ->
        CustomTopAppBar(title = "Settings")
        CustomBottomCard(
            modifier = Modifier.weight(0.8f),
            modifierColumn = Modifier.verticalScroll(rememberScrollState()),
            bottomPadding = bottomPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.size(16.dp))

            CardForSettings(
                iconID = R.drawable.icon_settings_account,
                label = "Аккаунт",
                iconSize = 24,
                onClick = { goToSettingsAccount() },
            )
            CardForSettings(
                iconID = R.drawable.icon_settings_aml,
                label = "AML",
                iconSize = 24,
                onClick = { goToSettingsAml() },
            )
            CardForSettings(
                iconID = R.drawable.icon_settings_theme,
                label = "Тема",
                noClick = true,
            ) {
                ContentCardForSettingsTheme(
                    openThemeChoice = openThemeChoice,
                    onOpenThemeChoiceChange = { openThemeChoice = it },
                    themeSharedInt = themeSharedInt,
                    onThemeSelected = { themeSharedInt = it },
                    themesNames = themesNames,
                    shared = shared,
                    themeVM = themeVM,
                )
            }
            CardForSettings(
                onClick = { goToSettingsNotifications() },
                iconID = R.drawable.icon_settings_alert,
                label = "Уведомления",
            )
            CardForSettings(
                onClick = {},
                iconID = R.drawable.icon_settings_dollar,
                label = "Валюта",
            ) {
                Text(
                    "USD",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            CardForSettings(
                onClick = {},
                iconID = R.drawable.icon_settings_support,
                label = "Поддержка",
            )
            CardForSettings(
                onClick = { goToSettingsSecurity() },
                iconID = R.drawable.icon_settings_security,
                label = "Безопасность",
            )
            CardForSettings(
                onClick = {},
                iconID = R.drawable.icon_settings_faq,
                label = "FAQ",
            )
            CardForSettings(
                onClick = {},
                iconID = R.drawable.icon_settings_privacy_policy,
                label = "Политика \nКонфиденциальности",
                smallLabel = true,
            )
            Spacer(modifier = Modifier.size(10.dp))
        }
    }
}
