package com.uxonauts.resq.views

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uxonauts.resq.controllers.AuthController

@Composable
fun SignUpStep2(controller: AuthController) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Verifikasi Identitas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Verifikasi KTP membantu kami memastikan setiap pengguna terdaftar secara valid.",
            textAlign = TextAlign.Center, color = TextGray, fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp)
                .border(2.dp, ResqLightBlue, RoundedCornerShape(12.dp))
                .clickable { /* TODO: Launch Camera/Gallery */ },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CreditCard, contentDescription = "KTP", modifier = Modifier.size(64.dp), tint = ResqBlue)
                Text("Ketuk untuk Mengunggah KTP", color = ResqBlue)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { controller.currentSignUpStep = 3 },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Lanjutkan", fontSize = 16.sp) }
        Spacer(modifier = Modifier.height(24.dp))
    }
}