package com.profpay.wallet.ui.screens

import StackedSnackbarHost
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.R
import com.profpay.wallet.bridge.view_model.smart_contract.GetSmartContractViewModel
import com.profpay.wallet.data.database.entities.wallet.SmartContractEntity
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.feature.smartList.AnimatedScrollToHideHeaderLazyColumn
import com.profpay.wallet.ui.feature.smartList.IsEmptyListSmartContract
import com.profpay.wallet.ui.feature.smartList.smartCard.SmartCardFeature
import com.profpay.wallet.ui.feature.smartList.SmartHeaderCreateContractInListFeature
import com.profpay.wallet.ui.feature.smartList.SmartHeaderInListFeature
import com.profpay.wallet.ui.feature.smartList.bottomSheets.ProgressIndicatorSmartModalFeature
import com.profpay.wallet.ui.feature.smartList.bottomSheets.confirmationSmartModalFeature
import com.profpay.wallet.ui.shared.sharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rememberStackedSnackbarHostState
import java.math.BigInteger

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SmartListScreen(
    viewModel: GetSmartContractViewModel = hiltViewModel(),
    goToSystemTRX: () -> Unit,
) {
    val smartContractsState by viewModel.state.collectAsStateWithLifecycle()
    val smartModalState by viewModel.stateModal.collectAsStateWithLifecycle()
    var contractBalance by remember { mutableStateOf<BigInteger>(BigInteger.ZERO) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = {
                !smartModalState.isActive
            },
        )

    var smartContractLD by remember { mutableStateOf<LiveData<SmartContractEntity?>?>(null) }
    val smartContract = smartContractLD?.observeAsState(initial = null)

    LaunchedEffect(Unit) {
        // Получаем LiveData внутри coroutine-контекста
        smartContractLD =
            withContext(Dispatchers.IO) {
                viewModel.smartContractDatabaseRepo.getSmartContractLiveData()
            }
    }

    LaunchedEffect(smartContract?.value) {
        smartContract?.value?.let {
            withContext(Dispatchers.IO) {
                contractBalance = viewModel.tron.addressUtilities.getUsdtBalance(it.contractAddress)
            }
        }
    }

    val stackedSnackbarHostState = rememberStackedSnackbarHostState()

    val bottomPadding = sharedPref().getFloat("bottomPadding", 54f)

    Scaffold(
//        modifier = Modifier.padding(bottom = bottomPadding.dp),
        snackbarHost = {
            StackedSnackbarHost(
                hostState = stackedSnackbarHostState,
                modifier =
                    Modifier
                        .padding(8.dp, 90.dp),
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Smart",
                        style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.onPrimary),
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                navigationIcon = {},
                actions = {
                    run {
                        IconButton(onClick = { /*goToBack()*/ }) {
                            Icon(
                                modifier = Modifier.size(35.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_help_quation),
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clickable { keyboardController?.hide() },
        ) {}
        Column(
            modifier =
                Modifier
                    .padding(top = padding.calculateTopPadding(), bottom = bottomPadding.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .fillMaxSize(),
        ) {
            AnimatedScrollToHideHeaderLazyColumn(
                contentToHide = {
                    Column(
                        modifier =
                            Modifier
                                .background(MaterialTheme.colorScheme.primary),
                    ) {
                        if (smartContract?.value != null) {
                            SmartHeaderInListFeature(
                                balance = contractBalance.toTokenAmount(),
                                address = smartContract.value?.contractAddress,
                                viewModel = viewModel,
                            )
                        } else {
                            SmartHeaderCreateContractInListFeature(
                                viewModel = viewModel,
                                snackbar = stackedSnackbarHostState,
                                goToSystemTRX = goToSystemTRX,
                            )
                        }
                        HorizontalDivider()
                    }
                },
            ) {
                itemsIndexed(smartContractsState) { index, item ->
                    SmartCardFeature(index + 1, item, viewModel)
                }
                if (smartContractsState.isEmpty()) {
                    item { IsEmptyListSmartContract() }
                }
                item { Spacer(modifier = Modifier.size(10.dp)) }
            }
        }
    }
    if (smartModalState.isActive) {
        ModalBottomSheet(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { Box(modifier = Modifier) },
            modifier = Modifier.height(IntrinsicSize.Min),
            onDismissRequest = {},
            sheetState = sheetState,
        ) {
            val (confirm, setConfirm) = remember { mutableStateOf(false) }
            if (confirm) {
                ProgressIndicatorSmartModalFeature(smartModalState.text)
            } else {
                setConfirm(
                    confirmationSmartModalFeature(
                        smartModalState = smartModalState,
                        snackbar = stackedSnackbarHostState,
                    ),
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
