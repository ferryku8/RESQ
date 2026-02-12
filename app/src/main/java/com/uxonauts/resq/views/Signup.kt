package com.uxonauts.resq.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.ui.theme.ResqBlue
import com.uxonauts.resq.views.ui.theme.ResqLightBlue

@Composable
fun SignUpHostScreen(navController: NavController, controller: AuthController) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // App Bar & Back Button
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 36.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.clickable {
                if (controller.currentSignUpStep > 1) controller.currentSignUpStep-- else navController.popBackStack()
            })
            Spacer(modifier = Modifier.width(16.dp))
            Text("Daftar", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        // Custom Progress Bar
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 36.dp), verticalAlignment = Alignment.CenterVertically) {
            for (i in 1..4) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(if (i <= controller.currentSignUpStep) ResqBlue else ResqLightBlue), contentAlignment = Alignment.Center) {
                    Text("$i", color = Color.White, fontWeight = FontWeight.Bold)
                }
                if (i < 4) Box(modifier = Modifier.weight(1f).height(4.dp).background(if (i < controller.currentSignUpStep) ResqBlue else ResqLightBlue))
            }
        }

        // Konten Dinamis Step 1-4
        Box(modifier = Modifier.weight(1f).padding(horizontal = 42.dp)) {
            when (controller.currentSignUpStep) {
                1 -> SignUpStep1(controller)
                2 -> SignUpStep2(controller)
                3 -> SignUpStep3(controller)
                4 -> SignUpStep4(controller, navController)
            }
        }
    }
}