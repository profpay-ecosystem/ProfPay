package com.profpay.wallet.ui.feature.wallet.walletInfo
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.app.theme.transparent

@Composable
fun ColorBoxOnCardInfoFeature(pricePercentage24h: Double) {
    val color =
        if (pricePercentage24h >= 0.0) {
            GreenColor.copy(alpha = 0.6f, green = 1f)
        } else {
            Color.Red.copy(alpha = 0.4f)
        }
    val colorTransparent = MaterialTheme.colorScheme.transparent

    Box(
        modifier =
            Modifier
                .fillMaxSize(0.7f)
                .background(colorTransparent),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors = listOf(color, colorTransparent),
                        center = Offset(size.width * 0.35f, size.height * 0.3f),
                        radius = size.minDimension * 0.5f,
                    ),
                radius = size.minDimension * 0.8f,
            )
        }
    }
}
