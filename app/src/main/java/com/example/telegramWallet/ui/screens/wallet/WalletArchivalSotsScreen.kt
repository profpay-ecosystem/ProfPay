package com.example.telegramWallet.ui.screens.wallet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.telegramWallet.PrefKeys
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.dto.TokenName
import com.example.telegramWallet.bridge.view_model.wallet.walletSot.WalletArchivalSotViewModel
import com.example.telegramWallet.data.database.models.AddressWithTokens
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.ui.app.theme.BackgroundIcon
import com.example.telegramWallet.ui.shared.sharedPref
import kotlinx.coroutines.launch
import java.math.BigInteger


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WalletArchivalSotsScreen(
    goToBack: () -> Unit,
    goToWalletAddress: () -> Unit,
    viewModel: WalletArchivalSotViewModel = hiltViewModel()
) {

    val coroutineScope = rememberCoroutineScope()
    val sharedPref = sharedPref()

    val walletId = sharedPref.getLong("wallet_id", 1)
    val token = sharedPref.getString("token_name", TokenName.USDT.tokenName) ?: TokenName.USDT.tokenName

    val addressWithTokensArchival by viewModel.getAddressWithTokensArchivalByBlockchainLD(
        walletId = walletId, blockchainName = TokenName.valueOf(token).blockchainName
    ).observeAsState(emptyList())

    val bottomPadding = sharedPref().getFloat("bottomPadding", 54f)

    Scaffold(modifier = Modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painterResource(id = R.drawable.wallet_background),
                    contentScale = ContentScale.FillBounds
                ), verticalArrangement = Arrangement.Bottom
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Wallet Archival Sots",
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color.White)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    run {
                        IconButton(onClick = { goToBack() }) {
                            Icon(
                                modifier = Modifier.size(34.dp),
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    run {
                        IconButton(onClick = { /*goToBack()*/ }) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_alert),
                                contentDescription = "",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
            Card(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(
                        RoundedCornerShape(15.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,

                    ) {
                    Text(text = "Архив сот", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Список ваших замененных адресов сот.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 0.dp
                        )
                    )
                    .weight(0.8f),
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = bottomPadding.dp)
                        .padding(vertical = 8.dp, horizontal = 0.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val titles = listOf("All", "With funds")
                    val pagerState = rememberPagerState(pageCount = { titles.size })
                    TabRow(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = {}
                    ) {
                        titles.forEachIndexed { index, title ->
                            Tab(
                                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedContentColor = BackgroundIcon,
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.scrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 2,
                                    )
                                }
                            )
                        }
                    }
                    HorizontalPager(
                        state = pagerState, modifier = Modifier
                            .fillMaxWidth()

                    ) { page ->
                        when (page) {
                            0 -> {
                                if (addressWithTokensArchival.isNotEmpty()) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(
                                                horizontal = 16.dp
                                            ),
                                        contentPadding = PaddingValues(
                                            horizontal = 0.dp,
                                            vertical = 0.dp
                                        ),
                                    ) {
                                        items(addressWithTokensArchival) { addressWithTokens ->
                                            CardArchivalAddress(
                                                goToWalletAddress = { goToWalletAddress() },
                                                addressWithTokens = addressWithTokens
                                            )
                                        }
                                        item { Spacer(modifier = Modifier.size(10.dp)) }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "У вас пока нет архивных сот...",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BackgroundIcon
                                        )
                                        Spacer(modifier = Modifier.size(10.dp))
                                    }
                                }
                            }

                            1 -> {
                                val addressWTAWithFunds = viewModel.getAddressesWTAWithFunds(
                                    addressWithTokensArchival,
                                    token
                                )
                                if (addressWTAWithFunds.isNotEmpty()) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(
                                                horizontal = 16.dp
                                            ),
                                        contentPadding = PaddingValues(
                                            horizontal = 0.dp,
                                            vertical = 0.dp
                                        ),
                                    ) {
                                        items(addressWTAWithFunds) { addressWithTokens ->
                                            CardArchivalAddress(
                                                goToWalletAddress = { goToWalletAddress() },
                                                addressWithTokens = addressWithTokens
                                            )
                                        }
                                        item { Spacer(modifier = Modifier.size(100.dp)) }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Нет архивных сот \n с средствами...",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = BackgroundIcon,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.size(100.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardArchivalAddress(goToWalletAddress: () -> Unit, addressWithTokens: AddressWithTokens) {
    val sharedPref = sharedPref()

    val tokenName = sharedPref.getString("token_name", TokenName.USDT.tokenName)
    val tokenEntity = addressWithTokens.tokens.stream()
        .filter { currentToken -> currentToken.token.tokenName == tokenName }
        .findFirst()
        .orElse(null)

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        elevation = CardDefaults.cardElevation(10.dp),
        onClick = {
            sharedPref.edit() {
                putString(PrefKeys.ADDRESS_FOR_WALLET_ADDRESS, addressWithTokens.addressEntity.address)
            }
            goToWalletAddress()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Column(modifier = Modifier.padding(horizontal = 12.dp, 8.dp)) {
                    Text(
                        text = "${addressWithTokens.addressEntity.address.take(7)}..." +
                                "${addressWithTokens.addressEntity.address.takeLast(7)} ",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    val balanceWF = tokenEntity?.balanceWithoutFrozen ?: BigInteger.ZERO
                    if (balanceWF > BigInteger.ZERO) {
                        Text(
                            text = "$${balanceWF.toTokenAmount()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

        }
    }

}