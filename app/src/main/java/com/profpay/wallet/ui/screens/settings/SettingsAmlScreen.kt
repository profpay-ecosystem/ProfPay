package com.profpay.wallet.ui.screens.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.profpay.wallet.PrefKeys.AUTO_CHECK_AML
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.components.feature.settings.CardWithTextForSettings
import com.profpay.wallet.ui.components.feature.settings.switchForSettings
import com.profpay.wallet.ui.shared.sharedPref

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAmlScreen(goToBack: () -> Unit) {
    val sharedPref = sharedPref()

    val (useAutoCheckAml, setUseAutoCheckAml) =
        remember {
            mutableStateOf(
                sharedPref.getBoolean(AUTO_CHECK_AML, true),
            )
        }

    CustomScaffoldWallet { bottomPadding ->
        CustomTopAppBar(title = "AML", goToBack = goToBack)
        CustomBottomCard(
            modifier = Modifier.weight(0.8f),
            modifierColumn = Modifier.verticalScroll(rememberScrollState()),
            bottomPadding = bottomPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.size(16.dp))
            CardWithTextForSettings(label = "Авто. проверка AML") {
                setUseAutoCheckAml(
                    switchForSettings(useAutoCheckAml) {
                        sharedPref.edit { putBoolean(AUTO_CHECK_AML, it) }
                    },
                )
            }
            Spacer(modifier = Modifier.size(20.dp))
        }
    }
}
