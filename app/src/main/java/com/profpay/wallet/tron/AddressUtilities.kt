package com.profpay.wallet.tron

import android.util.Log
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toEntropy
import cash.z.ecc.android.bip39.toSeed
import com.profpay.wallet.AppConstants
import io.sentry.Sentry
import kotlinx.coroutines.withTimeout
import org.bitcoinj.base.Base58
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicHierarchy
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.HDPath
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.contract.Contract
import org.tron.trident.core.contract.Trc20Contract
import org.tron.trident.core.key.KeyPair
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

// Сущность с данными нового адреса
data class AddressGenerateResult(
    val addressesWithKeysForM: AddressesWithKeysForM,
    val mnemonic: Mnemonics.MnemonicCode,
)

data class AddressesWithKeysForM(
    val addresses: List<AddressData>,
    val privKeyBytes: ByteArray,
    val chainCode: ByteArray,
    val entropy: ByteArray,
    val derivedIndices: Iterable<Int>,
)

data class AddressData(
    val address: String,
    val privateKey: String,
    val publicKey: String,
    val indexDerivationSot: Int,
    val indexSot: Byte,
)

// Сущность с данными восстановленного адреса по мнемонике(сид-фразе)
data class AddressGenerateFromSeedPhr(
    val addressesWithKeysForM: AddressesWithKeysForM,
)

