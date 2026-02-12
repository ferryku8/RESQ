package com.uxonauts.resq.views

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.resq.controller.AuthController

@Composable
fun ResqAuthApp() {
    val navController = rememberNavController()
    // Inisialisasi Controller (ViewModel) yang akan dipakai bersama
    val authController: AuthController = viewModel()

    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") {
            OnboardingScreen(navController)
        }
        composable("login") {
            LoginScreen(navController, authController)
        }
        composable("signup") {
            SignUpHostScreen(navController, authController)
        }
    }
}