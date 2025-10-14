package com.profpay.wallet.ui.screens.createOrRecoveryWallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.bridge.view_model.create_or_recovery_wallet.RecoverUiEvent
import com.profpay.wallet.bridge.view_model.create_or_recovery_wallet.RecoverWalletState
import com.profpay.wallet.bridge.view_model.create_or_recovery_wallet.RecoverWalletViewModel
import com.profpay.wallet.data.flow_db.repo.RecoveryResult
import com.profpay.wallet.ui.app.theme.BackgroundContainerButtonDark
import com.profpay.wallet.ui.app.theme.BackgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.BackgroundDark
import com.profpay.wallet.ui.app.theme.BackgroundIcon
import com.profpay.wallet.ui.app.theme.IndicatorGreen
import com.profpay.wallet.ui.widgets.dialog.AlertDialogConfButtonWidget
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun RecoverWalletScreen(
    goToRecoveringWalletAdding: () -> Unit,
    viewModel: RecoverWalletViewModel = hiltViewModel(),
    goToBack: () -> Unit,
) {
    var seedPhrase by remember { mutableStateOf("") }
    val openDialog = remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiEvent by viewModel.uiEvent.collectAsStateWithLifecycle()

    // Текущая клавиатура
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiEvent) {
        when (uiEvent) {
            is RecoverUiEvent.Success -> {
                delay(500L)
                openDialog.value = true
            }
            is RecoverUiEvent.Error -> Unit
            else -> Unit
        }
    }

    @Composable
    fun AlertDialogForCoRFeature(dialogText: String) {
        if (openDialog.value) {
            AlertDialogConfButtonWidget(
                onDismissRequest = {
                    openDialog.value = !openDialog.value
                },
                onConfirmation = {
                    openDialog.value = !openDialog.value
                },
                dialogTitle = "Восстановить кошелёк",
                dialogText = dialogText,
                icon = Icons.Default.Create,
                textButton = "Вернуться",
            )
        }
    }

    Scaffold { padding ->
        padding
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clickable { keyboardController?.hide() },
        ) {}
        TitleCreateOrRecoveryWalletFeature(
            title = "Вставьте вашу seed- фразу",
            bottomContent = {
                BottomButtonsForCoRFeature(
                    goToBack = { goToBack() },
                    goToNext = {
                        viewModel.recoverWallet(seedPhrase)
                    },
                    allowGoToNext = true,
                    currentScreen = 1,
                    quantityScreens = 1,
                )
            },
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(0.7f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.fillMaxHeight(0.1f))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier,
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = Color.White,
                        ),
                ) {
                    TextField(
                        value = seedPhrase,
                        modifier =
                            Modifier
                                .height(IntrinsicSize.Min)
                                .fillMaxWidth(),
                        label = {
                            Text(text = "Введите Сид-фразу", style = MaterialTheme.typography.titleSmall)
                        },
                        onValueChange = { seedPhrase = it },
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

                when (state) {
                    is RecoverWalletState.Loading -> Unit
                    is RecoverWalletState.Success -> {
                        val successState = state as RecoverWalletState.Success
                        when (successState.addressRecoverResult) {
                            is RecoveryResult.RepeatingMnemonic -> {
                                AlertDialogForCoRFeature(
                                    dialogText =
                                        "Кошелёк уже есть.\n\n" +
                                            "Кошелёк по данной сид-фразе уже добавлен у вас в приложении",
                                )
                            }
                            is RecoveryResult.InvalidMnemonic -> {
                                AlertDialogForCoRFeature(
                                    dialogText =
                                        "Введена некорректная сид-фраза.\n\n" +
                                            "Пожалуйста перепроверьте правильность написания сид-фразы, " +
                                            "и попробуйте снова.",
                                )
                            }
                            is RecoveryResult.AddressNotFound -> Unit
                            is RecoveryResult.Error -> {
                                val throwable = successState.addressRecoverResult.throwable
                                AlertDialogForCoRFeature(
                                    dialogText =
                                        "Произошла ошибка!\n\n" +
                                            throwable +
                                            "\nПожалуйста попробуйте позднее.",
                                )
                            }
                            is RecoveryResult.Success -> {
                                goToRecoveringWalletAdding()
                            }

                            is RecoveryResult.Empty -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomButtonsForCoRFeature(
    goToBack: () -> Unit,
    goToNext: () -> Unit,
    allowGoToNext: Boolean,
    currentScreen: Int,
    quantityScreens: Int,
) {
    val colorGoToNext: Color =
        if (allowGoToNext) {
            BackgroundContainerButtonDark
        } else {
            Color.White
        }
    Row(modifier = Modifier.fillMaxWidth(0.9f), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(0.33f), contentAlignment = Alignment.CenterStart) {
            IconButton(
                onClick = { goToBack() },
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(BackgroundContainerButtonLight)
                        .size(width = 70.dp, height = 30.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "",
                    tint = BackgroundContainerButtonDark,
                )
            }
        }
        Row(
            modifier = Modifier.weight(0.33f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            for (i in 1..quantityScreens) {
                val color =
                    if (i == currentScreen) {
                        IndicatorGreen
                    } else {
                        BackgroundIcon
                    }
                Box(
                    modifier =
                        Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(10.dp),
                )
            }
        }
        Box(modifier = Modifier.weight(0.33f), contentAlignment = Alignment.CenterEnd) {
            IconButton(
                onClick = { if (allowGoToNext) goToNext() },
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(BackgroundContainerButtonLight)
                        .size(width = 70.dp, height = 30.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "",
                    tint = colorGoToNext,
                )
            }
        }
    }
}
