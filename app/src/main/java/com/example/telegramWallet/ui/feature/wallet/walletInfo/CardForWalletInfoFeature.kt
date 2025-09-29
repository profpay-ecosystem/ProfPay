package com.example.telegramWallet.ui.feature.wallet.walletInfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.telegramWallet.backend.http.models.binance.BinanceSymbolEnum
import com.example.telegramWallet.backend.http.models.coingecko.CoinSymbolEnum
import com.example.telegramWallet.bridge.view_model.wallet.WalletInfoViewModel
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.ui.app.theme.GreenColor
import com.example.telegramWallet.ui.app.theme.RedColor
import com.example.telegramWallet.utils.decimalFormat
import java.math.BigInteger

@Composable
fun CardForWalletInfoFeature(
    paintIconId: Int,
    label: String,
    balance: BigInteger,
    balanceForLastMonth: Double = 0.0,
    shortNameToken: String,
    viewModel: WalletInfoViewModel,
    onClick: () -> Unit = {},
) {
    val (rateValue, setRateValue) = remember { mutableDoubleStateOf(0.0) }
    val (priceChangePercentage24hUsdt, setPriceChangePercentage24hUsdt) =
        remember {
            mutableDoubleStateOf(
                0.0,
            )
        }
    val (priceChangePercentage24hTrx, setPriceChangePercentage24hTrx) =
        remember {
            mutableDoubleStateOf(
                0.0,
            )
        }

    LaunchedEffect(Unit) {
        setRateValue(viewModel.exchangeRatesRepo.getExchangeRateValue(BinanceSymbolEnum.TRX_USDT.symbol))
        setPriceChangePercentage24hUsdt(
            viewModel.tradingInsightsRepo.getPriceChangePercentage24h(
                CoinSymbolEnum.USDT_TRC20.symbol,
            ),
        )
        setPriceChangePercentage24hTrx(
            viewModel.tradingInsightsRepo.getPriceChangePercentage24h(
                CoinSymbolEnum.TRON.symbol,
            ),
        )
    }

    val priceInUsdt: String =
        if (label == "TRX") {
            decimalFormat(balance.toTokenAmount() * rateValue.toBigDecimal())
        } else {
            decimalFormat(balance.toTokenAmount())
        }

    Card(
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
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
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "${decimalFormat(balance.toTokenAmount())} $shortNameToken",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
            ) {
                Text(text = "$$priceInUsdt", style = MaterialTheme.typography.bodySmall)

                val priceChangePercentage24h =
                    if (shortNameToken == "TRX") {
                        priceChangePercentage24hTrx
                    } else {
                        priceChangePercentage24hUsdt
                    }

                if (priceChangePercentage24h >= 0.0) {
                    Text(
                        "+${decimalFormat(priceChangePercentage24h.toBigDecimal())}%",
                        color = GreenColor,
                        style = MaterialTheme.typography.labelLarge,
                    )
                } else {
                    Text(
                        "${decimalFormat(priceChangePercentage24h.toBigDecimal())}%",
                        color = RedColor,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
