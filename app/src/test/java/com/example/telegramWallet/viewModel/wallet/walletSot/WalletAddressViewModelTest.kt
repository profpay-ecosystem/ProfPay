package com.example.telegramWallet.viewModel.wallet.walletSot

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.telegramWallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.example.telegramWallet.data.database.entities.wallet.AddressEntity
import com.example.telegramWallet.data.database.entities.wallet.TokenEntity
import com.example.telegramWallet.data.database.models.AddressWithTokens
import com.example.telegramWallet.data.database.models.TransactionModel
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.tron.Tron
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.math.BigInteger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class WalletAddressViewModelTest {
    private lateinit var viewModel: WalletAddressViewModel
    private val addressRepo: AddressRepo = mockk()
    private val transactionsRepo: TransactionsRepo = mockk()
    private val tron: Tron = mockk()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WalletAddressViewModel(addressRepo, transactionsRepo, tron)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getAddressWithTokensByAddressLD_shouldReturnCorrectLiveData() =
        runTest {
            val address = "TX1234567890ABCDEF"

            val expected =
                AddressWithTokens(
                    addressEntity =
                        AddressEntity(
                            addressId = 1,
                            walletId = 1,
                            blockchainName = "Tron",
                            address = address,
                            publicKey = "04A3F9D0B1C2D3E4F5...",
                            privateKey = "b9c0d1e2f3a4b5c6d7...",
                            isGeneralAddress = true,
                            sotIndex = 0,
                            sotDerivationIndex = 0,
                        ),
                    tokens =
                        listOf(
                            TokenEntity(
                                tokenId = 1,
                                addressId = 1,
                                tokenName = "TRX",
                                balance = BigInteger.valueOf(1_000_000),
                                frozenBalance = BigInteger.ZERO,
                            ),
                        ),
                )

            coEvery { addressRepo.getAddressWithTokensByAddressLD(address) } returns MutableLiveData(expected)

            val resultLiveData = viewModel.getAddressWithTokensByAddressLD(address)
            val result = resultLiveData.getOrAwaitValue()

            Assert.assertEquals(expected, result)
        }

    @Test
    fun getTransactionsByAddressSenderAndToken_ShouldReturnCorrectLiveData() =
        runTest {
            // Given
            val walletId = 1L
            val senderAddress = "TXabc123"
            val tokenName = "TRX"

            val transactionList =
                listOf(
                    TransactionModel(
                        transactionId = 1,
                        txId = "abcd1234",
                        senderAddressId = 1001,
                        receiverAddressId = 1002,
                        senderAddress = "TXabc123",
                        receiverAddress = "TXzzz999",
                        walletId = 1,
                        tokenName = "TRX",
                        amount = BigInteger.valueOf(1_000_000),
                        timestamp = 1_712_008_000, // Пример: 2024-05-01 в UNIX
                        status = "SUCCESS",
                        isProcessed = true,
                        serverResponseReceived = true,
                        transactionDate = "2024-05-01",
                        type = 0,
                    ),
                )

            val expectedLiveData = MutableLiveData(transactionList)

            coEvery {
                transactionsRepo.getTransactionsByAddressSenderAndTokenLD(
                    walletId,
                    senderAddress,
                    tokenName,
                )
            } returns expectedLiveData

            val result =
                viewModel.getTransactionsByAddressSenderAndTokenLD(walletId, senderAddress, tokenName)
            Assert.assertEquals(transactionList, result.getOrAwaitValue())
        }

    @Test
    fun getTransactionsByAddressReceiverAndToken_ShouldReturnCorrectLiveData() =
        runTest {
            val walletId = 1L
            val receiverAddress = "TXzzz999"
            val tokenName = "TRX"

            val expectedList =
                listOf(
                    TransactionModel(
                        transactionId = 2,
                        txId = "efgh5678",
                        senderAddressId = 1003,
                        receiverAddressId = 1004,
                        senderAddress = "TXsend000",
                        receiverAddress = receiverAddress,
                        walletId = walletId,
                        tokenName = tokenName,
                        amount = BigInteger.valueOf(2_000_000),
                        timestamp = 1_712_008_100,
                        status = "SUCCESS",
                        isProcessed = true,
                        serverResponseReceived = true,
                        transactionDate = "2024-05-01",
                        type = 1,
                    ),
                )

            val expectedLiveData = MutableLiveData(expectedList)

            coEvery {
                transactionsRepo.getTransactionsByAddressReceiverAndTokenLD(
                    walletId,
                    receiverAddress,
                    tokenName,
                )
            } returns expectedLiveData

            val result =
                viewModel
                    .getTransactionsByAddressReceiverAndTokenLD(
                        walletId,
                        receiverAddress,
                        tokenName,
                    ).getOrAwaitValue()

            Assert.assertEquals(expectedList, result)
        }

    @Test
    fun getListTransactionToTimestamp_ShouldGroupAndSortTransactionsCorrectly() =
        runTest {
            val transactions =
                listOf(
                    TransactionModel(
                        transactionId = 1,
                        txId = "tx1",
                        senderAddressId = 1,
                        receiverAddressId = 2,
                        senderAddress = "addr1",
                        receiverAddress = "addr2",
                        walletId = 1,
                        tokenName = "TRX",
                        amount = BigInteger.valueOf(100),
                        timestamp = 1712008000,
                        status = "SUCCESS",
                        transactionDate = "2024-05-02",
                        type = 0,
                    ),
                    TransactionModel(
                        transactionId = 2,
                        txId = "tx2",
                        senderAddressId = 1,
                        receiverAddressId = 3,
                        senderAddress = "addr1",
                        receiverAddress = "addr3",
                        walletId = 1,
                        tokenName = "TRX",
                        amount = BigInteger.valueOf(200),
                        timestamp = 1712009000,
                        status = "SUCCESS",
                        transactionDate = "2024-05-01",
                        type = 0,
                    ),
                    TransactionModel(
                        transactionId = 3,
                        txId = "tx3",
                        senderAddressId = 2,
                        receiverAddressId = 3,
                        senderAddress = "addr2",
                        receiverAddress = "addr3",
                        walletId = 1,
                        tokenName = "TRX",
                        amount = BigInteger.valueOf(300),
                        timestamp = 1712009100,
                        status = "SUCCESS",
                        transactionDate = "2024-05-01",
                        type = 0,
                    ),
                )

            val result = viewModel.getListTransactionToTimestamp(transactions)

            Assert.assertEquals(2, result.size)
            Assert.assertEquals("2024-05-01", result[0][0]?.transactionDate)
            Assert.assertEquals("2024-05-02", result[1][0]?.transactionDate)

            // Проверим сортировку внутри первой группы
            Assert.assertEquals("tx3", result[0][0]?.txId) // timestamp 1712009100
            Assert.assertEquals("tx2", result[0][1]?.txId) // timestamp 1712009000
        }

    fun <T> LiveData<T>.getOrAwaitValue(): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer =
            object : Observer<T> {
                override fun onChanged(value: T) {
                    data = value
                    latch.countDown()
                    this@getOrAwaitValue.removeObserver(this)
                }
            }
        this.observeForever(observer)
        latch.await(5, TimeUnit.SECONDS)
        return data ?: throw NullPointerException("LiveData value was null")
    }
}
