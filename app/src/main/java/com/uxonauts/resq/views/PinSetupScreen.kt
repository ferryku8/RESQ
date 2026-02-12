package com.uxonauts.resq.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.ui.theme.ResqBlue
import com.uxonauts.resq.views.ui.theme.ResqLightBlue
import com.uxonauts.resq.views.ui.theme.TextGray

@Composable
fun PinSetupScreen(navController: NavController, controller: AuthController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Buat PIN Keamanan", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ResqBlue)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Masukkan 6 digit PIN untuk keamanan tambahan.",
            textAlign = TextAlign.Center,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Input PIN (Invisible TextField di atas kotak visual)
        BasicTextField(
            value = controller.pinCode,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    controller.updatePin(it)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            decorationBox = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(6) { index ->
                        val char = if (index < controller.pinCode.length) controller.pinCode[index] else ""
                        val isFocused = index == controller.pinCode.length

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.8f)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = if (isFocused) ResqBlue else ResqLightBlue,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (char.toString().isNotEmpty()) "●" else "", // Sembunyikan angka dengan dot
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ResqBlue
                            )
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { navController.navigate("biometric_setup") },
            enabled = controller.pinCode.length == 6,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Lanjut", fontSize = 16.sp)
        }
    }
}