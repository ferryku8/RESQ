package com.uxonauts.resq.views.signup

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.ui.theme.ResqBlue
import com.uxonauts.resq.views.ui.theme.TextGray
import java.util.concurrent.Executor

@Composable
fun BiometricSetupScreen(navController: NavController, controller: AuthController) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    // Cek apakah perangkat mendukung biometrik
    fun checkBiometricSupport(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> {
                // Jika di emulator belum setup fingerprint, ini akan return false
                Toast.makeText(context, "Perangkat/Emulator tidak mendukung biometrik atau belum disetup.", Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    fun authenticateBiometric() {
        if (activity == null || !checkBiometricSupport()) return

        val executor: Executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(context, "Sidik Jari Terverifikasi!", Toast.LENGTH_SHORT).show()
                    controller.isBiometricEnabled = true
                    controller.finalizeRegistration(navController)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Abaikan error jika user menekan tombol 'Cancel' / back
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(context, "Error: $errString", Toast.LENGTH_SHORT).show()
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verifikasi Sidik Jari")
            .setSubtitle("Sentuh sensor sidik jari")
            .setNegativeButtonText("Gunakan PIN saja")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = ResqBlue
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text("Aktifkan Sidik Jari?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ResqBlue)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Mempercepat akses tombol SOS dan login tanpa perlu mengetik PIN.",
            textAlign = TextAlign.Center,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Tombol Aktifkan
        Button(
            onClick = { authenticateBiometric() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Aktifkan Sekarang", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Lewati
        OutlinedButton(
            onClick = {
                controller.isBiometricEnabled = false
                controller.finalizeRegistration(navController)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Lewati (Gunakan PIN Saja)", fontSize = 16.sp, color = TextGray)
        }

        if (controller.isLoading) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = ResqBlue)
        }
    }
}