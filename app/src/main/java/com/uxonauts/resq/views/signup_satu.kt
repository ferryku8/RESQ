package com.uxonauts.resq.views

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uxonauts.resq.controllers.AuthController


@Composable
fun SignUpStep1(controller: AuthController) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Biodata Diri", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Nama Lengkap")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = controller.firstName, onValueChange = { controller.firstName = it }, modifier = Modifier.weight(1f), placeholder = { Text("Nama Depan") })
                OutlinedTextField(value = controller.lastName, onValueChange = { controller.lastName = it }, modifier = Modifier.weight(1f), placeholder = { Text("Nama Belakang") })
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Jenis Kelamin")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = controller.gender == "Laki-laki", onClick = { controller.gender = "Laki-laki" })
                Text("Laki-laki")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = controller.gender == "Perempuan", onClick = { controller.gender = "Perempuan" })
                Text("Perempuan")
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = controller.address, onValueChange = { controller.address = it }, label = { Text("Alamat") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = controller.phone, onValueChange = { controller.phone = it }, label = { Text("Nomor Telepon") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = controller.email, onValueChange = { controller.email = it }, label = { Text("Alamat Email") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = controller.password, onValueChange = { controller.password = it }, label = { Text("Kata Sandi") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { controller.currentSignUpStep = 2 }, modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue), shape = RoundedCornerShape(8.dp)
            ) { Text("Lanjutkan", fontSize = 16.sp) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}