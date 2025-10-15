package com.profpay.wallet.ui.feature.wallet.send
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.profpay.wallet.ui.app.theme.PubAddressDark
import com.profpay.wallet.ui.app.theme.redColor
import com.profpay.wallet.ui.app.theme.transparent

@Composable
fun CardWithAddressForSendFromWallet(
    title: String,
    addressSending: String,
    onAddressChange: (String) -> Unit,
    warningAddress: Boolean,
) {
    val transition = updateTransition(targetState = warningAddress, label = "warningTransition")

    val animatedBorderColor by transition.animateColor(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessVeryLow,
            )
        },
        label = "borderColor",
    ) { if (it) MaterialTheme.colorScheme.redColor else MaterialTheme.colorScheme.transparent }

    val animatedContainerColor by transition.animateColor(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow,
            )
        },
        label = "containerColor",
    ) { if (it) MaterialTheme.colorScheme.redColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary }

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp),
    )

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        border = BorderStroke(2.dp, animatedBorderColor),
    ) {
        TextField(
            value = addressSending,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Введите адрес",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PubAddressDark,
                )
            },
            shape = MaterialTheme.shapes.small.copy(),
            onValueChange = { onAddressChange(it) },
            trailingIcon = {},
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = PubAddressDark,
                    focusedIndicatorColor = MaterialTheme.colorScheme.transparent,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.transparent,
                    disabledIndicatorColor = MaterialTheme.colorScheme.transparent,
                    focusedContainerColor = animatedContainerColor,
                    unfocusedContainerColor = animatedContainerColor,
                    cursorColor = MaterialTheme.colorScheme.onBackground,
                    selectionColors =
                        TextSelectionColors(
                            handleColor = MaterialTheme.colorScheme.onBackground,
                            backgroundColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        ),
                ),
        )
    }
}
