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

    var heightError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }
    var bloodTypeError by remember { mutableStateOf<String?>(null) }

    fun validateAndContinue() {
        heightError = null
        weightError = null
        bloodTypeError = null

        var hasError = false

        if (controller.height.isBlank()) {
            heightError = "Tinggi badan wajib diisi"
            hasError = true
        } else {
            val h = controller.height.toIntOrNull()
            if (h == null) {
                heightError = "Tinggi badan harus berupa angka"
                hasError = true
            } else if (h < 50 || h > 250) {
                heightError = "Tinggi badan tidak valid (50-250 cm)"
                hasError = true
            }
        }

        if (controller.weight.isBlank()) {
            weightError = "Berat badan wajib diisi"
            hasError = true
        } else {
            val w = controller.weight.toIntOrNull()
            if (w == null) {
                weightError = "Berat badan harus berupa angka"
                hasError = true
            } else if (w < 10 || w > 300) {
                weightError = "Berat badan tidak valid (10-300 kg)"
                hasError = true
            }
        }

        if (controller.bloodType.isBlank()) {
            bloodTypeError = "Golongan darah wajib dipilih"
            hasError = true
        }

        if (!hasError) {
            controller.currentSignUpStep = 4
        }
    }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = controller.height,
                        onValueChange = {
                            controller.height = it
                            heightError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tinggi Badan (cm) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = heightError != null,
                        singleLine = true
                    )
                    if (heightError != null) {
                        Text(
                            heightError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = controller.weight,
                        onValueChange = {
                            controller.weight = it
                            weightError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Berat Badan (kg) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = weightError != null,
                        singleLine = true
                    )
                    if (weightError != null) {
                        Text(
                            weightError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    isError = bloodTypeError != null
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
                                bloodTypeError = null
                                expanded = false
                            }
                        )
                    }
                }
            }
            if (bloodTypeError != null) {
                Text(
                    bloodTypeError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                onClick = { validateAndContinue() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Lanjutkan", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}