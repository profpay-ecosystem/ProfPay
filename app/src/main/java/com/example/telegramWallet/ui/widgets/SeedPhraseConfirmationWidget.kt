package com.example.telegramWallet.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.telegramWallet.tron.AddressGenerateResult
import com.example.telegramWallet.ui.app.theme.BackgroundDark
import com.example.telegramWallet.ui.app.theme.PubAddressDark
import com.example.telegramWallet.ui.screens.createOrRecoveryWallet.BottomButtonsForCoRFeature
import com.example.telegramWallet.ui.screens.createOrRecoveryWallet.TitleCreateOrRecoveryWalletFeature
import com.example.telegramWallet.ui.shared.utils.checkingValuesInList
import kotlin.random.Random

// about erupt space can lumber noise clean air across timber easy ridge

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
                var allowTrue = String(listCharArray[wordAndIndex.first]) == wordAndIndex.second
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

@Deprecated("Система для выбора порядка всех 12 слов")
@Composable
fun SeedPhraseInputWidget(
    addressGenerateResult: AddressGenerateResult,
    allowGoToNext1: Boolean,
) {
    var allowGoToNext = allowGoToNext1
    var seedPhraseInput by remember { mutableStateOf("") }
    val seedPhrase = String(addressGenerateResult.mnemonic.chars).plus(" ")
    val listCharArray = addressGenerateResult.mnemonic.words
    val listCharArrayShuffled = listCharArray.shuffled()
    Column(
        modifier = Modifier.fillMaxHeight(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(90.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = Color.White,
                ),
        ) {
            TextField(
                value = seedPhraseInput,
                modifier =
                    Modifier
                        .height(90.dp)
                        .fillMaxWidth(),
                readOnly = true,
                shape = MaterialTheme.shapes.small.copy(),
                onValueChange = { seedPhraseInput = it },
                trailingIcon = {},
                colors =
                    TextFieldDefaults.colors(
                        focusedTextColor = BackgroundDark,
                        unfocusedTextColor = BackgroundDark,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = BackgroundDark,
                        selectionColors =
                            TextSelectionColors(
                                handleColor = BackgroundDark,
                                backgroundColor = Color.Transparent,
                            ),
                    ),
            )
        }

        Spacer(modifier = Modifier.size(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.padding(),
        ) {
            itemsIndexed(listCharArrayShuffled) { index, item ->
                var colorCont by remember { mutableStateOf(Color.White) }
                var isAllowClick by remember { mutableStateOf(true) }
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier =
                        Modifier
                            .width(IntrinsicSize.Max)
                            .padding(4.dp),
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = colorCont,
                        ),
                    onClick = {
                        if (isAllowClick) {
                            colorCont = PubAddressDark
                            seedPhraseInput += String(item) + " "
                            isAllowClick = false
                        } else {
                            seedPhraseInput = seedPhraseInput.replace(String(item).plus(" "), "")
                            isAllowClick = true
                            colorCont = Color.White
                        }
                    },
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(IntrinsicSize.Max)
                                .fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = String(item),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = BackgroundDark,
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.size(20.dp)) }
        }
        if (seedPhrase.equals(seedPhraseInput)) {
            allowGoToNext = true
        } else {
            allowGoToNext = false
        }
    }
}

@Deprecated("")
@Composable
fun CheckingTheUsersSeedPhrase(
    listMnemonic: List<Any>,
    listIndex: List<Int>,
    goToWalletAdded: () -> Unit,
) {
    var value0 by remember { mutableStateOf("") }
    var value1 by remember { mutableStateOf("") }
    var value2 by remember { mutableStateOf("") }

    // Получаем remember по индексу
    fun getValueFromIndex(current: Int): String =
        when (current) {
            0 -> value0
            1 -> value1
            else -> value2
        }
    // Поля ввода элемментов сид-фразы
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        modifier =
            Modifier
                .alpha(0.8f),
    ) {
        itemsIndexed(listIndex) { index, item ->
            Row {
                OutlinedTextField(
                    value = getValueFromIndex(index),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                    label = { Text(text = "# ${item + 1}") },
                    onValueChange = {
                        // Устанавливаем значение каждому TextField-у
                        when (index) {
                            0 -> value0 = it
                            1 -> value1 = it
                            else -> value2 = it
                        }
                    },
                )
            }
        }
    }
    val gettingValue = listOf(value0, value1, value2)
    // Проверка на соответствие введённых элементов сид-фразы

    val checkingSeedP =
        checkingValuesInList(gettingValue, listIndex, listMnemonic as List<CharArray>)
    if (checkingSeedP) {
        Row {
            Button(
                onClick = { goToWalletAdded() },
                modifier =
                    Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(fraction = 1f),
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(text = "Продолжить")
            }
        }
    }
}
