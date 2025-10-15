package com.profpay.wallet.ui.feature.wallet.walletAddress

import StackedSnakbarHostState
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.R
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.models.TokenWithPendingTransactions
import com.profpay.wallet.ui.components.custom.getBottomPadding
import com.profpay.wallet.ui.widgets.dialog.AlertDialogWidget
import java.math.BigInteger

@Composable
fun ActionsRowWalletAddressFeature(
    tokenEntity: TokenWithPendingTransactions?,
    addressWithTokens: AddressWithTokens?,
    isActivated: Boolean,
    stackedSnackbarHostState: StackedSnakbarHostState,
    goToSendWalletAddress: (Long, String) -> Unit,
    goToSystemTRX: () -> Unit,
    goToReceive: () -> Unit,
    sharedPref: SharedPreferences,
    address: String,
    tokenName: String,
    setIsOpenRejectReceiptSheet: (Boolean) -> Unit,
) {
    var openDialog by remember { mutableStateOf(false) }

    val bottomPadding = getBottomPadding()

    val isGeneral = addressWithTokens?.addressEntity?.isGeneralAddress ?: false
    val onClickSend = {
        if (isGeneral) {
            addressWithTokens.addressEntity.addressId?.let {
                goToSendWalletAddress(it, tokenName)
            }
        } else {
            if (!isActivated) {
                stackedSnackbarHostState.showErrorSnackbar(
                    title = "Перевод валюты невозможен",
                    description = "Для перевода необходимо активировать адрес, отправив 1 TRX.",
                    actionTitle = "Перейти",
                    action = { goToSystemTRX() }
                )
            } else {
                setIsOpenRejectReceiptSheet(true)
            }
        }
    }
    val onClickReceive = {
        if (addressWithTokens?.addressEntity?.isGeneralAddress == true) {
            openDialog = true
        } else {
            sharedPref.edit {
                putString(PrefKeys.ADDRESS_FOR_RECEIVE, address)
            }
            goToReceive()
        }
    }
    Column(
        modifier =
            Modifier
                .padding(bottom = bottomPadding.dp)
                .fillMaxSize()
                .background(Color.Transparent),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Row(
            modifier =
                Modifier
                    .height(IntrinsicSize.Min)
                    .padding(8.dp)
                    .fillMaxWidth()
                    .background(Color.Transparent),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // --- Кнопка "Отправить"
            if (tokenEntity?.balanceWithoutFrozen?.let { it > BigInteger.ZERO } == true) {
                ActionCardWalletAddressFeature(
                    text = "Отправить",
                    icon = R.drawable.icon_send,
                    modifier = Modifier.weight(0.5f),
                    onClick = {
                        onClickSend()
                    }
                )
            }

            // --- Кнопка "Пополнить"
            ActionCardWalletAddressFeature(
                text = "Пополнить",
                icon = R.drawable.icon_get,
                modifier = Modifier.weight(0.5f),
                onClick = {
                    onClickReceive()
                }
            )
        }
    }

    // --- Диалог
    if (openDialog) {
        AlertDialogWidget(
            onConfirmation = {
                sharedPref.edit {
                    putString(PrefKeys.ADDRESS_FOR_RECEIVE, address)
                }
                goToReceive()
                openDialog = false
            },
            onDismissRequest = { openDialog = false },
            dialogTitle = "Главный адрес",
            dialogText = """
                Пополнение главной соты не рекомендуется.
                Вместо этого скопируйте любую доп-соту и пополните её. 
                После AML проверки вы сможете перевести валюту на центральную соту, 
                так ваш центральный адрес будет чист всегда.
            """.trimIndent(),
            textConfirmButton = "Всё-равно продолжить",
            textDismissButton = "Закрыть",
        )
    }
}
