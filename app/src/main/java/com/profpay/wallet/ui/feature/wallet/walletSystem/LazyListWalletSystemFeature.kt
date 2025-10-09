package com.profpay.wallet.ui.feature.wallet.walletSystem

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.profpay.wallet.data.database.dao.wallet.WalletProfileModel
import com.profpay.wallet.ui.shared.sharedPref

@Composable
fun LazyListWalletSystemFeature(
    walletList: List<WalletProfileModel>,
    goToWalletInfo: () -> Unit,
) {
    val sharedPref = sharedPref()

    LazyColumn(
        modifier = Modifier.padding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (walletList.isNotEmpty()) {
            items(walletList) { wallet ->
                val currentWalletId = sharedPref.getLong("wallet_id", 1)
                CardForWalletSystemFeature(
                    wallet = wallet,
                    onClick = {
                        sharedPref.edit { putLong("wallet_id", wallet.id!!) }
                        goToWalletInfo()
                    },
                    selected = wallet.id == currentWalletId,
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
