package com.profpay.wallet.ui.screens.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.components.feature.settings.CardWithTextForSettings
import com.profpay.wallet.ui.components.feature.settings.switchForSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsNotificationsScreen(goToBack: () -> Unit) {
    var checkIsTest by remember { mutableStateOf(false) }

    val (checkNotificationsInWallet, setCheckNotificationsInWallet) =
        remember {
            mutableStateOf(
                false,
            )
        }
    CustomScaffoldWallet { bottomPadding ->
        CustomTopAppBar(title = "Settings notifications", goToBack = goToBack)
        CustomBottomCard(
            modifier = Modifier.weight(0.8f),
            modifierColumn = Modifier.verticalScroll(rememberScrollState()),
            bottomPadding = bottomPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.size(16.dp))
            CardWithTextForSettings(label = "Уведомления в Telegram") {
                setCheckNotificationsInWallet(
                    switchForSettings(checkNotificationsInWallet) {
                        checkIsTest = it
                    },
                )
            }
            Spacer(modifier = Modifier.size(20.dp))
        }
    }
}
