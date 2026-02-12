package com.uxonauts.resq.views

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uxonauts.resq.controllers.AuthController

@Composable
fun ResqAuthApp() {
    val navController = rememberNavController()
    val authController: AuthController = viewModel()

    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("login") { LoginScreen(navController, authController) }
        composable("signup") { SignUpHostScreen(navController, authController) }

        // Rute Baru:
        composable("pin_setup") { PinSetupScreen(navController, authController) }
        composable("biometric_setup") { BiometricSetupScreen(navController, authController) }

        // Rute Home (Placeholder sementara biar gak crash kalau di-navigate)
        composable("home") {
            // Nanti diganti dengan HomeScreen beneran
            androidx.compose.material3.Text("Selamat Datang di Home Screen!")
        }
    }
}