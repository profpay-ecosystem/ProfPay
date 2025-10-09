package com.profpay.wallet.ui.feature.smartList.smartCard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.bridge.view_model.smart_contract.ContractButtonVisibleType
import com.profpay.wallet.bridge.view_model.smart_contract.GetSmartContractViewModel
import com.profpay.wallet.bridge.view_model.smart_contract.StatusData
import com.profpay.wallet.bridge.view_model.smart_contract.usecases.isBuyerNotDeposited
import com.profpay.wallet.bridge.view_model.smart_contract.usecases.isSellerNotPayedExpertFee
import com.profpay.wallet.data.flow_db.repo.SmartContractButtonType
import com.profpay.wallet.ui.feature.smartList.bottomSheets.bottomSheetDetails
import kotlinx.coroutines.launch
import org.example.protobuf.smart.SmartContractProto

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun SmartCardFeature(
    index: Int,
    item: SmartContractProto.ContractDealListResponse,
    viewModel: GetSmartContractViewModel,
) {
    val scope = rememberCoroutineScope()

    val (status, setStatus) = remember { mutableStateOf<StatusData?>(null) }
    val (oppositeUsername, setOppositeUsername) = remember { mutableStateOf<String?>(null) }
    val (oppositeUserId, setOppositeUserId) = remember { mutableStateOf<Long?>(null) }
    val (isButtonVisible, setIsButtonVisible) =
        remember {
            mutableStateOf<ContractButtonVisibleType>(
                ContractButtonVisibleType(false, false),
            )
        }
    val (isBuyerNotDeposited, setIsBuyerNotDeposited) = remember { mutableStateOf(false) }
    val (isSellerNotPayedExpertFee, setIsSellerNotPayedExpertFee) = remember { mutableStateOf(false) }
    val (_, setIsOpenDetailsSheet) = bottomSheetDetails(item, viewModel)

    LaunchedEffect(item) {
        scope.launch {
            setStatus(viewModel.smartContractStatus(deal = item))
            setOppositeUsername(viewModel.getOppositeUsername(deal = item))
            setOppositeUserId(viewModel.getOppositeTelegramId(deal = item))
            setIsButtonVisible(viewModel.isButtonVisible(deal = item))
            setIsBuyerNotDeposited(
                isBuyerNotDeposited(
                    item,
                    viewModel.profileRepo.getProfileUserId(),
                ),
            )
            setIsSellerNotPayedExpertFee(
                isSellerNotPayedExpertFee(
                    item,
                    viewModel.profileRepo.getProfileUserId(),
                ),
            )
        }
    }

    SmartCardWidget(
        indexToString = index.toString(),
        status = status,
        oppositeUsername = oppositeUsername,
        oppositeUserId = oppositeUserId,
        clickableDetails = { setIsOpenDetailsSheet(true) },
        item = item,
        isBuyerNotDeposited = isBuyerNotDeposited,
        isSellerNotPayedExpertFee = isSellerNotPayedExpertFee,
        isButtonVisible = isButtonVisible,
        onClickButtonCancel = {
            viewModel.viewModelScope.launch {
                viewModel.setSmartContractModalActive(true, SmartContractButtonType.REJECT, item)
            }
        },
        onClickButtonAgree = {
            viewModel.viewModelScope.launch {
                viewModel.setSmartContractModalActive(true, SmartContractButtonType.ACCEPT, item)
            }
        },
    )
}

