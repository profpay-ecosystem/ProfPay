package com.profpay.wallet.ui.feature.wallet.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity

@Composable
fun CACardHistoryTransactionsFeature(
    address: String,
    paintIconId: Int,
    amount: String,
    shortNameToken: String,
    transactionEntity: TransactionEntity,
) {
    val isSender = transactionEntity.senderAddress == address
    val isReceiver = transactionEntity.receiverAddress == address

    val transactionLabel: String
    val transactionDirection: String

    when {
        isSender -> {
            transactionLabel = "Отправлено"
            transactionDirection = "Куда: ${transactionEntity.receiverAddress.take(5)}...${transactionEntity.receiverAddress.takeLast(5)}"
        }
        isReceiver -> {
            transactionLabel = "Получено"
            transactionDirection = "Откуда: ${transactionEntity.senderAddress.take(5)}...${transactionEntity.senderAddress.takeLast(5)}"
        }
        else -> return
    }

    Card(
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .shadow(7.dp, RoundedCornerShape(10.dp)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(start = 10.dp, end = 16.dp)
                        .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp)
                                .size(40.dp)
                                .paint(
                                    painterResource(id = paintIconId),
                                    contentScale = ContentScale.FillBounds,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {}
                    Column(modifier = Modifier.padding(horizontal = 12.dp, 8.dp)) {
                        Text(
                            text = transactionLabel,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(text = transactionDirection, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "$amount $shortNameToken",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
