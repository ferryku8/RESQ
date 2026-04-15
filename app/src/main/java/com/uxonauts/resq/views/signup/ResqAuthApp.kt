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

    val startDest = if (FirebaseAuth.getInstance().currentUser != null) {
        "home"
    } else {
        "onboarding"
    }

    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("login") { LoginScreen(navController, authController) }
        composable("signup") { SignUpHostScreen(navController, authController) }

        composable("pin_setup") { PinSetupScreen(navController, authController) }
        composable("biometric_setup") { BiometricSetupScreen(navController, authController) }

        composable("home") {
            HomeScreen(
                onSosClick = { navController.navigate("sos_flow") },
                onProfileClick = { navController.navigate("profile") },
                onLaporanClick = { navController.navigate("laporan_kategori") }
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

        composable("laporan_kategori") {
            com.uxonauts.resq.views.laporan.LaporanKategoriScreen(
                navController = navController,
                onKategoriSelected = { jenis, sub ->
                    val encJenis = java.net.URLEncoder.encode(jenis, "UTF-8")
                    val encSub = java.net.URLEncoder.encode(sub, "UTF-8")
                    navController.navigate("laporan_form/$encJenis/$encSub")
                }
            )
        }

        composable(
            "laporan_form/{jenis}/{sub}",
            arguments = listOf(
                androidx.navigation.navArgument("jenis") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("sub") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val jenis = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("jenis") ?: "", "UTF-8"
            )
            val sub = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("sub") ?: "", "UTF-8"
            )
            com.uxonauts.resq.views.laporan.LaporanFormScreen(
                navController = navController,
                jenisLaporan = jenis,
                subJenis = sub
            )
        }
    }
}