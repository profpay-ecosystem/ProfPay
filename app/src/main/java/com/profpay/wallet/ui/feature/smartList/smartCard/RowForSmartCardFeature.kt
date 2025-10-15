package com.profpay.wallet.ui.feature.smartList.smartCard
import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.profpay.wallet.R
import com.profpay.wallet.ui.app.theme.LocalFontSize
import kotlinx.coroutines.launch

@Composable
internal fun RowForSmartCardFeature(
    label: String,
    address: String,
    clipboard: Clipboard,
    content: @Composable RowScope.() -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier =
            Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 4.dp)
                .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(0.5f),
            text = label,
            fontSize = LocalFontSize.Small.fS,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.clickable {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipData.newPlainText("Wallet address", address).toClipEntry(),
                        )
                    }
                },
        ) {
            Text(
                text = "${address.take(5)}...${address.takeLast(5)}",
                fontSize = LocalFontSize.Small.fS,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Icon(
                modifier = Modifier.padding(4.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            content()
        }
    }
}
