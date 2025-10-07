package com.profpay.wallet.ui.feature.wallet.send.bottomsheet
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.bridge.view_model.wallet.send.SendFromWalletViewModel
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.BackgroundLight
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.app.theme.PubAddressDark
import com.profpay.wallet.utils.decimalFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger


@Composable
fun ContentBottomSheetTransferConfirmation(
    viewModel: SendFromWalletViewModel = hiltViewModel(),
    isDetails: Boolean,
    modelTransferFromBS: ModelTransferFromBS,
    confirmTransaction: () -> Unit,
) {
    val tokenNameModel = modelTransferFromBS.tokenName

    val (trxToUsdtRate, setTrxToUsdtRate) = remember { mutableStateOf(BigDecimal.valueOf(1.0)) }
    val isConfirmButtonEnabled = remember { mutableStateOf(true) }
    val (isNeedActivationAddress, setIsNeedActivationAddress) = remember { mutableStateOf(false) }
    val (createNewAccountFeeInSystemContract, setCreateNewAccountFeeInSystemContract) =
        remember {
            mutableStateOf(
                BigInteger.ZERO,
            )
        }

    LaunchedEffect(Unit) {
        val isAddressActivated =
            withContext(Dispatchers.IO) {
                viewModel.tron.addressUtilities.isAddressActivated(modelTransferFromBS.addressReceiver)
            }
        if (!isAddressActivated) {
            setCreateNewAccountFeeInSystemContract(
                withContext(Dispatchers.IO) {
                    viewModel.tron.addressUtilities.getCreateNewAccountFeeInSystemContract()
                },
            )
            setIsNeedActivationAddress(true)
        }

        setTrxToUsdtRate(viewModel.trxToUsdtRate())
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(vertical = 0.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "-${modelTransferFromBS.amount} ${tokenNameModel.shortName}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            if (tokenNameModel.shortName == "TRX") {
                Text(
                    text = "≈ ${decimalFormat(modelTransferFromBS.amount * trxToUsdtRate)} $",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PubAddressDark,
                )
            } else {
                Text(
                    text = "≈ ${decimalFormat(modelTransferFromBS.amount)} $",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PubAddressDark,
                )
            }
        }
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier =
                Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(10.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(vertical = 16.dp, horizontal = 8.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Сеть",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PubAddressDark,
                        modifier = Modifier,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .padding(end = 4.dp)
                                    .size(20.dp)
                                    .paint(
                                        painterResource(id = tokenNameModel.paintIconId),
                                        contentScale = ContentScale.FillBounds,
                                    ),
                        )
                        Text(
                            text = "${tokenNameModel.blockchainName} (${tokenNameModel.shortName})",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Откуда",
                        color = PubAddressDark,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier,
                    )
                    Text(
                        text = "${modelTransferFromBS.addressSender.take(7)}...${
                            modelTransferFromBS.addressSender.takeLast(
                                7,
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier,
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Куда",
                        color = PubAddressDark,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier,
                    )
                    Text(
                        text = "${modelTransferFromBS.addressReceiver.take(7)}...${
                            modelTransferFromBS.addressReceiver.takeLast(
                                7,
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier,
                    )
                }
            }
        }
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier =
                Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(10.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(vertical = 16.dp, horizontal = 8.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Комиссия",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PubAddressDark,
                        modifier = Modifier,
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${modelTransferFromBS.commission} TRX",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        Text(
                            text = "≈ ${decimalFormat(modelTransferFromBS.commission * trxToUsdtRate)} $",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PubAddressDark,
                        )
                    }
                }
                if (isNeedActivationAddress) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Активация адреса",
                            color = PubAddressDark,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier,
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${createNewAccountFeeInSystemContract.toTokenAmount()} TRX",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                            Text(
                                text = "≈ ${
                                    decimalFormat(
                                        createNewAccountFeeInSystemContract.toTokenAmount() * trxToUsdtRate,
                                    )
                                } $",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PubAddressDark,
                            )
                        }
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Итого",
                        style = MaterialTheme.typography.bodySmall,
                        color = PubAddressDark,
                        modifier = Modifier,
                    )
                    Text(
                        text = "${
                            decimalFormat(
                                (createNewAccountFeeInSystemContract.toTokenAmount() + modelTransferFromBS.commission) * trxToUsdtRate + modelTransferFromBS.amount,
                            )
                        } $",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier,
                    )
                }
            }
        }
        if (!isDetails) {
            Spacer(modifier = Modifier.height(50.dp))
            Button(
                enabled = isConfirmButtonEnabled.value,
                onClick = {
                    if (!isConfirmButtonEnabled.value) return@Button
                    isConfirmButtonEnabled.value = false
                    confirmTransaction()
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = 4.dp, vertical = 16.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = GreenColor,
                        contentColor = BackgroundLight,
                    ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Подтвердить",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                )
            }
        }
    }
}
