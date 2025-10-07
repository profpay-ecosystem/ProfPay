package com.profpay.wallet.ui.feature.wallet.walletInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profpay.wallet.R
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.app.theme.RedColor
import com.profpay.wallet.utils.decimalFormat
import java.math.BigDecimal
import java.math.BigInteger

@Composable
fun WalletInfoCardInfoFeature(
    totalBalance: BigInteger,
    pricePercentage24h: Double,
    setIsOpenSheetChoiceTokenToSend: () -> Unit,
    goToWalletSystemTRX: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(16.dp)),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (totalBalance.toTokenAmount() > BigDecimal(0)) {
                ColorBoxOnCardInfoFeature(pricePercentage24h)
            }
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // row 1
                Row(
                    modifier =
                        Modifier
                            .padding()
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(modifier = Modifier.weight(0.8f)) {
                        Text(
                            text = "Total balance",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .clickable { },
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(20.dp)
                                        .paint(
                                            painterResource(id = R.drawable.trx_tron),
                                            contentScale = ContentScale.FillBounds,
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {}
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                modifier = Modifier.padding(),
                                text = "Tron",
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
                // row 2
                Row(
                    modifier =
                        Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier) {
//                                Canvas(modifier = Modifier.size(width = 140.dp, height = 70.dp)) {
//                                    drawRect(Color.Green.copy(alpha = 0.4f))
//                                }
                            Text(
                                text = "$${decimalFormat(totalBalance.toTokenAmount())}",
                                fontSize = 35.sp,
                                style = MaterialTheme.typography.displayMedium,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = "24 hours:",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        if (pricePercentage24h >= 0.0) {
                            Text(
                                "+${decimalFormat(pricePercentage24h.toBigDecimal())}%",
                                color = GreenColor,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        } else {
                            Text(
                                "${decimalFormat(pricePercentage24h.toBigDecimal())}%",
                                color = RedColor,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
                // row 3
                Row(
                    modifier =
                        Modifier
                            .padding(top = 20.dp)
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .fillMaxWidth()
                                .weight(0.5f)
                                .height(50.dp)
                                .shadow(7.dp, RoundedCornerShape(10.dp))
                                .clickable {
                                    setIsOpenSheetChoiceTokenToSend()
                                },
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_send),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(text = "Отправить")
                        }
                    }
                    Card(
                        modifier =
                            Modifier
                                .padding(start = 8.dp)
                                .fillMaxWidth()
                                .weight(0.5f)
                                .height(50.dp)
                                .shadow(7.dp, RoundedCornerShape(10.dp))
                                .clickable {
                                    goToWalletSystemTRX()
                                },
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_get),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(text = "Системный TRX")
                        }
                    }
                }
            }
        }
    }
}
