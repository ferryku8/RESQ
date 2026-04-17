package com.uxonauts.resq.views.signup

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.home.ArtikelDetailScreen
import com.uxonauts.resq.views.home.ArtikelListScreen
import com.uxonauts.resq.views.home.HomeScreen
import com.uxonauts.resq.views.home.NotifikasiScreen
import com.uxonauts.resq.views.home.ProgressLaporanScreen
import com.uxonauts.resq.views.home.RiwayatScreen
import com.uxonauts.resq.views.home.SosSystemFlow
import com.uxonauts.resq.views.laporan.LaporanFormScreen
import com.uxonauts.resq.views.laporan.LaporanKategoriScreen
import com.uxonauts.resq.views.login.LoginScreen
import com.uxonauts.resq.views.login.OnboardingScreen
import com.uxonauts.resq.views.profile.EditProfileScreen
import com.uxonauts.resq.views.profile.ProfileScreen
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun ResqAuthApp() {
    val navController = rememberNavController()
    val authController: AuthController = viewModel()

    val startDest = if (FirebaseAuth.getInstance().currentUser != null) {
        "home"
    } else {
        "onboarding"
    }

    NavHost(navController = navController, startDestination = startDest) {
        composable("onboarding") {
            OnboardingScreen(navController)
        }

        composable("login") {
            LoginScreen(navController, authController)
        }

        composable("signup") {
            SignUpHostScreen(navController, authController)
        }

        composable("pin_setup") {
            PinSetupScreen(navController, authController)
        }

        composable("biometric_setup") {
            BiometricSetupScreen(navController, authController)
        }

        composable("home") {
            HomeScreen(
                onSosClick = { navController.navigate("sos_flow") },
                onProfileClick = { navController.navigate("profile") },
                onLaporanClick = { navController.navigate("laporan_kategori") },
                onLaporanCategoryClick = { category ->
                    val enc = URLEncoder.encode(category, "UTF-8")
                    navController.navigate("laporan_kategori?preselect=$enc")
                },
                onArtikelListClick = { navController.navigate("artikel_list") },
                onArtikelDetailClick = { id ->
                    navController.navigate("artikel_detail/$id")
                },
                onRiwayatClick = { navController.navigate("riwayat") },
                onNotifikasiClick = { navController.navigate("notifikasi") }
            )
        }

        composable("profile") {
            ProfileScreen(
                navController = navController,
                onSosClick = { navController.navigate("sos_flow") }
            )
        }

        composable("sos_flow") {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            SosSystemFlow(
                onNavigateBack = { navController.popBackStack() },
                userId = currentUserId
            )
        }

        composable("edit_profile") {
            EditProfileScreen(navController = navController)
        }

        composable("laporan_kategori") {
            LaporanKategoriScreen(
                navController = navController,
                onKategoriSelected = { jenis, sub ->
                    val encJenis = URLEncoder.encode(jenis, "UTF-8")
                    val encSub = URLEncoder.encode(sub, "UTF-8")
                    navController.navigate("laporan_form/$encJenis/$encSub")
                }
            )
        }

        composable(
            route = "laporan_kategori?preselect={preselect}",
            arguments = listOf(
                navArgument("preselect") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val preselect = URLDecoder.decode(
                backStackEntry.arguments?.getString("preselect") ?: "", "UTF-8"
            )
            LaporanKategoriScreen(
                navController = navController,
                preselectedCategory = preselect,
                onKategoriSelected = { jenis, sub ->
                    val encJenis = URLEncoder.encode(jenis, "UTF-8")
                    val encSub = URLEncoder.encode(sub, "UTF-8")
                    navController.navigate("laporan_form/$encJenis/$encSub")
                }
            )
        }

        composable(
            route = "laporan_form/{jenis}/{sub}",
            arguments = listOf(
                navArgument("jenis") { type = NavType.StringType },
                navArgument("sub") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val jenis = URLDecoder.decode(
                backStackEntry.arguments?.getString("jenis") ?: "", "UTF-8"
            )
            val sub = URLDecoder.decode(
                backStackEntry.arguments?.getString("sub") ?: "", "UTF-8"
            )
            LaporanFormScreen(
                navController = navController,
                jenisLaporan = jenis,
                subJenis = sub
            )
        }

        composable("artikel_list") {
            ArtikelListScreen(navController)
        }

        composable(
            route = "artikel_detail/{articleId}",
            arguments = listOf(
                navArgument("articleId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
            ArtikelDetailScreen(navController, articleId)
        }

        composable("riwayat") {
            RiwayatScreen(navController)
        }

        composable("notifikasi") {
            NotifikasiScreen(navController)
        }

        composable(
            route = "progress_laporan/{reportId}",
            arguments = listOf(
                navArgument("reportId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            ProgressLaporanScreen(navController, reportId)
        }
    }
}