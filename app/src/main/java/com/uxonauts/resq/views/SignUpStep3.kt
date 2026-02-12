package com.uxonauts.resq.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.ui.theme.ResqBlue
import com.uxonauts.resq.views.ui.theme.TextGray

@Composable
fun SignUpStep3(controller: AuthController) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Laporan Medis", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Info medis membantu tim penolong memberikan tindakan terbaik.", textAlign = TextAlign.Center, color = TextGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = controller.height, onValueChange = { controller.height = it }, modifier = Modifier.weight(1f), label = { Text("Tinggi Badan (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = controller.weight, onValueChange = { controller.weight = it }, modifier = Modifier.weight(1f), label = { Text("Berat Badan (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.bloodType, onValueChange = { controller.bloodType = it }, label = { Text("Golongan Darah") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.diseaseHistory, onValueChange = { controller.diseaseHistory = it }, label = { Text("Riwayat Penyakit") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.routineMeds, onValueChange = { controller.routineMeds = it }, label = { Text("Obat-obatan Rutin") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.allergies, onValueChange = { controller.allergies = it }, label = { Text("Alergi") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { controller.currentSignUpStep = 4 }, modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue), shape = RoundedCornerShape(8.dp)
            ) { Text("Lanjutkan", fontSize = 16.sp) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}