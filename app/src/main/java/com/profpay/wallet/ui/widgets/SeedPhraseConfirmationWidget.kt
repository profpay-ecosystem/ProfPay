package com.profpay.wallet.ui.widgets

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.profpay.wallet.tron.AddressGenerateResult
import com.profpay.wallet.ui.app.theme.BackgroundDark
import com.profpay.wallet.ui.app.theme.PubAddressDark
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.BottomButtonsForCoRFeature
import com.profpay.wallet.ui.screens.createOrRecoveryWallet.TitleCreateOrRecoveryWalletFeature
import kotlin.random.Random

@Composable
fun SeedPhraseConfirmationWidget(
    addressGenerateResult: AddressGenerateResult,
    goToBack: () -> Unit,
    goToWalletAdded: () -> Unit,
) {
    var allowGoToNext by remember { mutableStateOf(false) }
    val listCharArray = addressGenerateResult.mnemonic.words
    val listGroupAndIndex = selectRandomIndices(listCharArray)
    val inputListCharAndIndex: MutableList<Boolean> = mutableListOf(false, false, false, false)
    TitleCreateOrRecoveryWalletFeature(
        title = "Повторите вашу seed-фразу",
        bottomContent = {
            BottomButtonsForCoRFeature(
                goToBack = { goToBack() },
                goToNext = { goToWalletAdded() },
                allowGoToNext = allowGoToNext,
                currentScreen = 2,
                quantityScreens = 2,
            )
        },
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.02f))
        LazyColumn(modifier = Modifier.fillMaxHeight(0.75f)) {
            itemsIndexed(listGroupAndIndex) { index, groupAndIndex ->
                val wordAndIndex = selectWordByIndex(groupAndIndex)
                val allowTrue = String(listCharArray[wordAndIndex.first]) == wordAndIndex.second
                inputListCharAndIndex[index] = allowTrue
                allowGoToNext = inputListCharAndIndex.all { it }
            }
        }
    }
}

fun selectRandomIndices(listMnemonic: List<CharArray>): List<Pair<Int, List<CharArray>>> {
    // Проверка, что массив содержит ровно 12 элементов
    require(listMnemonic.size == 12) { "Список должен содержать ровно 12 слов" }

    // Разделим на 4 группы по 3 элемента
    val groups = listMnemonic.chunked(3)

    // Для каждой группы случайно выбираем индекс в исходном массиве
    return groups.map { group ->
        // Выбираем случайный индекс для группы в диапазоне от 0 до 2, так как в каждой группе 3 элемента
        val randomIndex = Random.nextInt(0, group.size)
        val selectedIndex = listMnemonic.indexOf(group[randomIndex])
        selectedIndex to group.shuffled()
    }
}

@Composable
fun selectWordByIndex(group: Pair<Int, List<CharArray>>): Pair<Int, String> {
    var indexAllowClick by remember { mutableIntStateOf(-1) }
    var selectWord by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Выберите слово №${group.first + 1}",
            style = MaterialTheme.typography.titleSmall,
            color = BackgroundDark,
            modifier = Modifier.padding(start = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            group.second.forEachIndexed { index, item ->
                var colorCont by remember { mutableStateOf(Color.White) }
                colorCont =
                    if (indexAllowClick == index) {
                        PubAddressDark
                    } else {
                        Color.White
                    }
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier =
                        Modifier
                            .weight(0.3f)
                            .padding(4.dp),
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = colorCont,
                        ),
                    onClick = {
                        indexAllowClick = index
                        selectWord = String(item)
                    },
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = String(item),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            color = BackgroundDark,
                            modifier = Modifier.padding(6.dp),
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.size(4.dp))
    }

    return group.first to selectWord
}
