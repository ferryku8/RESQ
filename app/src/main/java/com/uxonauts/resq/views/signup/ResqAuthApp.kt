package com.uxonauts.resq.views.signup

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.home.SosSystemFlow
import com.uxonauts.resq.views.login.LoginScreen
import com.uxonauts.resq.views.login.OnboardingScreen


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
            com.uxonauts.resq.views.home.HomeScreen(
                onSosClick = {
                    // Navigasi ke rute SOS
                    navController.navigate("sos_flow")
                }
            )
        }

// Dan jangan lupa tambahkan rute SOS di NavHost
        composable("sos_flow") {
            val firebaseAuth = FirebaseAuth.getInstance()
            val currentUserId = firebaseAuth.currentUser?.uid ?: ""

            SosSystemFlow(
                onNavigateBack = { navController.popBackStack() },
                userId = currentUserId // Ini SANGAT PENTING agar Firestore tahu data siapa yang harus diambil
            )
        }
    }
}