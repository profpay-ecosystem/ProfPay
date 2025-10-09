package com.profpay.wallet.ui.components.custom

import StackedSnackbarHost
import StackedSnakbarHostState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.profpay.wallet.R
import com.profpay.wallet.ui.shared.sharedPref

@Composable
fun getBottomPadding(): Float {
    val sharedPref = sharedPref()
    val bottomPadding by remember {
        mutableFloatStateOf(sharedPref.getFloat("bottomPadding", 54f))
    }
    return bottomPadding
}


@Composable
fun CustomScaffoldWallet(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(bottomPadding: Float) -> Unit,
) {
    val bottomPadding = getBottomPadding()

    Scaffold(
        modifier = Modifier,
    ) { padding ->
        padding
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .paint(
                        painterResource(id = R.drawable.wallet_background),
                        contentScale = ContentScale.FillBounds,
                    ),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content(bottomPadding)
        }
    }
}

@Composable
fun CustomScaffoldWallet(
    modifier: Modifier = Modifier,
    stackedSnackbarHostState: StackedSnakbarHostState,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
    content: @Composable ColumnScope.(bottomPadding: Float) -> Unit,
) {
    val bottomPadding = getBottomPadding()
    Scaffold(
        modifier = Modifier,
        snackbarHost = {
            StackedSnackbarHost(
                hostState = stackedSnackbarHostState,
                modifier =
                    Modifier
                        .padding(8.dp, (bottomPadding + 50).dp),
            )
        },
    ) { padding ->
        padding
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clickable {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
        ) {}
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .paint(
                        painterResource(id = R.drawable.wallet_background),
                        contentScale = ContentScale.FillBounds,
                    )
                    .clickable {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
            verticalArrangement = Arrangement.Bottom,
        ) {
            content(bottomPadding)
        }
    }
}

@Composable
fun CustomScaffoldWallet(
    modifier: Modifier = Modifier,
    stackedSnackbarHostState: StackedSnakbarHostState,
    content: @Composable ColumnScope.(bottomPadding: Float) -> Unit,
) {
    val bottomPadding = getBottomPadding()
    Scaffold(
        modifier = Modifier,
        snackbarHost = {
            StackedSnackbarHost(
                hostState = stackedSnackbarHostState,
                modifier =
                    Modifier
                        .padding(8.dp, (bottomPadding + 50).dp),
            )
        },
    ) { padding ->
        padding
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .paint(
                        painterResource(id = R.drawable.wallet_background),
                        contentScale = ContentScale.FillBounds,
                    ),
            verticalArrangement = Arrangement.Bottom,
        ) {
            content(bottomPadding)
        }
    }
}

