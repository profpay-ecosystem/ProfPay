package com.profpay.wallet.ui.feature.wallet.walletArchivalSots

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.bridge.viewmodel.dto.TokenName
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.shared.sharedPref
import java.math.BigInteger

@Composable
internal fun CardArchivalAddressFeature(
    goToWalletAddress: () -> Unit,
    addressWithTokens: AddressWithTokens,
) {
    val sharedPref = sharedPref()

    val tokenName = sharedPref.getString("token_name", TokenName.USDT.tokenName)
    val tokenEntity =
        addressWithTokens.tokens
            .stream()
            .filter { currentToken -> currentToken.token.tokenName == tokenName }
            .findFirst()
            .orElse(null)

    val balanceWF = tokenEntity?.balanceWithoutFrozen ?: BigInteger.ZERO

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        elevation = CardDefaults.cardElevation(10.dp),
        onClick = {
            sharedPref.edit {
                putString(
                    PrefKeys.ADDRESS_FOR_WALLET_ADDRESS,
                    addressWithTokens.addressEntity.address,
                )
            }
            goToWalletAddress()
        },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, 8.dp)) {
                    Text(
                        text =
                            "${addressWithTokens.addressEntity.address.take(7)}..." +
                                "${addressWithTokens.addressEntity.address.takeLast(7)} ",
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    if (balanceWF > BigInteger.ZERO) {
                        Text(
                            text = "$${balanceWF.toTokenAmount()}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
