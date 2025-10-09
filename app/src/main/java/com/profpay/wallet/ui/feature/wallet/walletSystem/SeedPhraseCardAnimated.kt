package com.profpay.wallet.ui.feature.wallet.walletSystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SeedPhraseCardAnimated(
    seedPhrase: String,
    isExpanded: Boolean,
) {
    val words = seedPhrase.trim().split(" ")

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier =
            Modifier
                .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(500)) + expandVertically(tween(500)),
            exit = fadeOut(tween(500)) + shrinkVertically(tween(500)),
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Column {
                    if (seedPhrase.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            contentPadding = PaddingValues(4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            itemsIndexed(words) { index, word ->
                                Text(
                                    text = "${index + 1}. $word",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier =
                                        Modifier
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                RoundedCornerShape(8.dp),
                                            ).padding(8.dp),
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Удалите и восстановите данный кошелёк, для работы этой функции",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier =
                                Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp),
                                    ).padding(8.dp),
                        )
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}
