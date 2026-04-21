package com.uxonauts.resq.views.login

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.uxonauts.resq.R
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.services.EmergencyListenerService
import com.uxonauts.resq.views.ui.theme.ResqBlue

@Composable
fun LoginScreen(navController: NavController, controller: AuthController) {
    val context = LocalContext.current

    // Local validation errors
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun validateAndLogin() {
        // Reset errors
        emailError = null
        passwordError = null
        controller.errorMessage = null

        var hasError = false

        // Validasi email
        if (controller.loginEmail.isBlank()) {
            emailError = "Email tidak boleh kosong"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(controller.loginEmail).matches()) {
            emailError = "Format email tidak valid"
            hasError = true
        }

        // Validasi password
        if (controller.loginPassword.isBlank()) {
            passwordError = "Kata sandi tidak boleh kosong"
            hasError = true
        } else if (controller.loginPassword.length < 6) {
            passwordError = "Kata sandi minimal 6 karakter"
            hasError = true
        }

        if (hasError) return

        controller.doLogin(navController) {
            val intent = Intent(context, EmergencyListenerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text("Masuk", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // Email field
        OutlinedTextField(
            value = controller.loginEmail,
            onValueChange = {
                controller.loginEmail = it
                emailError = null  // Clear error saat user mengetik
                controller.errorMessage = null
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = emailError != null,
            supportingText = {
                if (emailError != null) {
                    Text(emailError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password field
        OutlinedTextField(
            value = controller.loginPassword,
            onValueChange = {
                controller.loginPassword = it
                passwordError = null
                controller.errorMessage = null
            },
            label = { Text("Kata Sandi") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = passwordError != null,
            supportingText = {
                if (passwordError != null) {
                    Text(passwordError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true
        )

        // Firebase error (email/password salah, akun tidak ditemukan, dll)
        if (controller.errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = formatFirebaseError(controller.errorMessage!!),
                    color = Color(0xFFD32F2F),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { validateAndLogin() },
            enabled = !controller.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (controller.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Masuk", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("signup") }) {
            Text("Belum punya akun? Daftar", color = ResqBlue)
        }
    }
}

/**
 * Terjemahkan error Firebase ke bahasa Indonesia yang user-friendly
 */
private fun formatFirebaseError(error: String): String {
    return when {
        error.contains("no user record", ignoreCase = true) ||
                error.contains("USER_NOT_FOUND", ignoreCase = true) ->
            "Akun dengan email ini tidak ditemukan. Silakan periksa kembali atau daftar akun baru."

        error.contains("password is invalid", ignoreCase = true) ||
                error.contains("INVALID_PASSWORD", ignoreCase = true) ->
            "Kata sandi salah. Silakan coba lagi."

        error.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                error.contains("invalid credential", ignoreCase = true) ->
            "Email atau kata sandi salah. Silakan periksa kembali."

        error.contains("too many", ignoreCase = true) ||
                error.contains("TOO_MANY_ATTEMPTS", ignoreCase = true) ->
            "Terlalu banyak percobaan login gagal. Silakan coba lagi dalam beberapa menit."

        error.contains("network", ignoreCase = true) ->
            "Tidak ada koneksi internet. Periksa jaringan Anda dan coba lagi."

        error.contains("disabled", ignoreCase = true) ->
            "Akun ini telah dinonaktifkan. Hubungi administrator."

        error.contains("email", ignoreCase = true) && error.contains("badly", ignoreCase = true) ->
            "Format email tidak valid."

        else -> "Login gagal. Silakan periksa email dan kata sandi Anda."
    }
}