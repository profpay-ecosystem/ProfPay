package com.profpay.wallet.viewmodel.create_of_recovery_wallet

import app.cash.turbine.test
import com.profpay.wallet.bridge.view_model.create_or_recovery_wallet.RecoverWalletState
import com.profpay.wallet.bridge.view_model.create_or_recovery_wallet.RecoverWalletViewModel
import com.profpay.wallet.data.repository.flow.AddressAndMnemonicRepo
import com.profpay.wallet.data.repository.flow.RecoveryResult
import com.profpay.wallet.tron.AddressDataWithoutPrivKey
import com.profpay.wallet.tron.AddressGenerateFromSeedPhr
import com.profpay.wallet.tron.AddressesWithKeysForM
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecoverWalletViewModelTest {

    @MockK(relaxed = true)
    private lateinit var addressAndMnemonicRepo: AddressAndMnemonicRepo

    private lateinit var viewModel: RecoverWalletViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `state changes to Success when repo emits RecoveryResult`() = runTest {
        val fakeFlow = MutableSharedFlow<RecoveryResult>()
        every { addressAndMnemonicRepo.addressFromMnemonic } returns fakeFlow

        viewModel = RecoverWalletViewModel(addressAndMnemonicRepo)

        viewModel.state.test {
            assertEquals(RecoverWalletState.Loading, awaitItem())

            val fakeResult = RecoveryResult.Success(
                address = AddressGenerateFromSeedPhr(
                    addressesWithKeysForM = AddressesWithKeysForM(
                        addresses = listOf(
                            AddressDataWithoutPrivKey(
                                address = "TFAKEADDRESS12345",
                                publicKey = "FAKE_PUBLIC_KEY",
                                indexDerivationSot = 0,
                                indexSot = 0
                            )
                        ),
                        entropy = byteArrayOf(1, 2, 3, 4, 5),
                        derivedIndices = listOf(0)
                    ),
                ),
                accountWasFound = false,
                userId = null
            )

            fakeFlow.emit(fakeResult)

            assertEquals(RecoverWalletState.Success(fakeResult), awaitItem())
        }
    }

    @Test
    fun `recoverWallet calls generateAddressFromMnemonic`() = runTest {
        every { addressAndMnemonicRepo.addressFromMnemonic } returns MutableSharedFlow()
        coEvery { addressAndMnemonicRepo.generateAddressFromMnemonic(any()) } just Runs

        viewModel = RecoverWalletViewModel(addressAndMnemonicRepo)
        viewModel.recoverWallet("abandon baby cabin damage")

        coVerify { addressAndMnemonicRepo.generateAddressFromMnemonic("abandon baby cabin damage") }
    }

    @Test
    fun `clearAddressFromMnemonic calls repo method`() = runTest {
        every { addressAndMnemonicRepo.addressFromMnemonic } returns MutableSharedFlow()
        coEvery { addressAndMnemonicRepo.clearAddressFromMnemonic() } just Runs

        viewModel = RecoverWalletViewModel(addressAndMnemonicRepo)

        viewModel.clearAddressFromMnemonic()

        coVerify { addressAndMnemonicRepo.clearAddressFromMnemonic() }
    }
}
