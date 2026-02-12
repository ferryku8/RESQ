package com.uxonauts.resq.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.ui.theme.ResqBlue


@Composable
fun SignUpStep4(controller: AuthController, navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Kontak Darurat", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Nama Lengkap")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = controller.ecFirstName, onValueChange = { controller.ecFirstName = it }, modifier = Modifier.weight(1f), placeholder = { Text("Nama Depan") })
                OutlinedTextField(value = controller.ecLastName, onValueChange = { controller.ecLastName = it }, modifier = Modifier.weight(1f), placeholder = { Text("Nama Belakang") })
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.ecRelation, onValueChange = { controller.ecRelation = it }, label = { Text("Hubungan") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.ecPhone, onValueChange = { controller.ecPhone = it }, label = { Text("Nomor Telepon") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

            Spacer(modifier = Modifier.height(16.dp))
            Text("+ Tambahkan Nomor Darurat", color = ResqBlue, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth().clickable { }, textAlign = TextAlign.End)

            Spacer(modifier = Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = controller.termsAccepted, onCheckedChange = { controller.termsAccepted = it })
                Text("Saya menyetujui ", fontSize = 12.sp)
                Text("Syarat dan Ketentuan", fontSize = 12.sp, color = ResqBlue)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { controller.submitRegistration(navController) },
                enabled = controller.termsAccepted, modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue), shape = RoundedCornerShape(8.dp)
            ) { Text("Mulai", fontSize = 16.sp) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}