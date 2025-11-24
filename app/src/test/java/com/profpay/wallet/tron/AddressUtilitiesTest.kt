package com.profpay.wallet.tron

import cash.z.ecc.android.bip39.Mnemonics
import io.mockk.coEvery
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class AddressUtilitiesTest {
    private lateinit var utils: AddressUtilities

    @BeforeEach
    fun setup() {
        utils = spyk(AddressUtilities())
    }

    private fun mockAddress(index: Int, sot: Byte = -1) =
        AddressDataWithoutPrivKey(
            address = "ADDR_$index",
            publicKey = "PUB_$index",
            indexDerivationSot = index,
            indexSot = sot
        )

    @Test
    fun `test generateNextAddressGroup with no activated addresses`() = runTest {
        every { utils["generateAddressData"](any<Mnemonics.MnemonicCode>(), any<Int>(), any<Byte>()) } answers {
            val idx = arg<Int>(1)
            val sot = arg<Byte>(2)
            mockAddress(idx, sot)
        }

        coEvery { utils.isAddressActivated(any()) } returns false

        val seed = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        val result = utils.generateNextAddressGroup(seed)

        val addresses = result.addressesWithKeysForM.addresses
        val derived = result.addressesWithKeysForM.derivedIndices.toList()

        // Проверяем, что 0-й адрес есть
        assertTrue(addresses.any { it.indexDerivationSot == 0 })

        // Проверяем, что найденная группа 6 адресов начинается с 1
        val hiveAddrs = addresses.filter { it.indexDerivationSot in 1..6 }
        assertEquals(6, hiveAddrs.size)

        // Проверяем, что derivedIndices корректны
        assertEquals(listOf(0,1,2,3,4,5,6), derived)

        // Проверяем, что архивные индексы пустые
        val archiveAddrs = addresses.filter { it.indexSot.toInt() == -1 }
        assertTrue(archiveAddrs.isEmpty())
    }

    @Test
    fun `test generateNextAddressGroup with first 6 addresses activated`() = runTest {
        every { utils["generateAddressData"](any<Mnemonics.MnemonicCode>(), any<Int>(), any<Byte>()) } answers {
            val idx = arg<Int>(1)
            val sot = arg<Byte>(2)
            mockAddress(idx, sot)
        }

        coEvery { utils.isAddressActivated(any()) } answers {
            val addr = arg<String>(0)
            val idx = addr.removePrefix("ADDR_").toInt()
            idx in 1..6  // активируем первые 6 адресов
        }

        val seed = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        val result = utils.generateNextAddressGroup(seed)

        val addresses = result.addressesWithKeysForM.addresses
        val derived = result.addressesWithKeysForM.derivedIndices.toList()

        // Проверяем, что 0-й адрес есть
        assertTrue(addresses.any { it.indexDerivationSot == 0 })

        // Проверяем, что найденная группа 6 адресов начинается с 7
        val hiveAddrs = addresses.filter { it.indexDerivationSot in 7..12 }
        assertEquals(6, hiveAddrs.size)

        // Проверяем derivedIndices
        assertEquals(listOf(0, 7, 8, 9, 10, 11, 12), derived)

        // Проверяем архивные индексы (1–6)
        val archiveAddrs = addresses.filter { it.indexSot.toInt() == -1 }
        assertEquals((1..6).toList(), archiveAddrs.map { it.indexDerivationSot })
    }

    @Test
    fun `test generateNextAddressGroup with gap in activated addresses`() = runTest {
        every {
            utils["generateAddressData"](any<Mnemonics.MnemonicCode>(), any<Int>(), any<Byte>())
        } answers {
            val index = arg<Int>(1)
            mockAddress(index, index.toByte())
        }

        coEvery { utils.isAddressActivated(any()) } answers {
            val addr = arg<String>(0)
            val idx = addr.removePrefix("ADDR_").toInt()
            idx == 1 || idx == 5 || idx == 7
        }

        val seed = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        val result = utils.generateNextAddressGroup(seed)

        val addresses = result.addressesWithKeysForM.addresses
        val derived = result.addressesWithKeysForM.derivedIndices.toList()

        // 0-й индекс всегда есть
        assertTrue(addresses.any { it.indexDerivationSot == 0 })

        // Хайв должен начинаться с 8: 8..13
        val hiveExpected = listOf(8, 9, 10, 11, 12, 13)
        val hiveActual = addresses
            .filter { it.indexDerivationSot in hiveExpected }
            .map { it.indexDerivationSot }

        assertEquals(hiveExpected, hiveActual.sorted())

        // derivedIndices
        assertEquals(listOf(0) + hiveExpected, derived)
    }

    @Test
    fun `test generateNextAddressGroup with invalid seed throws exception`() = runTest {
        val invalidSeed = "this is not a valid mnemonic phrase"

        val exception = assertFailsWith<Exception> {
            utils.generateNextAddressGroup(invalidSeed)
        }

        println("Caught expected exception: ${exception.message}")
    }

    @Test
    fun `test behavior when isAddressActivated fails`() = runTest {
        every {
            utils["generateAddressData"](any<Mnemonics.MnemonicCode>(), any<Int>(), any<Byte>())
        } answers {
            val index = arg<Int>(1)
            mockAddress(index, index.toByte())
        }

        coEvery { utils.isAddressActivated(any()) } throws RuntimeException("Network error")

        val seed = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"

        // Проверяем, что исключение пробрасывается
        val exception = assertFailsWith<RuntimeException> {
            utils.generateNextAddressGroup(seed)
        }

        assertEquals("Network error", exception.message)
    }

    @Test
    fun `test very large index for first inactive hive`() = runTest {
        every {
            utils["generateAddressData"](any<Mnemonics.MnemonicCode>(), any<Int>(), any<Byte>())
        } answers {
            val index = arg<Int>(1)
            mockAddress(index, index.toByte())
        }

        coEvery { utils.isAddressActivated(any()) } answers {
            val addr = arg<String>(0)
            val idx = addr.removePrefix("ADDR_").toInt()
            idx in 1..999  // активируем первые 6 адресов
        }

        val seed = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"

        val result = utils.generateNextAddressGroup(seed)
        val addresses = result.addressesWithKeysForM.addresses
        val derived = result.addressesWithKeysForM.derivedIndices

        // Проверяем, что найденный индекс = 1000
        val expectedHive = (1000..1005).toList()
        assertEquals(listOf(0) + expectedHive, derived)

        // Проверяем, что 6 hive адресов созданы
        val hiveAddrs = addresses.filter { it.indexDerivationSot in 1000..1005 }
        assertEquals(6, hiveAddrs.size)

        // Проверяем, что 0-й адрес присутствует
        assertTrue(addresses.any { it.indexDerivationSot == 0 })
    }

    @Test
    fun `test with empty seed phrase`() = runTest {
        val emptySeed = ""

        val exception = assertFailsWith<Exception> {
            utils.generateNextAddressGroup(emptySeed)
        }

        println("Caught exception: ${exception.message}")
    }

    @Test
    fun `test archive address calculation with non sequential activated indices`() = runTest {
        every { utils["generateAddressData"](any<Mnemonics.MnemonicCode>(), any<Int>(), any<Byte>()) } answers {
            val idx = arg<Int>(1)
            val sot = arg<Byte>(2)
            mockAddress(idx, sot)
        }

        coEvery { utils.isAddressActivated(any()) } answers {
            val addr = arg<String>(0)
            val idx = addr.removePrefix("ADDR_").toInt()
            idx in listOf(1, 3, 5)
        }

        val seed = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        val result = utils.generateNextAddressGroup(seed)

        val addresses = result.addressesWithKeysForM.addresses
        val derived = result.addressesWithKeysForM.derivedIndices

        assertTrue(addresses.any { it.indexDerivationSot == 0 })

        // Архивные должны быть все пропущенные между 1 и найденным max (1,3,5)
        val archiveAddrs = addresses.filter { it.indexSot.toInt() == -1 }
        val archiveIndices = archiveAddrs.map { it.indexDerivationSot }
        assertEquals(listOf(1, 2, 3, 4, 5), archiveIndices.sorted()) // 2 и 4 пропущенные

        // Проверяем что hive был создан корректно
        val hiveAddrs = addresses.filter { it.indexDerivationSot >= (derived.maxOrNull()!! - 5) }
        assertEquals(6, hiveAddrs.size)
    }

    @Test
    fun `test index 0 address is always included and correct`() = runTest {
        every { utils["generateAddressData"](any<Mnemonics.MnemonicCode>(), any<Int>(), any<Byte>()) } answers {
            val idx = arg<Int>(1)
            val sot = arg<Byte>(2)
            mockAddress(idx, sot)
        }

        coEvery { utils.isAddressActivated(any()) } returns false

        val seed = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        val result = utils.generateNextAddressGroup(seed)

        val addresses = result.addressesWithKeysForM.addresses

        // Проверяем, что 0-й адрес есть
        val zeroAddress = addresses.find { it.indexDerivationSot == 0 }
        assertNotNull(zeroAddress)

        // Проверяем корректность данных
        assertEquals("ADDR_0", zeroAddress!!.address)
        assertEquals("PUB_0", zeroAddress.publicKey)
        assertEquals(0, zeroAddress.indexDerivationSot)
        assertEquals(0.toByte(), zeroAddress.indexSot)
    }

    @Test
    fun `test hive found immediately after a single inactive address`() = runTest {
        // Сценарий: 1-й адрес активен, 2-й неактивен, 3-8 активны → hive начинается с 2
        every { utils["generateAddressData"](any<Mnemonics.MnemonicCode>(), any<Int>(), any<Byte>()) } answers {
            val idx = arg<Int>(1)
            val sot = arg<Byte>(2)
            mockAddress(idx, sot)
        }

        coEvery { utils.isAddressActivated(any()) } answers {
            val addr = arg<String>(0)
            val idx = addr.removePrefix("ADDR_").toInt()
            when (idx) {
                1,3,4,5,6,7,8 -> true
                else -> false
            }
        }

        val seed = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        val result = utils.generateNextAddressGroup(seed)

        val addresses = result.addressesWithKeysForM.addresses

        // Проверяем, что hive начинается с первого неактивного (2-й)
        val expectedHiveStart = 2
        val hiveAddrs = addresses.filter { it.indexDerivationSot in expectedHiveStart..expectedHiveStart+5 }
        assertEquals(6, hiveAddrs.size)
        assertTrue(hiveAddrs.all { it.indexDerivationSot in expectedHiveStart..expectedHiveStart+5 })

        // Проверяем архивные адреса (все активные до hive)
        val archiveAddrs = addresses.filter { it.indexSot.toInt() == -1 }
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8), archiveAddrs.map { it.indexDerivationSot })

        // Проверяем, что 0-й адрес есть
        val zeroAddress = addresses.find { it.indexDerivationSot == 0 }
        assertNotNull(zeroAddress)
    }
}
