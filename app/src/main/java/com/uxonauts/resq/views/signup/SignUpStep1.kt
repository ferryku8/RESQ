package com.uxonauts.resq.views.signup

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.ui.theme.ResqBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpStep1(controller: AuthController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Error states per field
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Tanggal lahir picker — batasi max date = 17 tahun lalu
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            controller.dateOfBirth = "$dayOfMonth/${month + 1}/$year"
            dobError = null
            // Cek usia setelah pilih tanggal
            val age = calculateAge(year, month, dayOfMonth)
            if (age < 17) {
                dobError = "Anda harus berusia minimal 17 tahun untuk mendaftar"
                controller.dateOfBirth = "" // Reset
            }
        },
        calendar.get(Calendar.YEAR) - 17, // Default ke 17 tahun lalu
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        // Batasi: tidak boleh pilih tanggal lebih muda dari 17 tahun
        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.YEAR, -17)
        datePicker.maxDate = maxDate.timeInMillis
    }

    // GPS
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isGettingLocation by remember { mutableStateOf(false) }

    fun processAddress(addresses: List<Address>?) {
        scope.launch(Dispatchers.Main) {
            if (!addresses.isNullOrEmpty()) {
                controller.address = addresses[0].getAddressLine(0)
                addressError = null
                Toast.makeText(context, "Lokasi terdeteksi!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Alamat tidak ditemukan.", Toast.LENGTH_SHORT).show()
            }
            isGettingLocation = false
        }
    }

    fun fetchCurrentLocation() {
        isGettingLocation = true
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        geocoder.getFromLocation(
                            location.latitude, location.longitude, 1
                        ) { addresses -> processAddress(addresses) }
                    } else {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(
                                    location.latitude, location.longitude, 1
                                )
                                processAddress(addresses)
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    isGettingLocation = false
                                    Toast.makeText(
                                        context, "Gagal memuat alamat", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                } else {
                    isGettingLocation = false
                    Toast.makeText(context, "Aktifkan GPS Anda", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                isGettingLocation = false
                Toast.makeText(context, "Gagal mengakses GPS", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            isGettingLocation = false
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) fetchCurrentLocation()
    }

    // Fungsi validasi semua field sebelum lanjut
    fun validateAndContinue() {
        firstNameError = null
        genderError = null
        dobError = null
        addressError = null
        phoneError = null
        emailError = null
        passwordError = null

        var hasError = false

        if (controller.firstName.isBlank()) {
            firstNameError = "Nama depan wajib diisi"
            hasError = true
        } else if (controller.firstName.length < 2) {
            firstNameError = "Nama depan minimal 2 karakter"
            hasError = true
        }

        if (controller.gender.isBlank()) {
            genderError = "Pilih jenis kelamin"
            hasError = true
        }

        if (controller.dateOfBirth.isBlank()) {
            dobError = "Tanggal lahir wajib dipilih"
            hasError = true
        } else {
            // Parse dan cek usia
            try {
                val parts = controller.dateOfBirth.split("/")
                val day = parts[0].toInt()
                val month = parts[1].toInt() - 1
                val year = parts[2].toInt()
                val age = calculateAge(year, month, day)
                if (age < 17) {
                    dobError = "Anda harus berusia minimal 17 tahun untuk mendaftar"
                    hasError = true
                }
            } catch (e: Exception) {
                dobError = "Format tanggal lahir tidak valid"
                hasError = true
            }
        }

        if (controller.address.isBlank()) {
            addressError = "Alamat wajib diisi"
            hasError = true
        }

        if (controller.phone.isBlank()) {
            phoneError = "Nomor telepon wajib diisi"
            hasError = true
        } else if (controller.phone.length < 10) {
            phoneError = "Nomor telepon minimal 10 digit"
            hasError = true
        } else if (!controller.phone.matches(Regex("^[0-9+\\-\\s]+$"))) {
            phoneError = "Nomor telepon hanya boleh berisi angka"
            hasError = true
        }

        if (controller.email.isBlank()) {
            emailError = "Email wajib diisi"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(controller.email).matches()) {
            emailError = "Format email tidak valid (contoh: nama@email.com)"
            hasError = true
        }

        if (controller.password.isBlank()) {
            passwordError = "Kata sandi wajib diisi"
            hasError = true
        } else if (controller.password.length < 6) {
            passwordError = "Kata sandi minimal 6 karakter"
            hasError = true
        } else if (!controller.password.matches(Regex(".*[A-Za-z].*")) ||
            !controller.password.matches(Regex(".*[0-9].*"))
        ) {
            passwordError = "Kata sandi harus mengandung huruf dan angka"
            hasError = true
        }

        if (!hasError) {
            controller.currentSignUpStep = 2
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                "Biodata Diri",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text("Nama Lengkap")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = controller.firstName,
                        onValueChange = {
                            controller.firstName = it
                            firstNameError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nama Depan *") },
                        isError = firstNameError != null,
                        singleLine = true
                    )
                    if (firstNameError != null) {
                        Text(
                            firstNameError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = controller.lastName,
                    onValueChange = { controller.lastName = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nama Belakang") },
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Jenis Kelamin *")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = controller.gender == "Laki-laki",
                    onClick = {
                        controller.gender = "Laki-laki"
                        genderError = null
                    }
                )
                Text("Laki-laki")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = controller.gender == "Perempuan",
                    onClick = {
                        controller.gender = "Perempuan"
                        genderError = null
                    }
                )
                Text("Perempuan")
            }
            if (genderError != null) {
                Text(
                    genderError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Tanggal Lahir
            OutlinedTextField(
                value = controller.dateOfBirth,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tanggal Lahir * (Min. 17 tahun)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Pilih Tanggal",
                            tint = ResqBlue
                        )
                    }
                },
                isError = dobError != null,
                supportingText = {
                    if (dobError != null) {
                        Text(dobError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Alamat
            OutlinedTextField(
                value = controller.address,
                onValueChange = {
                    controller.address = it
                    addressError = null
                },
                label = { Text("Alamat Lengkap *") },
                modifier = Modifier.fillMaxWidth(),
                isError = addressError != null,
                supportingText = {
                    if (addressError != null) {
                        Text(addressError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                trailingIcon = {
                    if (controller.address.isNotEmpty()) {
                        IconButton(onClick = { controller.address = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Bersihkan")
                        }
                    } else {
                        IconButton(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) fetchCurrentLocation()
                                else locationPermissionLauncher.launch(
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            },
                            enabled = !isGettingLocation
                        ) {
                            if (isGettingLocation)
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            else Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Deteksi Lokasi",
                                tint = ResqBlue
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Telepon
            OutlinedTextField(
                value = controller.phone,
                onValueChange = {
                    controller.phone = it
                    phoneError = null
                },
                label = { Text("Nomor Telepon *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError != null,
                supportingText = {
                    if (phoneError != null) {
                        Text(phoneError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Email
            OutlinedTextField(
                value = controller.email,
                onValueChange = {
                    controller.email = it
                    emailError = null
                },
                label = { Text("Alamat Email *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = {
                    if (emailError != null) {
                        Text(emailError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Password
            OutlinedTextField(
                value = controller.password,
                onValueChange = {
                    controller.password = it
                    passwordError = null
                },
                label = { Text("Kata Sandi *") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = passwordError != null,
                supportingText = {
                    if (passwordError != null) {
                        Text(passwordError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(
                            "Minimal 6 karakter, harus ada huruf dan angka",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                },
                singleLine = true
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

/**
 * Hitung usia dari tanggal lahir
 */
private fun calculateAge(year: Int, month: Int, day: Int): Int {
    val today = Calendar.getInstance()
    var age = today.get(Calendar.YEAR) - year

    // Kalau belum ulang tahun tahun ini, kurangi 1
    if (today.get(Calendar.MONTH) < month ||
        (today.get(Calendar.MONTH) == month && today.get(Calendar.DAY_OF_MONTH) < day)
    ) {
        age--
    }
    return age
}