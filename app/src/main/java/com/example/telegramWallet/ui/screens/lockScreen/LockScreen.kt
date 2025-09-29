package com.example.telegramWallet.ui.screens.lockScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.pin_lock.PinLockViewModel
import com.example.telegramWallet.ui.feature.lockScreen.InputDots
import com.example.telegramWallet.ui.feature.lockScreen.NumberBoard
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    toNavigate: () -> Unit,
    viewModel: PinLockViewModel = hiltViewModel(),
    goToBack: () -> Unit = {},
    goingBack: Boolean = false,
) {
    val inputPinCode = remember { mutableStateListOf<Int>() }
    var isError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFFff2a00),
                        contentColor = Color.White,
                    )
                },
            )
        },
    ) { padding ->
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(modifier = Modifier.fillMaxWidth(0.1f))

                Icon(
                    painter = painterResource(id = R.drawable.icon_smart),
                    contentDescription = "",
                    modifier =
                        Modifier
                            .fillMaxSize(0.5f)
                            .clip(CircleShape)
                            .weight(1f),
                )
                Spacer(modifier = Modifier.fillMaxHeight(0.02f))
                Text(text = "Hii, User!")
                Text(text = "Verify 4-digit security PIN")

                Spacer(modifier = Modifier.fillMaxHeight(0.02f))

                InputDots(
                    inputPinCode,
                    isError,
                    onErrorReset = { isError = false },
                )

                NumberBoard(
                    inputPinCode = inputPinCode,
                    goingBack = goingBack,
                    onNumberClick = { enterNumber ->
                        when (enterNumber) {
                            "" -> {}
                            "<" -> {
                                goToBack()
                            }
                            "-1" -> {}
                            "X" -> {
                                if (inputPinCode.isNotEmpty()) inputPinCode.removeAt(inputPinCode.lastIndex)
                            }

                            else -> {
                                if (inputPinCode.size == 1) isError = false

                                if (inputPinCode.size < 4) {
                                    inputPinCode.add(enterNumber.toInt())
                                }
                            }
                        }
                    },
                    onClickBiom = {
                        viewModel.unlockSession()
                        toNavigate()
                    },
                )

                Spacer(modifier = Modifier.weight(0.1f))

                LaunchedEffect(inputPinCode.size) {
                    if (inputPinCode.size == 4) {
                        delay(250)

                        val inputPinCodeInt = inputPinCode.joinToString(separator = "").toInt()
                        viewModel.validatePin(inputPinCodeInt.toString()) { isCorrect ->
                            if (isCorrect) {
                                viewModel.unlockSession()
                                toNavigate()
                            } else {
                                isError = true
                                inputPinCode.clear()
                            }
                        }
                    }
                }
            }
        }
    }
}
