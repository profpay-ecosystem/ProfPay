package com.profpay.wallet.utils

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun generateQRCode(
    text: String,
    width: Int = 400,
    height: Int = 400,
): Bitmap? {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary.toArgb()

    LaunchedEffect(text) {
        bitmap =
            withContext(Dispatchers.Default) {
                try {
                    val bitMatrix: BitMatrix =
                        QRCodeWriter().encode(
                            text,
                            BarcodeFormat.QR_CODE,
                            width,
                            height,
                        )

                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                        for (x in 0 until width) {
                            for (y in 0 until height) {
                                setPixel(
                                    x,
                                    y,
                                    if (bitMatrix[x, y]) {
                                        primaryColor
                                    } else {
                                        onPrimaryColor
                                    },
                                )
                            }
                        }
                    }
                } catch (e: WriterException) {
                    e.printStackTrace()
                    null
                }
            }
    }

    return bitmap
}
