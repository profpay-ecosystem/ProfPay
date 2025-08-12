package com.example.telegramWallet.ui.feature.lockScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun InputDots(
    numbers: List<Int> = listOf(1, 2),
    isError: Boolean = false,
    onErrorReset: (() -> Unit)? = null
) {
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            offsetX.snapTo(0f)
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 500
                    -10f at 50
                    10f at 100
                    -8f at 150
                    8f at 200
                    -4f at 250
                    4f at 300
                    0f at 500
                }
            )

            delay(1000)
            onErrorReset?.invoke()
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
    ) {
        for (i in 0..3) {
            PinIndicator(
                filled = when (i) {
                    0 -> numbers.isNotEmpty()
                    else -> numbers.size > i
                },
                isError = isError
            )
        }
    }
}

@Composable
private fun PinIndicator(
    filled: Boolean,
    isError: Boolean
) {
    val targetBorderColor = when {
        isError -> Color.Red
        else -> MaterialTheme.colorScheme.onPrimary
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 400),
        label = "PinIndicatorColor"
    )

    Box(
        modifier = Modifier
            .padding(15.dp)
            .size(15.dp)
            .clip(CircleShape)
            .background(if (filled) MaterialTheme.colorScheme.onPrimary else Color.Transparent)
            .border(2.dp, animatedBorderColor, CircleShape)
    )
}
