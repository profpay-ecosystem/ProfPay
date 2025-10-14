package com.profpay.wallet.ui.widgets

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.profpay.wallet.R
import com.profpay.wallet.tron.AddressGenerateResult
import com.profpay.wallet.ui.app.theme.BackgroundDark
import com.profpay.wallet.ui.app.theme.DarkBlue
import com.profpay.wallet.ui.app.theme.RedColor
import com.profpay.wallet.ui.feature.createOrRecoveryWallet.bottomSheetAttentionWhenSavingMnemonic
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.BottomButtonsForCoRFeature
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.TitleCreateOrRecoveryWalletFeature
import kotlinx.coroutines.launch

@Composable
fun CreateNewWalletWidget(
    addressGenerateResult: AddressGenerateResult,
    goToBack: () -> Unit,
    goToSeedPhraseConfirmation: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val charMnemonic: CharArray = addressGenerateResult.mnemonic.chars
    val (isOpenAWSMS, setIsOpenAttentionWhenSavingMnemonicSheet) = bottomSheetAttentionWhenSavingMnemonic()
    var allowGoToNext by remember { mutableStateOf(false) }
    if (isOpenAWSMS && !allowGoToNext) {
        allowGoToNext = true
    }

    TitleCreateOrRecoveryWalletFeature(
        title = "Запишите вашу seed-фразу",
        bottomContent = {
            BottomButtonsForCoRFeature(
                goToBack = { goToBack() },
                goToNext = { goToSeedPhraseConfirmation() },
                allowGoToNext = allowGoToNext,
                currentScreen = 1,
                quantityScreens = 2,
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(0.75f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val listCharArray = addressGenerateResult.mnemonic.words
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(5f),
            ) {
                itemsIndexed(listCharArray) { index, item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier =
                            Modifier
                                .padding(4.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = Color.White,
                            ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = String(item),
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                color = BackgroundDark,
                                modifier = Modifier.padding(vertical = 8.dp).padding(start = 8.dp),
                            )

                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 16.dp, end = 4.dp, bottom = 2.dp),
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.size(10.dp)) }
            }
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier,
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipData.newPlainText("Wallet address", String(charMnemonic)).toClipEntry()
                                    )
                                }
                            },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Скопировать в буфер обмена ",
                        style = MaterialTheme.typography.bodyLarge.copy(color = DarkBlue),
                    )
                    Icon(
                        modifier =
                            Modifier
                                .size(18.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                        contentDescription = "",
                        tint = DarkBlue,
                    )
                }
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(top = 4.dp),
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = Color.White,
                        ),
                    onClick = {
                        setIsOpenAttentionWhenSavingMnemonicSheet(true)
                    },
                ) {
                    Row(
                        modifier =
                            Modifier
                                .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_exclamation_mark_in_circle),
                                contentDescription = "",
                                tint = RedColor,
                                modifier =
                                    Modifier
                                        .fillMaxSize(0.5f)
                                        .weight(1f),
                            )
                            Text(
                                text = "На что стоит обратить внимание при сохранении сид-фразы",
                                style = MaterialTheme.typography.bodySmall,
                                color = RedColor,
                                modifier = Modifier.weight(5f),
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                            contentDescription = "",
                            tint = RedColor,
                        )
                    }
                }
            }
        }
    }
}
