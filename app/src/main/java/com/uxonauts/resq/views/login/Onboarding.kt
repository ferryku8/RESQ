package com.uxonauts.resq.views.login

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.uxonauts.resq.R
import com.uxonauts.resq.views.ui.theme.*

@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("resq_prefs", Context.MODE_PRIVATE) }
    val isFirstTime = remember { sharedPreferences.getBoolean("is_first_time", true) }

    LaunchedEffect(isFirstTime) {
        if (!isFirstTime) {
            navController.navigate("login") {
                popUpTo("onboarding") { inclusive = true }
            }
        }
    }

    if (isFirstTime) {
        var currentPage by remember { mutableStateOf(1) }

        // Menggunakan Box untuk memastikan seluruh area tertutup background #FBFBFB
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ResqBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. Gambar (Ditempatkan di tengah atas grup konten)
                Image(
                    painter = painterResource(
                        id = when (currentPage) {
                            1 -> R.drawable.onboardingsatu
                            2 -> R.drawable.onboardingdua
                            else -> R.drawable.onboardingtiga
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp), // Tinggi tetap agar gambar konsisten di tengah
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 2. Judul
                Text(
                    text = when (currentPage) {
                        1 -> "SOS Darurat Cepat"
                        2 -> "Laporan Keamanan"
                        else -> "Profil & Medis"
                    },
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1D1D1D),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Deskripsi
                Text(
                    text = when (currentPage) {
                        1 -> "Tekan tombol SOS saat darurat. Lokasi & info Anda langsung terkirim ke pihak berwenang dan kontak darurat terdekat."
                        2 -> "Buat laporan non-darurat seperti kehilangan atau penipuan dengan mudah dan pantau perkembangannya."
                        else -> "Lengkapi data medis penting Anda untuk memudahkan tim penolong memberikan tindakan yang tepat."
                    },
                    textAlign = TextAlign.Center,
                    color = TextGray,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 4. Tombol Lanjutkan (Berada di tengah bawah grup konten)
                Button(
                    onClick = {
                        if (currentPage < 3) {
                            currentPage++
                        } else {
                            sharedPreferences.edit().putBoolean("is_first_time", false).apply()
                            navController.navigate("login") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentPage == 3) "Mulai Sekarang" else "Lanjutkan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}