package com.profpay.wallet.ui.screens.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.bridge.viewmodel.settings.SettingsAccountViewModel
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.feature.settings.account.DescriptionCardSettingsAccountFeature
import com.profpay.wallet.ui.feature.settings.account.ProfPayCardSettingsAccountFeature
import com.profpay.wallet.ui.feature.settings.account.SectionTitleSettingsAccountFeature
import com.profpay.wallet.ui.feature.settings.account.TelegramCardSettingsAccountFeature

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAccountScreen(
    goToBack: () -> Unit,
    viewModel: SettingsAccountViewModel = hiltViewModel(),
) {
    val tgId by viewModel.profileTelegramId.observeAsState()
    val tgUsername by viewModel.profileTelegramUsername.observeAsState()

    var userId by remember { mutableStateOf<Long?>(null) }
    var appId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getUserTelegramData()
        viewModel.loadUserAndAppIds(
            onLoaded = { u, a ->
                userId = u
                appId = a
            },
        )
    }

    CustomScaffoldWallet { bottomPadding ->
        CustomTopAppBar(title = "Account", goToBack = goToBack)
        CustomBottomCard(
            modifier = Modifier.weight(0.8f),
            modifierColumn = Modifier.verticalScroll(rememberScrollState()),
            bottomPadding = bottomPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.size(16.dp))
            DescriptionCardSettingsAccountFeature()
            SectionTitleSettingsAccountFeature("Telegram")
            TelegramCardSettingsAccountFeature(tgId = tgId, tgUsername = tgUsername)
            SectionTitleSettingsAccountFeature("ProfPay")
            ProfPayCardSettingsAccountFeature(userId = userId, appId = appId, status = "User")
        }
    }
}
