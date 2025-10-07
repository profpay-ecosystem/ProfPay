package com.profpay.wallet.ui.feature.wallet.send
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.profpay.wallet.bridge.view_model.dto.TokenName
import com.profpay.wallet.ui.app.theme.PubAddressDark
import com.profpay.wallet.ui.app.theme.transparent


@Composable
fun TextFieldForSendFeature(
    sumSending: String,
    currentTokenName: TokenName,
    tokenBalance: String,
    onSumChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        TextField(
            value = sumSending,
            onValueChange = { onSumChange(it.replace(",", ".")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Введите кол-во ${currentTokenName.shortName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PubAddressDark,
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentTokenName.shortName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = PubAddressDark,
                        modifier = Modifier.padding(end = 8.dp),
                    )

                    Card(
                        shape = RoundedCornerShape(5.dp),
                        modifier = Modifier.padding(end = 8.dp),
                        elevation = CardDefaults.cardElevation(7.dp),
                        onClick = {
                            onSumChange(tokenBalance)
                        },
                    ) {
                        Text(
                            "MAX",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedIndicatorColor = MaterialTheme.colorScheme.transparent,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.transparent,
                disabledIndicatorColor = MaterialTheme.colorScheme.transparent,
                focusedContainerColor = MaterialTheme.colorScheme.transparent,
                unfocusedContainerColor = MaterialTheme.colorScheme.transparent,
                cursorColor = MaterialTheme.colorScheme.onBackground,
                selectionColors = TextSelectionColors(
                    handleColor = MaterialTheme.colorScheme.onBackground,
                    backgroundColor = MaterialTheme.colorScheme.transparent,
                ),
            ),
        )
    }
}
