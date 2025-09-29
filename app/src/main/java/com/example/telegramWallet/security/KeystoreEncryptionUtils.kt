package com.example.telegramWallet.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreEncryptionUtils {
    private val keyAlias = "ProfWalletKey"
    private val androidKeyStore = "AndroidKeyStore"
    private val transformation = "AES/GCM/NoPadding"
    private val ivSize = 12

    init {
        generateKeyIfNeeded()
    }

    private fun generateKeyIfNeeded() {
        val keyStore = KeyStore.getInstance(androidKeyStore)
        keyStore.load(null)
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, androidKeyStore)
            keyGenerator.init(
                KeyGenParameterSpec
                    .Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(androidKeyStore)
        keyStore.load(null)
        return keyStore.getKey(keyAlias, null) as SecretKey
    }

    fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data)
        return iv + encryptedData
    }

    @Throws(GeneralSecurityException::class)
    fun decrypt(encryptedDataWithIv: ByteArray): ByteArray {
        val iv = encryptedDataWithIv.copyOfRange(0, ivSize)
        val encryptedData = encryptedDataWithIv.copyOfRange(ivSize, encryptedDataWithIv.size)
        val cipher = Cipher.getInstance(transformation)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        return cipher.doFinal(encryptedData)
    }
}
