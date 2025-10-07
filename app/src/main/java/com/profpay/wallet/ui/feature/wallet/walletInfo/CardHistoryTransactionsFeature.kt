package com.profpay.wallet.ui.feature.wallet.walletInfo
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
import com.profpay.wallet.data.database.entities.wallet.TransactionType

@Composable
fun CardHistoryTransactionsFeature(
    address: String,
    typeTransaction: Int,
    paintIconId: Int,
    amount: String,
    shortNameToken: String,
    transactionEntity: TransactionEntity,
    onClick: () -> Unit = {},
) {
    val label =
        when (typeTransaction) {
            TransactionType.SEND.index -> "Отправлено"
            TransactionType.RECEIVE.index -> "Получено"
            TransactionType.BETWEEN_YOURSELF.index -> "Между своими"
            else -> {
                ""
            }
        }
    val label2 =
        when (typeTransaction) {
            TransactionType.SEND.index -> "Куда: ${address.take(5)}...${address.takeLast(5)}"
            TransactionType.RECEIVE.index -> "Откуда: ${address.take(5)}...${address.takeLast(5)}"
            TransactionType.BETWEEN_YOURSELF.index ->
                "Откуда: ${transactionEntity.senderAddress.take(5)}..." +
                        "${transactionEntity.senderAddress.takeLast(5)}\n" +
                        "Куда: ${transactionEntity.receiverAddress.take(5)}..." +
                        transactionEntity.receiverAddress.takeLast(5)

            else -> return
        }

    Card(
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .shadow(7.dp, RoundedCornerShape(10.dp)),
        onClick = { onClick() },
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
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(text = label2, style = MaterialTheme.typography.labelLarge)
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
