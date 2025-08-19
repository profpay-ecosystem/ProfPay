package com.example.telegramWallet.data.flow_db.token

import android.content.Context
import android.util.Base64
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.telegramWallet.AppConstants
import com.example.telegramWallet.PrefKeys
import com.example.telegramWallet.R
import com.example.telegramWallet.backend.grpc.AuthGrpcClient
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.security.KeystoreEncryptionUtils
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import javax.inject.Inject

class SharedPrefsTokenProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val profileRepo: ProfileRepo,
    private val grpcClientFactory: Lazy<GrpcClientFactory>
) : TokenProvider {
    private val keystore = KeystoreEncryptionUtils()
    private val authGrpcClient: AuthGrpcClient by lazy {
        grpcClientFactory.get().getGrpcClient(
            AuthGrpcClient::class.java,
            AppConstants.Network.GRPC_ENDPOINT,
            AppConstants.Network.GRPC_PORT
        )
    }

    private val prefs = context.getSharedPreferences(
        ContextCompat.getString(context, R.string.preference_file_key),
        Context.MODE_PRIVATE
    )

    override fun getAccessToken(): String =
        prefs.getString(PrefKeys.JWT_ACCESS_TOKEN, null)?.let { decryptBase64(it) } ?: ""

    override fun getRefreshToken(): String =
        prefs.getString(PrefKeys.JWT_REFRESH_TOKEN, null)?.let { decryptBase64(it) } ?: ""

    override suspend fun refreshTokensIfNeeded() {
        val userId = profileRepo.getProfileUserId()
        val appId = profileRepo.getProfileAppId()
        val deviceToken = profileRepo.getDeviceToken() ?: return

        if (getRefreshToken().isEmpty()) {
            val result = authGrpcClient.issueTokens(
                appId = appId,
                userId = userId,
                deviceToken = deviceToken
            )

            result.fold(
                onSuccess = {
                    saveTokens(
                        access = it.accessToken,
                        refresh = it.refreshToken
                    )
                },
                onFailure = {
                    Sentry.captureException(it)
                    throw RuntimeException(it)
                }
            )
        } else {
            val result = authGrpcClient.refreshTokenPair(
                refreshToken = getRefreshToken(),
                userId = userId,
                deviceToken = deviceToken
            )

            result.fold(
                onSuccess = {
                    saveTokens(
                        access = it.accessToken,
                        refresh = it.refreshToken
                    )
                },
                onFailure = {
                    Sentry.captureException(it)
                    throw RuntimeException(it)
                }
            )
        }
    }

    override fun saveTokens(access: String, refresh: String) {
        prefs.edit(commit = true) {
            putString(PrefKeys.JWT_ACCESS_TOKEN, encryptBase64(access))
            putString(PrefKeys.JWT_REFRESH_TOKEN, encryptBase64(refresh))
        }
    }

    override fun clearTokens() {
        prefs.edit { clear() }
    }

    private fun encryptBase64(value: String): String {
        val encryptedBytes = keystore.encrypt(value.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    private fun decryptBase64(base64Value: String): String {
        return try {
            if (base64Value.isBlank()) {
                Sentry.captureMessage("decryptBase64: пустое значение")
                return ""
            }

            // Декодируем base64
            val encryptedBytes = try {
                Base64.decode(base64Value, Base64.NO_WRAP)
            } catch (e: IllegalArgumentException) {
                Sentry.captureException(Exception("decryptBase64: некорректная base64 строка", e))
                return ""
            }

            if (encryptedBytes.isEmpty()) {
                Sentry.captureMessage("decryptBase64: декодированные данные пустые")
                return ""
            }

            // Пытаемся расшифровать
            val decryptedBytes = try {
                keystore.decrypt(encryptedBytes)
            } catch (e: Exception) {
                Sentry.captureException(Exception("decryptBase64: ошибка при расшифровке", e))
                return ""
            }

            if (decryptedBytes.isEmpty()) {
                Sentry.captureMessage("decryptBase64: результат расшифровки пустой")
                return ""
            }

            // Преобразуем в строку
            val decryptedString = String(decryptedBytes, Charsets.UTF_8)

            if (decryptedString.isBlank()) {
                Sentry.captureMessage("decryptBase64: результат пустая строка")
            }

            decryptedString
        } catch (e: Exception) {
            Sentry.captureException(Exception("decryptBase64: критическая ошибка", e))
            ""
        }
    }
}
