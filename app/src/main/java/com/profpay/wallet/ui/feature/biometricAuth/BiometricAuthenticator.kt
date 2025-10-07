package com.profpay.wallet.ui.feature.biometricAuth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.R
import com.profpay.wallet.bridge.view_model.pin_lock.PinLockViewModel
import com.profpay.wallet.ui.shared.sharedPref

@Composable
fun FaceIDAuthentication(
    toNavigate: () -> Unit,
    viewModel: PinLockViewModel = hiltViewModel(),
) {
    if (sharedPref().getBoolean("useBiomAuth", true)) {
        val context = LocalContext.current
        val fragmentActivity = context as? FragmentActivity ?: return
        var authResult by remember { mutableStateOf("Tap Authenticate to start Face ID") }
        var isAuthCancelled by remember { mutableStateOf(false) }

        val biometricManager = BiometricManager.from(context)
        val executor = ContextCompat.getMainExecutor(context)

        val isBiometricSupported =
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

        if (isBiometricSupported) {
            val promptInfo =
                BiometricPrompt.PromptInfo
                    .Builder()
                    .setTitle("Biometric Authentication")
                    .setSubtitle("Authenticate using your face")
                    .setNegativeButtonText("Cancel")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build()

            val biometricPrompt =
                BiometricPrompt(
                    fragmentActivity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            viewModel.unlockSession()
                            authResult = "Authentication succeeded!"
                            toNavigate()
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence,
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            authResult = "Authentication error: $errString"
                            if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                isAuthCancelled = true
                            }
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            authResult = "Authentication failed. Try again."
                        }
                    },
                )

            if (!isAuthCancelled && authResult != "Authentication succeeded!") {
                biometricPrompt.authenticate(promptInfo)
            }

            IconButton(
                modifier = Modifier.fillMaxSize(0.7f),
                onClick = { biometricPrompt.authenticate(promptInfo) },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_face_scan),
                    contentDescription = "Face ID",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
