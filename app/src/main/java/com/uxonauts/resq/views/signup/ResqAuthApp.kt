package com.uxonauts.resq.views.signup

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.home.HomeScreen
import com.uxonauts.resq.views.home.SosSystemFlow
import com.uxonauts.resq.views.login.LoginScreen
import com.uxonauts.resq.views.login.OnboardingScreen
import com.uxonauts.resq.views.profile.ProfileScreen

@Composable
fun ResqAuthApp() {
    val navController = rememberNavController()
    val authController: AuthController = viewModel()

    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("login") { LoginScreen(navController, authController) }
        composable("signup") { SignUpHostScreen(navController, authController) }

        composable("pin_setup") { PinSetupScreen(navController, authController) }
        composable("biometric_setup") { BiometricSetupScreen(navController, authController) }

        composable("home") {
            HomeScreen(
                onSosClick = { navController.navigate("sos_flow") },
                onProfileClick = { navController.navigate("profile") }
            )
        }

        composable("profile") {
            ProfileScreen(
                navController = navController,
                onSosClick = { navController.navigate("sos_flow") }
            )
        }

        composable("sos_flow") {
            val firebaseAuth = FirebaseAuth.getInstance()
            val currentUserId = firebaseAuth.currentUser?.uid ?: ""

            SosSystemFlow(
                onNavigateBack = { navController.popBackStack() },
                userId = currentUserId
            )
        }
        composable("edit_profile") {
            com.uxonauts.resq.views.profile.EditProfileScreen(
                navController = navController
            )
        }
    }
}