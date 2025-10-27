package com.profpay.wallet.ui.feature.wallet.send.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profpay.wallet.ui.app.theme.PubAddressDark


@Composable
internal fun AmountForBSTransferConfirmation(
    amount: String,
    amountUSD: String,
    tokenShortName: String,
) {
    Column(
        modifier =
            Modifier
                .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "-$amount $tokenShortName",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Text(
            text = "≈ $amountUSD $",
            style = MaterialTheme.typography.bodyMedium,
            color = PubAddressDark,
        )

    }
}
