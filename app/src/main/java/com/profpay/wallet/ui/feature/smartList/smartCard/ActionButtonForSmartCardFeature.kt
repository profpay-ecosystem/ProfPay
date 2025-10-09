package com.profpay.wallet.ui.feature.smartList.smartCard
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profpay.wallet.ui.app.theme.LocalFontSize
import com.profpay.wallet.ui.app.theme.backgroundContainerButtonLight


@Composable
internal fun ActionButtonForSmartCardFeature(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .padding(horizontal = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.backgroundContainerButtonLight,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            fontSize = LocalFontSize.Medium.fS,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
