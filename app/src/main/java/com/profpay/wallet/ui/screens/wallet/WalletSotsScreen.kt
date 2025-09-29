package com.profpay.wallet.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.R
import com.profpay.wallet.bridge.view_model.dto.TokenName
import com.profpay.wallet.bridge.view_model.wallet.walletSot.WalletSotViewModel
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.BackgroundIcon2
import com.profpay.wallet.ui.feature.wallet.HexagonShape
import com.profpay.wallet.ui.feature.wallet.HexagonsFeature
import com.profpay.wallet.ui.shared.sharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.util.stream.Collectors

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
        .getAddressesSotsWithTokensByBlockchainLD(
            walletId = walletId,
            blockchainName = TokenName.valueOf(token!!).blockchainName,
        ).observeAsState(emptyList())

    val bottomPadding = sharedPref().getFloat("bottomPadding", 54f)

    BottomSheetScaffold(
        modifier = Modifier.padding(bottom = bottomPadding.dp),
        sheetDragHandle = {
            Box(
                modifier =
                    Modifier
                        .padding(
                            horizontal = 160.dp,
                            vertical = 7.dp,
                        ).clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.onPrimary)
                        .size(width = 90.dp, height = 5.dp),
            )
        },
        sheetShape = RoundedCornerShape(20.dp),
        scaffoldState = scaffoldState,
        sheetContent = {
            SheetContent(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SheetContent(
    walletId: Long,
    addressList: List<AddressWithTokens>,
    goToWalletAddress: () -> Unit,
    viewModel: WalletSotViewModel,
    goToWalletArchivalSots: () -> Unit,
) {
    val bottomPadding = sharedPref().getFloat("bottomPadding", 54f)
    val sharedPref = sharedPref()
    val tokenName = sharedPref.getString("token_name", TokenName.USDT.tokenName)

    val listColors: List<Color> =
        listOf(
            Color(0xFF6A0E8D),
            Color(0xFF29A512),
            Color(0xFFFFA200),
            Color(0xFF0019FF),
            Color(0xFF00FE00),
            Color(0xFFF6DA00),
            Color(0xFF0099D7),
        )
    Column(modifier = Modifier) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.Bottom,
        ) {
            LazyColumn(
                modifier = Modifier,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(
                    addressList
                        .stream()
                        .filter { it.addressEntity.sotIndex >= 0 }
                        .sorted(Comparator.comparingInt { it.addressEntity.sotIndex.toInt() })
                        .collect(Collectors.toList()),
                ) { index, address ->
                    val tokenEntity =
                        address.tokens
                            .stream()
                            .filter { currentToken -> currentToken.token.tokenName == tokenName }
                            .findFirst()
                            .orElse(null)

                    var expandedDropdownMenu by remember { mutableStateOf(false) }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                        elevation = CardDefaults.cardElevation(10.dp),
                        onClick = {
                            sharedPref.edit {
                                putString(PrefKeys.ADDRESS_FOR_WALLET_ADDRESS, address.addressEntity.address)
                            }
                            goToWalletAddress()
                        },
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .padding(start = 8.dp)
                                        .padding(vertical = 8.dp)
                                        .fillMaxHeight(),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .clip(HexagonShape(true))
                                                .border(2.dp, listColors[index], HexagonShape(true))
                                                .size(40.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text(
                                            text =
                                                "${address.addressEntity.address.take(7)}..." +
                                                    "${address.addressEntity.address.takeLast(7)} ",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                        Text(
                                            text =
                                                if (tokenName == "USDT") {
                                                    "$${(tokenEntity?.balanceWithoutFrozen ?: BigInteger.ZERO).toTokenAmount()}"
                                                } else {
                                                    "${(tokenEntity?.balanceWithoutFrozen ?: BigInteger.ZERO).toTokenAmount()} TRX"
                                                },
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                    }
                                }
                            }
                            Column(
                                modifier =
                                    Modifier
                                        .padding()
                                        .fillMaxHeight(),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                IconButton(onClick = {
                                    expandedDropdownMenu = !expandedDropdownMenu
                                }) {
                                    Icon(
                                        modifier = Modifier.size(25.dp),
                                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_more_vert),
                                        contentDescription = "",
                                        tint = BackgroundIcon2,
                                    )
                                }
                                DropdownMenu(
                                    expanded = expandedDropdownMenu,
                                    onDismissRequest = { expandedDropdownMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                        },
                                        text = {
                                            Text(
                                                "Получить AML",
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        },
                                    )
                                    if (address.addressEntity.sotDerivationIndex != 0) {
                                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                                        DropdownMenuItem(
                                            modifier = Modifier.height(IntrinsicSize.Min),
                                            onClick = {
                                                viewModel.viewModelScope.launch {
                                                    withContext(Dispatchers.IO) {
                                                        viewModel.creationOfANewCell(
                                                            walletId,
                                                            address.addressEntity,
                                                        )
                                                    }
                                                    expandedDropdownMenu = !expandedDropdownMenu
                                                }
                                            },
                                            text = {
                                                Text(
                                                    "Заменить адрес",
                                                    style = MaterialTheme.typography.labelLarge,
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                        elevation = CardDefaults.cardElevation(10.dp),
                        onClick = {
                            goToWalletArchivalSots()
                        },
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(text = "Архивные соты", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.padding(bottom = (bottomPadding + 8).dp))
                }
            }
        }
    }
}
