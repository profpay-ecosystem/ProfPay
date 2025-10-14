package com.profpay.wallet.ui.feature.wallet.walletSystem

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.R
import com.profpay.wallet.bridge.view_model.wallet.WalletSystemViewModel
import com.profpay.wallet.data.database.dao.wallet.WalletProfileModel
import com.profpay.wallet.ui.app.theme.RedColor
import com.profpay.wallet.ui.widgets.dialog.AlertDialogWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetControlOfTheWallet(
    viewModel: WalletSystemViewModel = hiltViewModel(),
    wallet: WalletProfileModel,
): Pair<Boolean, (Boolean) -> Unit> {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true },
        )
    val coroutineScope = rememberCoroutineScope()
    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }

    val (isOpenSeedPhrase, setIsOpenSeedPhrase) = remember { mutableStateOf(false) }
    val (isOpenConfDeleteWallet, setIsOpenConfDeleteWallet) = remember { mutableStateOf(false) }

    val (seedPhr, setSeedPhr) = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.viewModelScope.launch {
            setSeedPhr(
                viewModel.getSeedPhrase(walletId = wallet.id!!) ?: "",
            )
        }
    }

    if (isOpenSheet) {
        ModalBottomSheet(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { Box(modifier = Modifier) },
            modifier = Modifier.fillMaxHeight(0.7f),
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    delay(400)
                    setIsOpenSheet(false)
                }
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize(),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = "Кошелёк",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                ) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "Название",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                RenamingWalletFeature(
                    walletName = wallet.name,
                    onClick = { newName ->
                        viewModel.updateNameWalletById(wallet.id!!, newName)
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
                        onClick = { setIsOpenSeedPhrase(!isOpenSeedPhrase) },
                    ) {
                        Text(
                            text = "${if (isOpenSeedPhrase) "Скрыть" else "Посмотреть"} сид-фразу",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                    if (isOpenSeedPhrase && seedPhr.isNotEmpty()) {
                        IconButton(onClick = {
                            scope.launch {
                                clipboard.setClipEntry(
                                    ClipData.newPlainText("!!!", seedPhr).toClipEntry()
                                )
                            }
                        }) {
                            Icon(
                                modifier =
                                    Modifier
                                        .size(18.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
                SeedPhraseCardAnimated(
                    seedPhrase = seedPhr,
                    isExpanded = isOpenSeedPhrase,
                )

                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = RedColor),
                    onClick = {
                        setIsOpenConfDeleteWallet(!isOpenConfDeleteWallet)
//                            viewModel.deleteWalletProfile(walletId = wallet.id!!)
                    },
                ) {
                    Text(text = "Удалить кошелёк", style = MaterialTheme.typography.titleSmall)
                }
                if (isOpenConfDeleteWallet) {
                    AlertDialogWidget(
                        onDismissRequest = {
                            setIsOpenConfDeleteWallet(!isOpenConfDeleteWallet)
                        },
                        onConfirmation = {
                            viewModel.deleteWalletProfile(walletId = wallet.id!!)
                            setIsOpenConfDeleteWallet(!isOpenConfDeleteWallet)
                        },
                        dialogTitle = "Удалить кошелёк",
                        isSmallDialogTitle = true,
                        dialogText =
                            "Вы действительно уверены, что хотите удалить кошелек?\n\n" +
                                "Данные и привязанные к нему смарт-контракты будут уничтожены.",
                        textDismissButton = "Назад",
                        textConfirmButton = "Подтвердить",
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    return isOpenSheet to { setIsOpenSheet(it) }
}
