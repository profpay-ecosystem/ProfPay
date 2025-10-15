package com.profpay.wallet.security

import android.content.Context
import android.content.pm.PackageManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreCryptoManager(
    private val context: Context,
) {
    /**
     * Создает AES ключ в Android Keystore
     *
     * @param alias имя ключа
     */
    fun createAesKey(alias: String) {
        val keyGenerator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore",
            )

        val builder =
            KeyGenParameterSpec
                .Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                // Не трогать
                .setInvalidatedByBiometricEnrollment(false)
                .setKeySize(256)

        val hasStrongBox =
            context.packageManager.hasSystemFeature(
                PackageManager.FEATURE_STRONGBOX_KEYSTORE,
            )
        if (hasStrongBox) {
            builder.setIsStrongBoxBacked(true)
        }

        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }

    /**
     * Шифрование данных
     *
     * @param alias имя ключа
     * @param data байты для шифрования
     * @return Pair(iv, cipherText)
     */
    fun encrypt(
        alias: String,
        data: ByteArray,
    ): Pair<ByteArray, ByteArray> {
        val secretKey = getSecretKey(alias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv // 12 байт для GCM
        val cipherText = cipher.doFinal(data)

        // Очистка исходного массива
        data.fill(0)

        return Pair(iv, cipherText)
    }

    /**
     * Дешифровка данных
     *
     * @param alias имя ключа
     * @param iv вектор инициализации
     * @param cipherText зашифрованные данные
     * @return ByteArray
     */
    fun decrypt(
        alias: String,
        iv: ByteArray,
        cipherText: ByteArray,
    ): ByteArray {
        val secretKey = getSecretKey(alias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        return cipher.doFinal(cipherText)
    }

    /**
     * Получает SecretKey из Keystore
     *
     * @param alias имя ключа
     * @return SecretKey
     */
    private fun getSecretKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return keyStore.getKey(alias, null) as SecretKey
    }
}
