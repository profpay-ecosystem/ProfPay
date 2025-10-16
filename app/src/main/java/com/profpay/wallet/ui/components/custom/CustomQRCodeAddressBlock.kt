package com.profpay.wallet.ui.components.custom

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CustomQRCodeAddressBlock(
    modifier: Modifier = Modifier,
    qrBitmap: Bitmap?,
    address: String,
    balanceText: String? = null,
) {
    if (address.isEmpty() || address == "empty") return

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.onPrimary)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- QR-код ---
        qrBitmap?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = "qr",
                modifier = Modifier.size(300.dp)
            )
        }
        // --- Адрес (в 2 строки) ---
        if (address.isNotEmpty()) {
            Text(
                text = buildString {
                    append(address.dropLast(10))
                    append("\n")
                    append(address.takeLast(10))
                },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }
        // --- Баланс (если есть) ---
        if (balanceText != null) {
            Text(
                text = "Balance $balanceText",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}

