package com.profpay.wallet.data.repository.flow

import android.content.Context
import app.cash.turbine.test
import com.profpay.wallet.backend.grpc.CryptoAddressGrpcClient
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.tron.Tron
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddressAndMnemonicRepoImplTest {
    private val mockContext = mockk<Context>(relaxed = true)
    private val testScheduler = TestCoroutineScheduler()
    private val ioDispatcher = StandardTestDispatcher(testScheduler)

    @Test
    fun `getAddressAndMnemonic emits generated data`() = runTest(testScheduler) {
        val mockProfileRepo = mockk<ProfileRepo>(relaxed = true)
        val mockAddressRepo = mockk<AddressRepo>(relaxed = true)

        val mockGrpcClient = mockk<CryptoAddressGrpcClient>(relaxed = true)
        val mockGrpcClientFactory = mockk<GrpcClientFactory>()

        every { mockGrpcClientFactory.getGrpcClient(CryptoAddressGrpcClient::class.java, any(), any()) } returns mockGrpcClient

        val repo = AddressAndMnemonicRepoImpl(
            tron = Tron(mockContext),
            ioDispatcher = ioDispatcher,
            profileRepo = mockProfileRepo,
            addressRepo = mockAddressRepo,
            grpcClientFactory = mockGrpcClientFactory
        )

        repo.addressAndMnemonic.test {
            repo.generateNewAddressAndMnemonic()

            val emitted = awaitItem()

            assertEquals(7, emitted.addressesWithKeysForM.addresses.size)
            assertTrue(emitted.addressesWithKeysForM.addresses.all { it.publicKey.isNotEmpty() })
            assertTrue(emitted.addressesWithKeysForM.entropy.isNotEmpty())
            assertEquals(12, emitted.mnemonic.wordCount)

            cancelAndConsumeRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `generateNewAddressAndMnemonic uses IO dispatcher`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val mockGrpcClient = mockk<CryptoAddressGrpcClient>(relaxed = true)
        val mockGrpcClientFactory = mockk<GrpcClientFactory>()

        every { mockGrpcClientFactory.getGrpcClient(CryptoAddressGrpcClient::class.java, any(), any()) } returns mockGrpcClient

        val repo = AddressAndMnemonicRepoImpl(
            tron = Tron(mockContext),
            ioDispatcher = testDispatcher,
            profileRepo = mockk(relaxed = true),
            addressRepo = mockk(relaxed = true),
            grpcClientFactory = mockGrpcClientFactory
        )

        var executedOnIoDispatcher = false

        // spy внутри с проверкой текущего диспетчера
        val job = launch(testDispatcher) {
            repo.generateNewAddressAndMnemonic()
            executedOnIoDispatcher = (coroutineContext[CoroutineDispatcher] as CoroutineDispatcher) == testDispatcher
        }

        // нужно прогнать все задачи
        testScheduler.advanceUntilIdle()

        assertTrue("Expected code to run on ioDispatcher", executedOnIoDispatcher)
        job.cancel()
    }

    @Test
    fun `generateNewAddressAndMnemonic multiple calls`() = runTest(testScheduler) {
        val mockProfileRepo = mockk<ProfileRepo>(relaxed = true)
        val mockAddressRepo = mockk<AddressRepo>(relaxed = true)
        val mockGrpcClient = mockk<CryptoAddressGrpcClient>(relaxed = true)
        val mockGrpcClientFactory = mockk<GrpcClientFactory>()

        every { mockGrpcClientFactory.getGrpcClient(CryptoAddressGrpcClient::class.java, any(), any()) } returns mockGrpcClient

        val repo = AddressAndMnemonicRepoImpl(
            tron = Tron(mockContext),
            ioDispatcher = ioDispatcher,
            profileRepo = mockProfileRepo,
            addressRepo = mockAddressRepo,
            grpcClientFactory = mockGrpcClientFactory
        )

        repo.addressAndMnemonic.test {
            repo.generateNewAddressAndMnemonic()
            val firstResult = awaitItem()

            repo.generateNewAddressAndMnemonic()
            val secondResult = awaitItem()

            assertTrue("Expected different mnemonic phrases", firstResult.mnemonic != secondResult.mnemonic)
            assertTrue("Expected different entropy", firstResult.addressesWithKeysForM.entropy != secondResult.addressesWithKeysForM.entropy)
            assertTrue("Expected different addresses", firstResult.addressesWithKeysForM.addresses != secondResult.addressesWithKeysForM.addresses)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getAddressFromMnemonic emits correct states`() = runTest(testScheduler) {

    }

    @Test
    fun `clearAddressFromMnemonic emits Empty state`() = runTest(testScheduler) {
        val mockProfileRepo = mockk<ProfileRepo>(relaxed = true)
        val mockAddressRepo = mockk<AddressRepo>(relaxed = true)
        val mockGrpcClient = mockk<CryptoAddressGrpcClient>(relaxed = true)
        val mockGrpcClientFactory = mockk<GrpcClientFactory>()

        every { mockGrpcClientFactory.getGrpcClient(CryptoAddressGrpcClient::class.java, any(), any()) } returns mockGrpcClient

        val repo = AddressAndMnemonicRepoImpl(
            tron = Tron(mockContext),
            ioDispatcher = ioDispatcher,
            profileRepo = mockProfileRepo,
            addressRepo = mockAddressRepo,
            grpcClientFactory = mockGrpcClientFactory
        )

        repo.addressFromMnemonic.test {
            repo.clearAddressFromMnemonic()
            assertEquals(RecoveryResult.Empty, awaitItem())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `generateAddressFromMnemonic with valid new mnemonic`() = runTest {

    }

    @Test
    fun `generateAddressFromMnemonic with existing mnemonic`() {
        // Test with a valid mnemonic that corresponds to an address already in `addressRepo`. Verify that `addressFromMnemonic` flow emits `RecoveryResult.RepeatingMnemonic`.
        // TODO implement test
    }

    @Test
    fun `generateAddressFromMnemonic with invalid mnemonic`() {
        // Test with an invalid or malformed mnemonic string. Verify that the function catches the exception, reports it to Sentry, and emits `RecoveryResult.InvalidMnemonic`.
        // TODO implement test
    }

    @Test
    fun `generateAddressFromMnemonic database check failure`() {
        // Test the scenario where `addressRepo.getAddressEntityByAddress` throws an exception. Verify the exception is caught and an appropriate error state is emitted.
        // TODO implement test
    }

    @Test
    fun `generateAddressFromMnemonic uses IO dispatcher`() = runTest(testScheduler) {
        val mockProfileRepo = mockk<ProfileRepo>(relaxed = true)
        val mockAddressRepo = mockk<AddressRepo>(relaxed = true)
        val mockGrpcClient = mockk<CryptoAddressGrpcClient>(relaxed = true)
        val mockGrpcClientFactory = mockk<GrpcClientFactory>()

        every { mockGrpcClientFactory.getGrpcClient(CryptoAddressGrpcClient::class.java, any(), any()) } returns mockGrpcClient

        val repo = AddressAndMnemonicRepoImpl(
            tron = Tron(mockContext),
            ioDispatcher = ioDispatcher,
            profileRepo = mockProfileRepo,
            addressRepo = mockAddressRepo,
            grpcClientFactory = mockGrpcClientFactory
        )

        var executedOnIoDispatcher = false

        val job = launch(ioDispatcher) {
            repo.generateAddressFromMnemonic("word ".repeat(12).trim())
            executedOnIoDispatcher = (coroutineContext[CoroutineDispatcher] as CoroutineDispatcher) == ioDispatcher
        }

        testScheduler.advanceUntilIdle()

        assertTrue("Expected generateAddressFromMnemonic to run on ioDispatcher", executedOnIoDispatcher)
        job.cancel()    }

    @Test
    fun `recoveryWallet successful with existing remote account`() {
        // Mock `cryptoAddressGrpcClient.getWalletData` to return a successful `walletData`.
        // Verify `tron.addressUtilities.recoveryKeysAndAddressBySeedPhrase` is called and `addressFromMnemonic` emits `RecoveryResult.Success` with `accountWasFound = true`.
        // TODO implement test
    }

    @Test
    fun `recoveryWallet successful with no remote account`() {
        // Mock `cryptoAddressGrpcClient.getWalletData` to return a failure with the specific message 'INTERNAL: Address not found in database'.
        // Verify `tron.addressUtilities.generateKeysAndAddressBySeedPhrase` is called and the flow emits `RecoveryResult.Success` with `accountWasFound = false`.
        // TODO implement test
    }

    @Test
    fun `recoveryWallet with gRPC communication failure`() {
        // Mock `cryptoAddressGrpcClient.getWalletData` to return a generic failure (not 'Address not found').
        // Verify that the error is captured by Sentry and the flow emits `RecoveryResult.Error`.
        // TODO implement test
    }

    @Test
    fun `recoveryWallet with gRPC client throwing exception`() {
        // Make `cryptoAddressGrpcClient.getWalletData` throw an exception (e.g., network error).
        // Verify the exception is caught, reported to Sentry, and `addressFromMnemonic` emits `RecoveryResult.Error`.
        // TODO implement test
    }

    @Test
    fun `recoveryWallet with invalid mnemonic for found account`() {
        // Mock a successful gRPC response, but make `tron.addressUtilities.recoveryKeysAndAddressBySeedPhrase` throw an exception (simulating wrong mnemonic for the account).
        // Verify the flow emits `RecoveryResult.InvalidMnemonic`.
        // TODO implement test
    }

    @Test
    fun `recoveryWallet with invalid mnemonic for new account`() {
        // Mock the 'Address not found' gRPC failure, but make `tron.addressUtilities.generateKeysAndAddressBySeedPhrase` throw an exception (simulating an invalid mnemonic format).
        // Verify the flow emits `RecoveryResult.InvalidMnemonic`.
        // TODO implement test
    }

    @Test
    fun `getProfileRepo returns correct instance`() {
        // Verify that `getProfileRepo()` returns the same instance of `ProfileRepo` that was injected into the constructor.
        // TODO implement test
    }

    @Test
    fun `getAddressRepo returns correct instance`() {
        // Verify that `getAddressRepo()` returns the same instance of `AddressRepo` that was injected into the constructor.
        // TODO implement test
    }

}
