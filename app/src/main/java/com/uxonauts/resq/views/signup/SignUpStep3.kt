package com.uxonauts.resq.views.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.ui.theme.*

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpStep3(controller: AuthController) {
    var expanded by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                "Laporan Medis",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Berikan informasi medis dasar. Tanda (*) wajib diisi.",
                textAlign = TextAlign.Center,
                color = TextGray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = controller.height,
                    onValueChange = { controller.height = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Tinggi Badan (cm) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = controller.weight,
                    onValueChange = { controller.weight = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Berat Badan (kg) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown Golongan Darah (Wajib)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = controller.bloodType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Golongan Darah *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    controller.bloodTypeOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                controller.bloodType = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kolom Opsional
            OutlinedTextField(
                value = controller.diseaseHistory,
                onValueChange = { controller.diseaseHistory = it },
                label = { Text("Riwayat Penyakit (Opsional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = controller.routineMeds,
                onValueChange = { controller.routineMeds = it },
                label = { Text("Obat-obatan Rutin (Opsional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = controller.allergies,
                onValueChange = { controller.allergies = it },
                label = { Text("Alergi (Opsional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { controller.currentSignUpStep = 4 },
                // Tombol aktif jika Tinggi, Berat, dan Gol Darah sudah terisi
                enabled = controller.isStep3Valid(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Lanjutkan", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}