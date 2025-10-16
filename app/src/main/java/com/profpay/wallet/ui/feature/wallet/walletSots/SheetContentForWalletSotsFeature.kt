package com.profpay.wallet.ui.feature.wallet.walletSots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.bridge.viewmodel.dto.TokenName
import com.profpay.wallet.bridge.viewmodel.wallet.walletSot.WalletSotViewModel
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.ui.app.theme.HexagonColor1
import com.profpay.wallet.ui.app.theme.HexagonColor2
import com.profpay.wallet.ui.app.theme.HexagonColor3
import com.profpay.wallet.ui.app.theme.HexagonColor4
import com.profpay.wallet.ui.app.theme.HexagonColor5
import com.profpay.wallet.ui.app.theme.HexagonColor6
import com.profpay.wallet.ui.app.theme.HexagonColor7
import com.profpay.wallet.ui.components.custom.getBottomPadding
import com.profpay.wallet.ui.shared.sharedPref


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SheetContentForWalletSotsFeature(
    walletId: Long,
    addressList: List<AddressWithTokens>,
    goToWalletAddress: () -> Unit,
    viewModel: WalletSotViewModel,
    goToWalletArchivalSots: () -> Unit,
) {
    val sharedPref = sharedPref()
    val bottomPadding = getBottomPadding()
    val tokenName = sharedPref.getString("token_name", TokenName.USDT.tokenName)

    val listColors = listOf(
        HexagonColor1,
        HexagonColor2,
        HexagonColor3,
        HexagonColor4,
        HexagonColor5,
        HexagonColor6,
        HexagonColor7
    )

    val sortedAddresses = remember(addressList) {
        addressList
            .filter { it.addressEntity.sotIndex >= 0 }
            .sortedBy { it.addressEntity.sotIndex }
    }

    val onAddressClick = { address: AddressWithTokens ->
        sharedPref.edit {
            putString(PrefKeys.ADDRESS_FOR_WALLET_ADDRESS, address.addressEntity.address)
        }
        goToWalletAddress()
    }

    val onReplaceAddressClick = { address: AddressWithTokens ->
        viewModel.creationOfANewCell(walletId, address.addressEntity)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(sortedAddresses) { index, address ->
            AddressCardForWalletSotsFeature(
                index = index,
                color = listColors[index % listColors.size],
                addressWithTokens = address,
                tokenName = tokenName ?: TokenName.USDT.tokenName,
                onAddressClick = {
                    onAddressClick(address)
                },
                onReplaceAddressClick = {
                    onReplaceAddressClick(address)
                }
            )
        }
        item {
            ArchivalSotsCardFeature(
                text = "Архивные соты",
                onClick = goToWalletArchivalSots,
                bottomPadding = bottomPadding
            )
        }
    }
}
