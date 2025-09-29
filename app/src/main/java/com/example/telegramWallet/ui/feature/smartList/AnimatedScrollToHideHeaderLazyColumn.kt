package com.example.telegramWallet.ui.feature.smartList

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedScrollToHideHeaderLazyColumn(
    contentToHide: @Composable () -> Unit = {},
    contentInLColumn: LazyListScope.() -> Unit,
) {
    val listState = rememberLazyListState()
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }
    var isVisible by remember { mutableStateOf(true) }

    // Плавная анимация прозрачности с измененной продолжительностью и кривой
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = 600, // Продолжительность анимации
                easing = FastOutSlowInEasing, // Кривая интерполяции
            ),
    )

    // Отслеживаем изменения прокрутки
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (currentIndex, currentScrollOffset) ->
                val scrolledDown =
                    (currentIndex > previousIndex) ||
                        (currentIndex == previousIndex && currentScrollOffset > previousScrollOffset)
                val scrolledUp =
                    (currentIndex < previousIndex) ||
                        (currentIndex == previousIndex && currentScrollOffset < previousScrollOffset)

                if (scrolledDown) {
                    isVisible = false
                } else if (scrolledUp) {
                    isVisible = true
                }

                previousIndex = currentIndex
                previousScrollOffset = currentScrollOffset
            }
    }

    Column {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
        ) {
            stickyHeader {
                // Анимируемый заголовок
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedAlpha) // Анимируем высоту
                            .alpha(animatedAlpha), // Анимируем прозрачность
                ) {
                    if (isVisible || animatedAlpha > 0f) {
                        contentToHide()
                    }
                }
            }
            item { Spacer(modifier = Modifier.size(10.dp)) }

            contentInLColumn()

            if (!isVisible) {
                item { Spacer(modifier = Modifier.size(90.dp)) }
            }
        }
    }
}
