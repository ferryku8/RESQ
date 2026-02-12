package com.uxonauts.resq.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.color_theme

@Composable
fun LoginScreen(navController: NavController, controller: AuthController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.MedicalServices, contentDescription = "Logo", modifier = Modifier.size(80.dp), tint = ResqBlue)
        Text("RESQ", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = ResqBlue)

        Spacer(modifier = Modifier.height(48.dp))
        Text("Masuk", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = controller.loginEmail, onValueChange = { controller.loginEmail = it },
            placeholder = { Text("Email") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = controller.loginPassword, onValueChange = { controller.loginPassword = it },
            placeholder = { Text("Kata Sandi") }, visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Lupa kata sandi?", color = ResqBlue, modifier = Modifier.align(Alignment.End).clickable { })
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { controller.doLogin(navController) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Masuk", fontSize = 16.sp) }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { navController.navigate("signup") },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Daftar", fontSize = 16.sp, color = ResqBlue) }
    }
}