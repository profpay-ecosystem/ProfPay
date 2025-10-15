package com.profpay.wallet.viewmodel.createorrecovery

import app.cash.turbine.test
import cash.z.ecc.android.bip39.Mnemonics
import com.profpay.wallet.bridge.viewmodel.createorrecovery.CreateNewWalletState
import com.profpay.wallet.bridge.viewmodel.createorrecovery.CreateNewWalletViewModel
import com.profpay.wallet.data.repository.flow.AddressAndMnemonicRepo
import com.profpay.wallet.tron.AddressDataWithoutPrivKey
import com.profpay.wallet.tron.AddressGenerateResult
import com.profpay.wallet.tron.AddressesWithKeysForM
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateNewWalletViewModelTest {
    private val addressAndMnemonicRepo = mockk<AddressAndMnemonicRepo>(relaxed = true)
    private lateinit var viewModel: CreateNewWalletViewModel

    private val fakeFlow = MutableSharedFlow<AddressGenerateResult>(replay = 1)

    @Before
    fun setup() {
        every { addressAndMnemonicRepo.addressAndMnemonic } returns fakeFlow
        viewModel = CreateNewWalletViewModel(addressAndMnemonicRepo)
    }

    @Test
    fun `When initializing ViewModel, generateNewAddressAndMnemonic is called`() =
        runTest {
            coVerify { addressAndMnemonicRepo.generateNewAddressAndMnemonic() }
        }

    @Test
    fun `State is updated to Success when new data appears`() =
        runTest {
            val fakeResult =
                AddressGenerateResult(
                    addressesWithKeysForM =
                        AddressesWithKeysForM(
                            addresses =
                                listOf(
                                    AddressDataWithoutPrivKey(
                                        address = "TFAKEADDRESS12345",
                                        publicKey = "FAKE_PUBLIC_KEY",
                                        indexDerivationSot = 0,
                                        indexSot = 0,
                                    ),
                                ),
                            entropy = byteArrayOf(1, 2, 3, 4, 5),
                            derivedIndices = listOf(0),
                        ),
                    mnemonic =
                        Mnemonics.MnemonicCode(
                            "abandon baby cabin damage abandon baby cabin damage abandon baby cabin damage",
                        ),
                )

            viewModel.state.test {
                // Начальное состояние — Loading
                assertEquals(CreateNewWalletState.Loading, awaitItem())

                // Эмитим данные из репозитория
                fakeFlow.emit(fakeResult)

                val state = awaitItem()
                assert(state is CreateNewWalletState.Success)
                assertEquals(fakeResult, (state as CreateNewWalletState.Success).addressGenerateResult)
            }
        }
}
