package com.profpay.wallet.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.R
import com.profpay.wallet.bridge.viewmodel.dto.TokenName
import com.profpay.wallet.bridge.viewmodel.wallet.walletSot.WalletSotViewModel
import com.profpay.wallet.ui.components.custom.getBottomPadding
import com.profpay.wallet.ui.feature.wallet.walletSots.HexagonsFeature
import com.profpay.wallet.ui.feature.wallet.walletSots.SheetContentForWalletSotsFeature
import com.profpay.wallet.ui.shared.sharedPref

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSotsScreen(
    goToBack: () -> Unit,
    goToReceive: () -> Unit,
    goToWalletAddress: () -> Unit,
    goToWalletArchivalSots: () -> Unit,
    viewModel: WalletSotViewModel = hiltViewModel(),
) {
    val sheetState =
        rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            confirmValueChange = { newState ->
                newState != SheetValue.Hidden
            },
            skipHiddenState = true,
        )

    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    val walletId = sharedPref().getLong("wallet_id", 1)

    val token = sharedPref().getString("token_name", TokenName.USDT.tokenName)

    val addressWithTokens by viewModel
        .getAddressesSotsWithTokensByBlockchain(
            walletId = walletId,
            blockchainName = TokenName.valueOf(token!!).blockchainName,
        ).observeAsState(emptyList())

    val bottomPadding = getBottomPadding()

    BottomSheetScaffold(
        modifier = Modifier.padding(bottom = bottomPadding.dp),
        sheetDragHandle = {
            Box(
                modifier =
                    Modifier
                        .padding(
                            horizontal = 160.dp,
                            vertical = 7.dp,
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.onPrimary)
                        .size(width = 90.dp, height = 5.dp),
            )
        },
        sheetShape = RoundedCornerShape(20.dp),
        scaffoldState = scaffoldState,
        sheetContent = {
            SheetContentForWalletSotsFeature(
                walletId = walletId,
                addressList = addressWithTokens,
                goToWalletAddress = { goToWalletAddress() },
                viewModel = viewModel,
                goToWalletArchivalSots = { goToWalletArchivalSots() },
            )
        },
        sheetPeekHeight = 435.dp,
    ) { _ ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .paint(
                        painterResource(id = R.drawable.wallet_background),
                        contentScale = ContentScale.FillBounds,
                    ),
            contentAlignment = Alignment.TopCenter,
        ) {
            HexagonsFeature(
                goToBack = goToBack,
                goToReceive = goToReceive,
                addressList = addressWithTokens,
                size = with(LocalDensity.current) { sheetState.requireOffset().toDp() * 1.1f },
            )
        }
    }
}
