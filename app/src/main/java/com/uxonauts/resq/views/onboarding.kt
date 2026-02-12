package com.uxonauts.resq.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun OnboardingScreen(navController: NavController) {
    var currentPage by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(250.dp).clip(RoundedCornerShape(16.dp))
                .background(ResqLightBlue.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (currentPage == 1) Icons.Default.LocalHospital else if (currentPage == 2) Icons.Default.Security else Icons.Default.ContactEmergency,
                contentDescription = null, modifier = Modifier.size(100.dp), tint = ResqBlue
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = if (currentPage == 1) "SOS Darurat Cepat" else if (currentPage == 2) "Laporan & Info" else "Profil Medis",
            fontSize = 24.sp, fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Langkah ${currentPage} dari 3: Penjelasan fitur aplikasi RESQ.",
            textAlign = TextAlign.Center, color = TextGray
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = {
                if (currentPage < 3) currentPage++
                else navController.navigate("login") { popUpTo("onboarding") { inclusive = true } }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (currentPage == 3) "Mulai Sekarang" else "Lanjut", fontSize = 16.sp)
        }
    }
}