package com.example.telegramWallet.ui.feature.wallet.walletInfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.dto.TokenName
import com.example.telegramWallet.data.database.entities.wallet.TokenEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetChoiceTokenToSend(
    listTokensWithTotalBalance: List<TokenEntity?>,
    goToSendWalletInfo: (addressId: Long, tokenName: String) -> Unit,
): Pair<Boolean, (Boolean) -> Unit> {
    val coroutineScope = rememberCoroutineScope()
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true },
        )
    val (isOpenBottomSheet, setIsOpenBottomSheet) = remember { mutableStateOf(false) }

    if (isOpenBottomSheet) {
        ModalBottomSheet(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { Box(modifier = Modifier) },
            modifier = Modifier.fillMaxHeight(0.5f),
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    delay(400)
                    setIsOpenBottomSheet(false)
                }
            },
            sheetState = sheetState,
        ) {
            Row(
                modifier =
                    Modifier
                        .padding(
                            top = 16.dp,
                        ).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Отправить",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            if (listTokensWithTotalBalance.any { it!!.balance != BigInteger.ZERO }) {
                LazyColumn(
                    modifier =
                        Modifier
                            .padding(
                                vertical = 4.dp,
                                horizontal = 8.dp,
                            ).fillMaxSize(),
                ) {
                    itemsIndexed(listTokensWithTotalBalance) { _, tokenEntity ->
                        if (tokenEntity != null && tokenEntity.getBalanceWithoutFrozen() != BigInteger.ZERO) {
                            val currentTokenName =
                                TokenName.entries
                                    .stream()
                                    .filter { it.tokenName == tokenEntity.tokenName }
                                    .findFirst()
                                    .orElse(TokenName.USDT)

                            CardForWalletInfoSendFeature(
                                onClick = {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        delay(400)
                                        setIsOpenBottomSheet(false)
                                    }

                                    goToSendWalletInfo(tokenEntity.addressId, tokenEntity.tokenName)
                                },
                                paintIconId = currentTokenName.paintIconId,
                                label = currentTokenName.tokenName,
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.size(10.dp)) }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        modifier = Modifier.size(80.dp).padding(bottom = 16.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_search_to_file),
                        contentDescription = "NotSearch",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = "Активы не найдены",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
    return isOpenBottomSheet to { setIsOpenBottomSheet(it) }
}
