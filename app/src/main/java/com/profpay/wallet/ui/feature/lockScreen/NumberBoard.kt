package com.profpay.wallet.ui.feature.lockScreen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumberBoard(
    inputPinCode: List<Int>,
    goingBack: Boolean,
    onNumberClick: (num: String) -> Unit,
    onClickBiom: () -> Unit = {},
    isCreateLockScreen: Boolean = false,
) {
    val list = (1..9).map { it.toString() }.toMutableList()
    if (goingBack) {
        list.addAll(mutableListOf("<"))
    } else {
        list.addAll(mutableListOf(""))
    }
    list.addAll(mutableListOf("0"))
    if (inputPinCode.isEmpty() && !isCreateLockScreen) {
        list.addAll(mutableListOf("-1"))
    }
    if (inputPinCode.isNotEmpty()) {
        list.addAll(mutableListOf("X"))
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding =
            PaddingValues(
                start = 12.dp,
                top = 16.dp,
                end = 12.dp,
                bottom = 16.dp,
            ),
        content = {
            itemsIndexed(items = list) { _, item ->
                NumberButton(
                    modifier = Modifier,
                    number = item,
                    onClick = { onNumberClick(it) },
                    onClickBiom = { onClickBiom() },
                )
            }
        },
    )
}
