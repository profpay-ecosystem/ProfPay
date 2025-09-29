package com.example.telegramWallet.ui.feature.createOrRecoveryWallet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegramWallet.R
import com.example.telegramWallet.ui.app.theme.BackgroundDark
import com.example.telegramWallet.ui.app.theme.BackgroundLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetAttentionWhenSavingMnemonic(): Pair<Boolean, (Boolean) -> Unit> {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true },
        )

    val coroutineScope = rememberCoroutineScope()

    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }

    if (isOpenSheet) {
        ModalBottomSheet(
            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            dragHandle = { Box(modifier = Modifier) },
            modifier = Modifier.height(IntrinsicSize.Max),
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
                        .fillMaxSize()
                        .background(color = BackgroundDark),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.icon_lockpad),
                    contentDescription = "",
                    tint = BackgroundLight,
                    modifier = Modifier.fillMaxSize(0.3f),
                )
                Row(
                    modifier =
                        Modifier.padding(
                            start = 16.dp,
                            bottom = 16.dp,
                            end = 16.dp,
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Никому не сообщайте вашу seed-фразу",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BackgroundLight,
                        textAlign = TextAlign.Center,
                    )
                }

                CardForAttentionWhenSavingMnemonic(1, "Мы не получаем доступа к вашей seed-фразе")
                CardForAttentionWhenSavingMnemonic(2, "Сохранять копию seed-фразы в виде скриншота небезопасно")
                CardForAttentionWhenSavingMnemonic(3, "Храните вашу seed-фразу в безопасном, доступном тоько Вам месте")

                Button(
                    onClick = {
                        coroutineScope.launch {
                            sheetState.hide()
                            delay(400)
                            setIsOpenSheet(false)
                        }
                    },
                    modifier =
                        Modifier
                            .padding(vertical = 10.dp, horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(120.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = BackgroundLight,
                            contentColor = BackgroundDark,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Продолжить",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
    return isOpenSheet to { setIsOpenSheet(it) }
}

@Composable
fun CardForAttentionWhenSavingMnemonic(
    index: Int,
    text: String,
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier =
            Modifier
                .padding(vertical = 4.dp, horizontal = 16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = BackgroundDark,
                contentColor = BackgroundLight,
            ),
        border = BorderStroke(1.dp, color = BackgroundLight),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(vertical = 18.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(90f))
                        .padding(end = 16.dp)
                        .size(40.dp)
                        .border(1.dp, color = BackgroundLight, shape = RoundedCornerShape(90f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier.padding(),
                    text = "$index",
                    fontSize = 14.sp,
                    color = BackgroundLight,
                )
            }

            Text(
                text = text,
                fontSize = 14.sp,
                color = BackgroundLight,
                modifier = Modifier,
            )
        }
    }
}
