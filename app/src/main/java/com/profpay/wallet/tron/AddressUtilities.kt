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
    val addresses: List<AddressDataWithoutPrivKey>,
    val entropy: ByteArray,
    val derivedIndices: Iterable<Int>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressesWithKeysForM

        if (addresses != other.addresses) return false
        if (!entropy.contentEquals(other.entropy)) return false
        if (derivedIndices != other.derivedIndices) return false

        return true
    }

    override fun hashCode(): Int {
        var result = addresses.hashCode()
        result = 31 * result + entropy.contentHashCode()
        result = 31 * result + derivedIndices.hashCode()
        return result
    }
}

data class AddressDataWithoutPrivKey(
    val address: String,
    val publicKey: String,
    val indexDerivationSot: Int,
    val indexSot: Byte,
)

data class AddressDataWithPrivKey(
    val address: String,
    val publicKey: String,
    val privateKey: String,
)

// Сущность с данными восстановленного адреса по мнемонике(сид-фразе)
data class AddressGenerateFromSeedPhr(
    val addressesWithKeysForM: AddressesWithKeysForM,
)

class AddressUtilities {
    /**
     * Преобразует энтропию (байтовый массив) в мнемоническую фразу (seed-фразу).
     *
     * Эта функция использует стандарт BIP-39 для создания последовательности слов,
     * которая является человекочитаемым представлением исходной энтропии.
     *
     * @param entropy Байтовый массив, представляющий энтропию. Длина массива должна
     *                соответствовать требованиям BIP-39 (например, 16 байт для 12 слов).
     * @return [String] — мнемоническая фраза, состоящая из слов, разделенных пробелами.
     */
    fun getSeedPhraseByEntropy(entropy: ByteArray): String {
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)
        val mnemonic = mnemonicCode.words.joinToString(" ") { String(it) }
        return mnemonic
    }

    /**
     * Генерирует один новый TRON-адрес вместе с его приватным и публичным ключами.
     *
     * Этот метод выполняет следующие шаги:
     * 1. Генерирует случайную энтропию для 12-словной мнемонической фразы (BIP-39).
     * 2. Создает мнемоническую фразу из энтропии.
     * 3. Извлекает seed из мнемонической фразы.
     * 4. Генерирует детерминированную пару ключей для первого индекса деривации (index = 0)
     *    согласно пути BIP-44 (`M/44H/195H/0H/0/0`).
     * 5. Создает TRON-совместимый адрес из публичного ключа.
     *
     * @return Объект [AddressDataWithPrivKey], содержащий сгенерированный адрес,
     *         публичный ключ (в hex) и приватный ключ (в hex).
     * @throws IllegalStateException если не удалось создать адрес из публичного ключа.
     */
    fun generateSingleAddress(): AddressDataWithPrivKey {
        // Генерация энтропии для 12-словной мнемонической фразы (BIP-39)
        val entropy: ByteArray = Mnemonics.WordCount.COUNT_12.toEntropy()

        // Создание мнемоники из энтропии
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)

        // Индекс в дереве деривации (для первого ключа = 0)
        val index = 0

        // Получение детерминированного ключа (приватного + публичного)
        val deterministicKey: DeterministicKey = generateKeys(mnemonicCode.toSeed(validate = true), index)

        // Генерация адреса из публичного ключа
        val address =
            public2Address(deterministicKey.pubKeyPoint.getEncoded(false))
                ?: throw IllegalStateException("Не удалось создать публичный адрес")

        return AddressDataWithPrivKey(
            address = address,
            publicKey = deterministicKey.publicKeyAsHex,
            privateKey = deterministicKey.privateKeyAsHex,
        )
    }

    /**
     * Получает приватный ключ из энтропии и индекса деривации.
     *
     * Эта функция преобразует предоставленную энтропию в мнемоническую фразу по стандарту BIP-39,
     * затем генерирует из этой мнемоники seed. Используя seed, она получает
     * детерминированный ключ в соответствии с указанным индексом иерархического пути деривации.
     *
     * @param entropy Байтовый массив энтропии.
     * @param index Индекс в иерархическом пути деривации.
     * @return [ByteArray], представляющий приватный ключ.
     * @throws IllegalArgumentException если энтропия невалидна или не удалось сгенерировать мнемонику.
     */
    fun derivePrivateKeyFromEntropy(
        entropy: ByteArray,
        index: Int,
    ): ByteArray {
        // Генерация мнемонической фразы из энтропии по стандарту BIP-39
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)
        // Получение seed из мнемоники (с проверкой контрольной суммы)
        val seed = mnemonicCode.toSeed(validate = true)

        // Деривация детерминированного ключа по указанному индексу
        val deterministicKey: DeterministicKey = generateKeys(seed, index)
        deterministicKey.privateKeyAsHex

        // Возврат приватного ключа в виде байтового массива
        return deterministicKey.privKeyBytes
    }

    /**
     * Генерирует новую 12-словную мнемоническую фразу (BIP-39) и на её основе создает 7 TRON-адресов.
     *
     * Процесс включает следующие шаги:
     * 1.  Генерация случайной энтропии для 12-словной мнемонической фразы.
     * 2.  Создание мнемоники из энтропии.
     * 3.  Получение seed из мнемоники.
     * 4.  Иерархическая деривация 7 ключей (по пути `M/44H/195H/0H/0/i`, где `i` от 0 до 6) согласно BIP-44 для TRON (coin type 195).
     * 5.  Генерация TRON-адресов и публичных ключей для каждого деривированного ключа.
     *
     * @return [AddressGenerateResult] — объект, содержащий:
     *         - `mnemonic`: сгенерированная мнемоническая фраза.
     *         - `addressesWithKeysForM`: данные, включающие список из 7 адресов с их публичными ключами,
     *           индексами деривации, а также исходную энтропию.
     * @throws Exception если не удалось создать публичный адрес в процессе генерации.
     */
    fun generateAddressAndMnemonic(): AddressGenerateResult {
        val entropy: ByteArray = Mnemonics.WordCount.COUNT_12.toEntropy()
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)

        val addressDataList = mutableListOf<AddressDataWithoutPrivKey>()

        repeat(7) { index ->
            val deterministicKeyForSots: DeterministicKey =
                generateKeys(mnemonicCode.toSeed(validate = true), index)
            val addressForSots =
                public2Address(deterministicKeyForSots.pubKeyPoint.getEncoded(false))
                    ?: throw Exception("The public address has not been created!")

            addressDataList.add(
                AddressDataWithoutPrivKey(
                    address = addressForSots,
                    publicKey = deterministicKeyForSots.publicKeyAsHex,
                    indexDerivationSot = index,
                    indexSot = index.toByte(),
                ),
            )
        }

        val addressesWithKeysForM =
            AddressesWithKeysForM(
                addresses = addressDataList,
                entropy = entropy,
                derivedIndices = (1..6),
            )

        return AddressGenerateResult(
            addressesWithKeysForM = addressesWithKeysForM,
            mnemonic = mnemonicCode,
        )
    }

    /**
     * Восстанавливает адреса и ключи из заданной сид-фразы и списка индексов деривации.
     *
     * Эта функция используется при восстановлении кошелька, когда индексы деривации существующих
     * адресов уже известны (например, получены с сервера). Она генерирует адреса
     * для предоставленных `derivedIndices`, а также для любых "архивных" или пропущенных индексов
     * между 1 и максимальным предоставленным индексом. Функция всегда включает адрес для индекса 0,
     * который является стандартным основным адресом согласно BIP44.
     *
     * Процесс включает в себя:
     * 1. Преобразование строки сид-фразы в объект `MnemonicCode`.
     * 2. Создание списка индексов для деривации, который включает 0, `derivedIndices` и любые архивные индексы.
     * 3. Деривацию HD (иерархически детерминированного) ключа для каждого индекса.
     * 4. Генерацию соответствующего адреса Tron и публичного ключа из каждого производного ключа.
     * 5. Сборку результатов, включая исходную энтропию и список использованных индексов деривации (кроме 0),
     *    в объект `AddressGenerateFromSeedPhr`.
     *
     * @param seed Сид-фраза (мнемоническая фраза) из 12 слов по стандарту BIP-39 в виде строки.
     * @param derivedIndices Список целочисленных индексов деривации для адресов, которые необходимо восстановить.
     * @return [AddressGenerateFromSeedPhr] — объект, содержащий список восстановленных адресов
     *   (с публичными ключами и информацией о деривации) и энтропию сид-фразы.
     * @throws Exception если генерация адреса не удалась, что может указывать на неверную или невалидную мнемоническую фразу.
     */
    fun recoveryKeysAndAddressBySeedPhrase(
        seed: String,
        derivedIndices: List<Int>,
    ): AddressGenerateFromSeedPhr {
        val charArray = seed.toCharArray()
        val mnemonicCode = Mnemonics.MnemonicCode(chars = charArray)

        val addressGenerateFromSeedPhrList = mutableListOf<AddressDataWithoutPrivKey>()
        // Добавляем индекс 0 (используется для "первого" адреса по стандарту BIP44)
        val derivedIndicesWithZero = listOf(0) + derivedIndices

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

        val addressesWithKeysForM =
            AddressesWithKeysForM(
                addresses = addressGenerateFromSeedPhrList,
                entropy = mnemonicCode.toEntropy(),
                derivedIndices = derivedIndicesWithZero.filter { it != 0 },
            )

        return AddressGenerateFromSeedPhr(addressesWithKeysForM)
    }

    /**
     * Генерирует набор адресов на основе предоставленной seed-фразы.
     *
     * Этот метод предназначен для случаев, когда пользователь вводит новую для системы seed-фразу.
     * Он создает 7 адресов по стандартному пути деривации BIP44 (`M/44H/195H/0H/0/i`),
     * используя индексы от 0 до 6.
     *
     * 1.  Преобразует строковую seed-фразу в объект `MnemonicCode`.
     * 2.  В цикле генерирует 7 адресов, используя индексы от 0 до 6.
     * 3.  Собирает сгенерированные данные (адреса, энтропию) в объект `AddressesWithKeysForM`.
     * 4.  Возвращает результат в обертке `AddressGenerateFromSeedPhr`.
     *
     * @param seed Строковое представление seed-фразы (мнемонической фразы).
     * @return [AddressGenerateFromSeedPhr] — объект, содержащий список сгенерированных адресов
     *         и связанную с ними информацию.
     * @throws Exception если мнемоническая фраза некорректна или не удалось создать адрес.
     */
    fun generateKeysAndAddressBySeedPhrase(seed: String): AddressGenerateFromSeedPhr {
        val charArray = seed.toCharArray()
        val mnemonicCode = Mnemonics.MnemonicCode(chars = charArray)
        val addressGenerateFromSeedPhrList = mutableListOf<AddressDataWithoutPrivKey>()

        repeat(7) { item ->
            addressGenerateFromSeedPhrList.add(
                generateAddressData(mnemonicCode, item, item.toByte()),
            )
        }

        val addressesWithKeysForM =
            AddressesWithKeysForM(
                addresses = addressGenerateFromSeedPhrList,
                entropy = mnemonicCode.toEntropy(),
                derivedIndices = (1..6),
            )

        return AddressGenerateFromSeedPhr(addressesWithKeysForM)
    }

    /**
     * Получает основной TRON-адрес из заданной мнемонической seed-фразы (BIP-39).
     *
     * Эта функция следует стандартной иерархической детерминированной (HD) структуре кошелька (BIP-44)
     * для генерации главного адреса. Она использует путь деривации `M/44H/195H/0H/0/0`,
     * где `195` — это тип монеты для TRON, а последний индекс `0` соответствует первому,
     * или "общему", адресу.
     *
     * @param seed Мнемоническая seed-фраза по стандарту BIP-39 (например, "слово1 слово2 ... слово12").
     * @return Строка с TRON-адресом в формате Base58Check (например, "T...").
     * @throws Exception если мнемоническая фраза недействительна или если процесс генерации адреса завершается с ошибкой по любой причине.
     */
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
     * Получает баланс TRC20 USDT для указанного адреса Tron-аккаунта.
     *
     * Эта функция подключается к сети Tron, взаимодействует с официальным
     * контрактом USDT TRC20 и запрашивает баланс токенов для указанного адреса.
     *
     * Баланс возвращается в наименьших единицах (например, если баланс составляет 1 USDT,
     * а у USDT 6 десятичных знаков, функция вернёт 1_000_000).
     *
     * В случае сетевой ошибки, сбоя подключения или любого другого исключения,
     * функция логирует ошибку и возвращает `BigInteger.ZERO`.
     *
     * @param accountAddr Адрес Tron (например, "TJJaVcRremausriMLkZeRedM95v7HW4j4D"),
     *                    для которого необходимо получить баланс USDT.
     * @return [BigInteger], представляющий баланс USDT в наименьших единицах,
     *         или `BigInteger.ZERO` в случае ошибки.
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
     * Получает баланс TRX для указанного адреса аккаунта Tron.
     *
     * Эта функция подключается к сети Tron через gRPC, запрашивает баланс для указанного
     * адреса и возвращает сумму в SUN (наименьшая единица TRX).
     * В случае любой ошибки во время вызова API (например, проблемы с сетью, неверный адрес),
     * функция логирует ошибку и возвращает нулевой баланс.
     *
     * @param accountAddr Адрес Tron в формате base58, для которого необходимо получить баланс.
     * @return [BigInteger], представляющий баланс TRX аккаунта в SUN. Возвращает `BigInteger.ZERO` в случае ошибки.
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

    /**
     * Генерирует главный приватный ключ из заданного seed.
     *
     * Эта функция является оберткой для метода `createMasterPrivateKey` из библиотеки `bitcoinj`,
     * который является фундаментальным шагом в создании иерархически детерминированного (HD) кошелька.
     * Главный ключ является корнем всей иерархии ключей.
     *
     * @param seed Байтовый массив seed, обычно генерируемый из мнемонической фразы в соответствии с BIP-39.
     * @return Объект [DeterministicKey], представляющий главный приватный ключ для HD-кошелька.
     */
    private fun generateMasterPrivateKey(seed: ByteArray): DeterministicKey = HDKeyDerivation.createMasterPrivateKey(seed)

    /**
     * Генерирует детерминированный ключ из seed и индекса деривации.
     *
     * Эта функция следует стандарту BIP-44 для иерархических детерминированных кошельков.
     * Она принимает seed (полученный из мнемонической фразы) и индекс для генерации
     * определенной пары ключей (приватного и публичного).
     *
     * Используется путь деривации "M/44H/195H/0H/0/$index", где:
     * - `M` — это мастер-ключ.
     * - `44H` означает стандарт BIP-44.
     * - `195H` — зарегистрированный тип монеты для TRON (TRX).
     * - `0H` — индекс аккаунта.
     * - `0` — для внешней цепи (адреса для получения).
     * - `$index` — конкретный индекс адреса.
     *
     * @param seed Байтовый массив seed, обычно генерируемый из мнемонической фразы.
     * @param index Индекс адреса в пути деривации.
     * @return [DeterministicKey] Объект ключа, содержащий как приватный, так и публичный ключ.
     */
    private fun generateKeys(
        seed: ByteArray,
        index: Int,
    ): DeterministicKey {
        val masterPrivateKey: DeterministicKey = generateMasterPrivateKey(seed)
        val dh = DeterministicHierarchy(masterPrivateKey)

        val path: List<ChildNumber> = HDPath.parsePath("M/44H/195H/0H/0/$index")

        val key: DeterministicKey = dh.get(path, true, true)
        return key
    }

    /**
     * Создает новый детерминированный ключ для определенной "ячейки" или адреса на основе энтропии и индекса.
     *
     * Эта функция выводит ключ в соответствии со стандартом BIP-44 для Tron (тип монеты 195).
     * Она принимает корневую энтропию, преобразует ее в seed, а затем использует иерархический
     * путь деривации для генерации конкретного ключа.
     *
     * Используемый путь деривации: `M/44H/195H/0H/0/{index}`.
     *
     * @param entropy Байтовый массив исходной энтропии, обычно получаемый из 12-словной мнемонической фразы.
     * @param index Конкретный индекс для адреса, который необходимо получить. Это последний компонент HD-пути.
     * @return [DeterministicKey], содержащий публичный и приватный ключ для указанного индекса. В случае сбоя текущая реализация выбрасывает исключение.
     */
    fun creationOfANewCell(
        entropy: ByteArray,
        index: Long,
    ): DeterministicKey? {
        val mnemonicCode = Mnemonics.MnemonicCode(entropy)
        val masterKey = generateMasterPrivateKey(mnemonicCode.toSeed(validate = true))

        val dh = DeterministicHierarchy(masterKey)

        val path: List<ChildNumber> = HDPath.parsePath("M/44H/195H/0H/0/$index")

        val key: DeterministicKey = dh.get(path, true, true)
        return key
    }

    /**
     * Преобразует адрес из шестнадцатеричной строки (HEX) в формат Base58Check.
     *
     * Этот метод выполняет следующие шаги:
     * 1. Декодирует HEX-строку в массив байтов.
     * 2. Вычисляет контрольную сумму, дважды применяя хеш-функцию SHA-256 к байтам адреса и беря первые 4 байта результата.
     * 3. Добавляет контрольную сумму в конец массива байтов адреса.
     * 4. Кодирует полученный массив байтов в строку формата Base58.
     *
     * @param hex Входной адрес в виде шестнадцатеричной строки (например, Tron-адрес, начинающийся с "41").
     * @return Адрес, закодированный в формате Base58Check (например, Tron-адрес, начинающийся с "T").
     */
    fun hexToBase58CheckAddress(hex: String): String {
        val hexBytes = hex.hexStringToByteArray()
        val checksum = sha256(sha256(hexBytes)).copyOfRange(0, 4)
        return Base58.encode(hexBytes + checksum)
    }

    /**
     * Вычисляет хеш SHA-256 для входного массива байтов.
     *
     * Это вспомогательная функция, которая принимает массив байтов, вычисляет для него
     * хеш SHA-256 и возвращает результат в виде другого массива байтов.
     * Для вычислений используется класс `MessageDigest` из Java Security API.
     *
     * @param input Массив байтов для хеширования.
     * @return Хеш SHA-256 в виде [ByteArray].
     * @throws RuntimeException если алгоритм SHA-256 недоступен в системе,
     *         оборачивая оригинальное исключение [NoSuchAlgorithmException].
     */
    private fun sha256(input: ByteArray): ByteArray =
        try {
            val digest = MessageDigest.getInstance(AppConstants.Application.HASH_ALGORITHM)
            digest.update(input)
            digest.digest()
        } catch (err: NoSuchAlgorithmException) {
            throw RuntimeException(err)
        }

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

            val sha256First =
                MessageDigest
                    .getInstance("SHA-256")
                    .digest(decoded.copyOfRange(0, decoded.size - 4))
            val sha256Second = MessageDigest.getInstance("SHA-256").digest(sha256First)

            // Проверяем контрольную сумму
            val calculatedChecksum = sha256Second.copyOfRange(0, 4)

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
    ): AddressDataWithoutPrivKey {
        val key = generateKeys(mnemonicCode.toSeed(validate = true), index)
        val address =
            public2Address(key.pubKeyPoint.getEncoded(false))
                ?: throw Exception("The public address has not been created!")

        return AddressDataWithoutPrivKey(
            address = address,
            publicKey = key.publicKeyAsHex,
            indexDerivationSot = index,
            indexSot = indexSot,
        )
    }
}
