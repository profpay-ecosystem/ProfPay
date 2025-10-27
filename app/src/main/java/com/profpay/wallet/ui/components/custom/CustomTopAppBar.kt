package com.profpay.wallet.ui.components.custom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.profpay.wallet.ui.app.theme.transparent
import com.profpay.wallet.ui.app.theme.white

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    goToBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.white),
            )
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.transparent,
            ),
        navigationIcon = {
            run {
                IconButton(onClick = { goToBack() }) {
                    Icon(
                        modifier = Modifier.size(34.dp),
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.white,
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: String,
    goToNext: () -> Unit,
    iconNext: ImageVector,
) {
    TopAppBar(
        title = {
            Row(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { goToNext() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.padding(start = 2.dp),
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.white),
                )
                Icon(
                    modifier = Modifier.size(30.dp),
                    imageVector = iconNext,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.white,
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.transparent,
            ),
    )
}
