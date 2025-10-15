package com.profpay.wallet.ui.components.custom

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profpay.wallet.ui.app.theme.BackgroundIcon
import com.profpay.wallet.ui.app.theme.transparent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CustomTabRow(
    titles: List<String>,
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
) {
    TabRow(
        modifier = Modifier.padding(horizontal = 8.dp),
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.transparent,
        divider = {},
        indicator = {},
    ) {
        titles.forEachIndexed { index, title ->
            Tab(
                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                unselectedContentColor = BackgroundIcon,
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.scrollToPage(index)
                    }
                },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                        maxLines = 2,
                    )
                },
            )
        }
    }
}
