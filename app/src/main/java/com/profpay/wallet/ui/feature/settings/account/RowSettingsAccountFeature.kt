package com.profpay.wallet.ui.feature.settings.account

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.profpay.wallet.R
import kotlinx.coroutines.launch


@Composable
fun RowSettingsAccountFeature(
    label: String,
    info: String,
    isInfoShorted: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current

    val infoForUI =
        if (isInfoShorted) {
            if (info.length > 12) {
                "${info.take(6)}...${info.takeLast(6)}"
            } else {
                info
            }
        } else {
            info
        }

    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(0.45f),
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )

        Row(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .clickable {
                        scope.launch {
                            clipboard.setClipEntry(
                                ClipData.newPlainText("Telegram Data", info).toClipEntry(),
                            )
                        }
                    },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier =
                    Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .weight(0.4f),
                text = infoForUI,
                style = MaterialTheme.typography.bodySmall,
            )
            Icon(
                modifier =
                    Modifier
                        .padding(4.dp)
                        .weight(0.05f),
                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
