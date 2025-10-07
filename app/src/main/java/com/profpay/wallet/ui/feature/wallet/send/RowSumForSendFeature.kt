package com.profpay.wallet.ui.feature.wallet.send
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.profpay.wallet.bridge.view_model.dto.TokenName


@Composable
fun RowSumForSendFeature(tokenBalance: String, currentTokenName: TokenName) {
    Row(
        modifier =
            Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = "Сумма",
            style = MaterialTheme.typography.titleMedium,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                modifier = Modifier.padding(end = 4.dp),
                text = tokenBalance,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = currentTokenName.shortName,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}
