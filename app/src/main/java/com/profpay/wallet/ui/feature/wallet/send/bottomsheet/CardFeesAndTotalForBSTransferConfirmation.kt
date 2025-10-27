package com.profpay.wallet.ui.feature.wallet.send.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.PubAddressDark
import com.profpay.wallet.utils.decimalFormat
import java.math.BigDecimal
import java.math.BigInteger


@Composable
fun CardFeesAndTotalForBSTransferConfirmation(
    commission: BigDecimal,
    trxToUsdtRate: BigDecimal,
    isActivated: Boolean,
    createNewAccountFee: BigInteger,
    totalAmount: BigDecimal,
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)) {
            FeeRow(
                label = "Комиссия",
                amount = commission,
                currency = "TRX",
                trxToUsdtRate = trxToUsdtRate
            )

            if (!isActivated) {
                FeeRow(
                    label = "Активация адреса",
                    amount = createNewAccountFee.toTokenAmount(),
                    currency = "TRX",
                    trxToUsdtRate = trxToUsdtRate
                )
            }

            TotalRow(
                commission = commission,
                activationFee = if (!isActivated) createNewAccountFee.toTokenAmount() else BigDecimal.ZERO,
                trxToUsdtRate = trxToUsdtRate,
                totalAmount = totalAmount
            )
        }
    }
}

@Composable
private fun FeeRow(
    label: String,
    amount: BigDecimal,
    currency: String,
    trxToUsdtRate: BigDecimal
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = PubAddressDark)
        FeeAmount(amount = amount, currency = currency, trxToUsdtRate = trxToUsdtRate)
    }
}

@Composable
private fun FeeAmount(
    amount: BigDecimal,
    currency: String,
    trxToUsdtRate: BigDecimal
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = "$amount $currency",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "≈ ${decimalFormat(amount * trxToUsdtRate)} $",
            style = MaterialTheme.typography.bodyMedium,
            color = PubAddressDark
        )
    }
}

@Composable
private fun TotalRow(
    commission: BigDecimal,
    activationFee: BigDecimal,
    trxToUsdtRate: BigDecimal,
    totalAmount: BigDecimal,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Итого",
            style = MaterialTheme.typography.bodySmall,
            color = PubAddressDark
        )
        Text(
            text = "${decimalFormat((activationFee + commission) * trxToUsdtRate + totalAmount)} $",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