class AddressUtilities {
    // Получение сид фразы по энтропии юзера.
    fun getSeedPhraseByEntropy(entropy: ByteArray): String {
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)
        val mnemonic = mnemonicCode.words.joinToString(" ") { String(it) }
        return mnemonic
    }

    /**
     * Данный метод используется для генерации адреса и его публичного, приватного ключей.
     */
    fun generateAddressAndMnemonic(): AddressGenerateResult {
        val entropy: ByteArray = Mnemonics.WordCount.COUNT_12.toEntropy()
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)

        val addressDataList = mutableListOf<AddressData>()

        repeat(7) { index ->
            val deterministicKeyForSots: DeterministicKey =
                generateKeys(mnemonicCode.toSeed(validate = true), index)
            val addressForSots =
                public2Address(deterministicKeyForSots.pubKeyPoint.getEncoded(false))
                    ?: throw Exception("The public address has not been created!")

            addressDataList.add(
                AddressData(
                    address = addressForSots,
                    privateKey = deterministicKeyForSots.privateKeyAsHex,
                    publicKey = deterministicKeyForSots.publicKeyAsHex,
                    indexDerivationSot = index,
                    indexSot = index.toByte(),
                ),
            )
        }

        val masterKey = generateMasterPrivateKey(mnemonicCode.toSeed(validate = true))
        val addressesWithKeysForM =
            AddressesWithKeysForM(
                addresses = addressDataList,
                privKeyBytes = masterKey.privKeyBytes,
                chainCode = masterKey.chainCode,
                entropy = entropy,
                derivedIndices = (1..6),
            )

        return AddressGenerateResult(
            addressesWithKeysForM = addressesWithKeysForM,
            mnemonic = mnemonicCode,
        )
    }

    // Данный метод используется для восстановления адреса и его публичного, приватного ключей по мнемонике(сид-фразе)
    // Если адрес найден на сервере
    fun recoveryKeysAndAddressBySeedPhrase(
        seed: String,
        derivedIndices: List<Int>,
    ): AddressGenerateFromSeedPhr {
        val charArray = seed.toCharArray()
        val mnemonicCode = Mnemonics.MnemonicCode(chars = charArray)

        val addressGenerateFromSeedPhrList = mutableListOf<AddressData>()
        // Добавляем индекс 0 (используется для "первого" адреса по стандарту BIP44)
        val derivedIndicesWithZero = derivedIndices.toMutableList().apply { add(0, 0) }

        try {
            derivedIndicesWithZero
                .forEachIndexed { index, item ->
                    addressGenerateFromSeedPhrList.add(generateAddressData(mnemonicCode, item, index.toByte()))
                }

            getArchiveSots(derivedIndices)
                .forEach { item ->
                    addressGenerateFromSeedPhrList.add(generateAddressData(mnemonicCode, item, -1))
                }
        } catch (_: Exception) {
            throw Exception("Failed generate, may be uncorrect mnemonic")
        }

        val masterKey = generateMasterPrivateKey(mnemonicCode.toSeed(validate = true))
        val addressesWithKeysForM =
            AddressesWithKeysForM(
                addresses = addressGenerateFromSeedPhrList,
                privKeyBytes = masterKey.privKeyBytes,
                chainCode = masterKey.chainCode,
                entropy = mnemonicCode.toEntropy(),
                derivedIndices = derivedIndicesWithZero.filter { it != 0 },
            )

        return AddressGenerateFromSeedPhr(addressesWithKeysForM)
    }

    // Данный метод используется для восстановления адреса и его публичного, приватного ключей по мнемонике(сид-фразе)
    // Если это новая для нас фраза.
    fun generateKeysAndAddressBySeedPhrase(seed: String): AddressGenerateFromSeedPhr {
        val charArray = seed.toCharArray()
        val mnemonicCode = Mnemonics.MnemonicCode(chars = charArray)
        val addressGenerateFromSeedPhrList = mutableListOf<AddressData>()

        try {
            repeat(7) { item ->
                addressGenerateFromSeedPhrList.add(
                    generateAddressData(mnemonicCode, item, item.toByte()),
                )
            }
        } catch (_: Exception) {
            throw Exception("Failed generate, may be uncorrect mnemonic")
        }

        val masterKey = generateMasterPrivateKey(mnemonicCode.toSeed(validate = true))
        val addressesWithKeysForM =
            AddressesWithKeysForM(
                addresses = addressGenerateFromSeedPhrList,
                privKeyBytes = masterKey.privKeyBytes,
                chainCode = masterKey.chainCode,
                entropy = mnemonicCode.toEntropy(),
                derivedIndices = (1..6),
            )

        return AddressGenerateFromSeedPhr(addressesWithKeysForM)
    }

    fun getGeneralAddressBySeedPhrase(seed: String): String {
        val charArray = seed.toCharArray()
        val mnemonicCode = Mnemonics.MnemonicCode(chars = charArray)

        try {
            val deterministicKeyForSots = generateKeys(mnemonicCode.toSeed(validate = true), 0)
            val addressForSots =
                public2Address(deterministicKeyForSots.pubKeyPoint.getEncoded(false))
                    ?: throw Exception("The public address has not been created!")
            return addressForSots
        } catch (_: Exception) {
            throw Exception("Failed generate, may be uncorrect mnemonic")
        }
    }

    /**
     * Данный метод используется для получения TRC20 USDT баланса
     */
    fun getUsdtBalance(accountAddr: String): BigInteger {
        try {
            val wrapper =
                ApiWrapper(
                    AppConstants.Network.TRON_GRPC_ENDPOINT,
                    AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY,
                    KeyPair.generate().toPrivateKey(),
                )

            val contract: Contract = wrapper.getContract("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t")
            val token = Trc20Contract(contract, "TJJaVcRremausriMLkZeRedM95v7HW4j4D", wrapper)
            val balance = token.balanceOf(accountAddr)
            wrapper.close()

            return balance
        } catch (e: Exception) {
            Log.e("USDT_BALANCE", "Unexpected error: ${e.message}")
            return BigInteger.ZERO
        }
    }

    /**
     * Данный метод используется для получения TRX баланса
     */
    fun getTrxBalance(accountAddr: String): BigInteger {
        try {
            val wrapper =
                ApiWrapper(
                    AppConstants.Network.TRON_GRPC_ENDPOINT,
                    AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY,
                    KeyPair.generate().toPrivateKey(),
                )
            val balanceInSun: BigInteger = BigInteger.valueOf(wrapper.getAccountBalance(accountAddr))
            wrapper.close()
            return balanceInSun
        } catch (e: Exception) {
            Log.e("TRX_BALANCE", "Unexpected error: ${e.message}")
            return BigInteger.ZERO
        }
    }

    private fun generateMasterPrivateKey(seed: ByteArray): DeterministicKey = HDKeyDerivation.createMasterPrivateKey(seed)

    // Функция для генерации ключей по seed фразе.
    private fun generateKeys(
        seed: ByteArray,
        index: Int,
    ): DeterministicKey {
        val masterPrivateKey: DeterministicKey = generateMasterPrivateKey(seed)
        val dh = DeterministicHierarchy(masterPrivateKey)

        val path: List<ChildNumber> = HDPath.parsePath("M/44H/195H/0H/0/$index")

        val startWallet: DeterministicKey =
            dh.deriveChild(path.subList(0, path.size - 1), false, true, path[path.size - 1])

        return HDKeyDerivation.deriveChildKey(startWallet, ChildNumber(0))
    }

    fun creationOfANewCell(
        privKeyBytes: ByteArray,
        chainCode: ByteArray,
        index: Long,
    ): DeterministicKey? {
        val masterPrivateKey: DeterministicKey =
            HDKeyDerivation.createMasterPrivKeyFromBytes(privKeyBytes, chainCode)
        val dh = DeterministicHierarchy(masterPrivateKey)

        val path: List<ChildNumber> = HDPath.parsePath("M/44H/195H/0H/0/$index")

        val startWallet: DeterministicKey =
            dh.deriveChild(path.subList(0, path.size - 1), false, true, path[path.size - 1])

        return HDKeyDerivation.deriveChildKey(startWallet, ChildNumber(0))
    }

    // Вспомогательная функция для перевода HEX в Base58Check.
    fun hexToBase58CheckAddress(hex: String): String {
        val hexBytes = hex.hexStringToByteArray()
        val checksum = sha256(sha256(hexBytes)).copyOfRange(0, 4)
        return Base58.encode(hexBytes + checksum)
    }

    // Переводим массив байт в SHA256
    private fun sha256(input: ByteArray): ByteArray =
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(input)
            digest.digest()
        } catch (err: NoSuchAlgorithmException) {
            throw RuntimeException(err)
        }

    // Переводим массив байт в SHA3
    private fun sha3(input: ByteArray): ByteArray? {
        val kecc: Keccak.DigestKeccak = Keccak.Digest256()
        kecc.update(input)
        return kecc.digest()
    }

    // Переводим hexString в массив байт.
    private fun String.hexStringToByteArray(): ByteArray {
        val len = length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] =
                ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    private fun ByteArray.toHexString(): String = joinToString("") { String.format("%02x", it) }

    // Этот код реализует функцию public2Address, которая принимает публичный ключ в виде массива байтов и преобразует его в адрес в формате Tron.
    fun public2Address(publicKey: ByteArray): String? {
        val hash = sha3(publicKey.copyOfRange(1, publicKey.size))
        val address: ByteArray = hash?.copyOfRange(11, hash.size)!!

        // Устанавливаем первый байт адреса в 65 (идентификатор для Tron)
        address[0] = 65 // T symbol

        val salt = sha256(sha256(address))
        val inputCheck = ByteArray(address.size + 4)

        System.arraycopy(address, 0, inputCheck, 0, address.size)
        System.arraycopy(salt, 0, inputCheck, address.size, 4)
        return Base58.encode(inputCheck)
    }

    fun address2Public(address: String): ByteArray? {
        val inputCheck = Base58.decode(address)

        if (inputCheck.size != 25) {
            return null
        }

        val addressWithoutCheck = inputCheck.copyOfRange(0, 21)
        val salt = inputCheck.copyOfRange(21, 25)
        val hashedAddress = sha256(sha256(addressWithoutCheck))

        if (!salt.contentEquals(hashedAddress.copyOfRange(0, 4))) {
            return null
        }

        val publicKey = ByteArray(33)
        publicKey[0] = 0x04 // Uncompressed public key identifier
        System.arraycopy(addressWithoutCheck, 0, publicKey, 1, 20)

        return publicKey
    }

    fun isValidTronAddress(address: String): Boolean {
        try {
            val pattern = "^T[1-9A-HJ-NP-Za-km-z]{33}$".toRegex()
            if (!pattern.matches(address)) {
                return false
            }

            val decoded = Base58.decode(address)
            val checksum = decoded.copyOfRange(decoded.size - 4, decoded.size)

            val sha256_1 =
                MessageDigest
                    .getInstance("SHA-256")
                    .digest(decoded.copyOfRange(0, decoded.size - 4))
            val sha256_2 = MessageDigest.getInstance("SHA-256").digest(sha256_1)

            // Проверяем контрольную сумму
            val calculatedChecksum = sha256_2.copyOfRange(0, 4)

            return checksum.contentEquals(calculatedChecksum)
        } catch (_: Exception) {
            return false
        }
    }

    suspend fun isAddressActivated(address: String): Boolean {
        val wrapper =
            ApiWrapper(
                AppConstants.Network.TRON_GRPC_ENDPOINT,
                AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY,
                KeyPair.generate().toPrivateKey(),
            )

        return try {
            withTimeout(5000) {
                val res = wrapper.getAccount(address)
                res.activePermissionList.isNotEmpty()
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            false
        } finally {
            wrapper.close()
        }
    }

    fun getCreateNewAccountFeeInSystemContract(): BigInteger {
        val wrapper =
            ApiWrapper(
                AppConstants.Network.TRON_GRPC_ENDPOINT,
                AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY,
                KeyPair.generate().toPrivateKey(),
            )

        for (chainParameter in wrapper.chainParameters.chainParameterList) {
            if (chainParameter.key == "getCreateNewAccountFeeInSystemContract") {
                return BigInteger.valueOf(chainParameter.value)
            }
        }
        wrapper.close()
        return BigInteger.ZERO
    }

    private fun getArchiveSots(derivedIndices: List<Int>): List<Int> {
        val maxIndex = derivedIndices.maxOrNull() ?: return emptyList()
        val indexSet = derivedIndices.toSet()
        return (1 until maxIndex).filter { it !in indexSet }
    }

    private fun generateAddressData(
        mnemonicCode: Mnemonics.MnemonicCode,
        index: Int,
        indexSot: Byte,
    ): AddressData {
        val key = generateKeys(mnemonicCode.toSeed(validate = true), index)
        val address =
            public2Address(key.pubKeyPoint.getEncoded(false))
                ?: throw Exception("The public address has not been created!")

        return AddressData(
            address = address,
            privateKey = key.privateKeyAsHex,
            publicKey = key.publicKeyAsHex,
            indexDerivationSot = index,
            indexSot = indexSot,
        )
    }
}
