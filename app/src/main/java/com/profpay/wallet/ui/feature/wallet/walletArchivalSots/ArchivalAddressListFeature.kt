package com.profpay.wallet.ui.feature.wallet.walletArchivalSots

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.profpay.wallet.data.database.models.AddressWithTokens


@Composable
fun ArchivalAddressListFeature(
    addresses: List<AddressWithTokens>,
    goToWalletAddress: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        items(addresses) { addressWithTokens ->
            CardArchivalAddressFeature(
                goToWalletAddress = goToWalletAddress,
                addressWithTokens = addressWithTokens,
            )
        }
    }
}