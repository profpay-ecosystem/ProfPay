package com.profpay.wallet.ui.screens.wallet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.bridge.viewmodel.wallet.WalletSystemViewModel
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.feature.wallet.walletSystem.ButtonAddWalletSystemFeature
import com.profpay.wallet.ui.feature.wallet.walletSystem.LazyListWalletSystemFeature

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSystemScreen(
    goToBack: () -> Unit,
    goToWalletInfo: () -> Unit,
    goToCoRA: () -> Unit,
    viewModel: WalletSystemViewModel = hiltViewModel(),
) {
    val walletList by viewModel.getListAllWallets().observeAsState(emptyList())

    CustomScaffoldWallet { bottomPadding ->
        CustomTopAppBar(title = "Wallet System", goToBack = { goToBack() })
        CustomBottomCard(
            modifier = Modifier.weight(0.8f),
            modifierColumn = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            bottomPadding = bottomPadding,
        ) {
            Text(
                text = "Выберите кошелёк:",
                modifier =
                    Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
            )
            LazyListWalletSystemFeature(
                walletList = walletList,
                goToWalletInfo = { goToWalletInfo() },
            )
            ButtonAddWalletSystemFeature(goToCoRA = { goToCoRA() })
        }
    }
}
