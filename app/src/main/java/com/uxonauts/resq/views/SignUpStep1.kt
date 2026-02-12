package com.uxonauts.resq.views

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.util.Locale

@Composable
fun SignUpStep1(controller: AuthController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Client untuk akses lokasi (GPS)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isGettingLocation by remember { mutableStateOf(false) }

    // Fungsi Helper untuk memproses hasil Geocoder
    fun processAddress(addresses: List<Address>?) {
        scope.launch(Dispatchers.Main) {
            if (!addresses.isNullOrEmpty()) {
                val addressLine = addresses[0].getAddressLine(0)
                controller.address = addressLine
                Toast.makeText(context, "Alamat ditemukan!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Alamat tidak ditemukan di koordinat ini.", Toast.LENGTH_SHORT).show()
            }
            isGettingLocation = false
        }
    }

    // Fungsi untuk mendapatkan alamat dari koordinat GPS (Reverse Geocoding)
    // Mendukung versi Android lama dan baru (Tiramisu API 33+)
    fun fetchCurrentLocation() {
        isGettingLocation = true
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Toast.makeText(context, "Mendeteksi lokasi...", Toast.LENGTH_SHORT).show()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Versi Android Baru (Asynchronous)
                        val geocoder = Geocoder(context, Locale.getDefault())
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                            processAddress(addresses)
                        }
                    } else {
                        // Versi Android Lama (Synchronous - butuh background thread)
                        scope.launch(Dispatchers.IO) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                processAddress(addresses)
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    isGettingLocation = false
                                    Toast.makeText(context, "Gagal memuat alamat: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                } else {
                    isGettingLocation = false
                    Toast.makeText(context, "Lokasi tidak terdeteksi. Coba nyalakan GPS.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                isGettingLocation = false
                Toast.makeText(context, "Gagal mengakses GPS: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            isGettingLocation = false
            Toast.makeText(context, "Izin lokasi diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher untuk meminta izin lokasi
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchCurrentLocation()
        } else {
            Toast.makeText(context, "Izin lokasi ditolak. Harap isi manual.", Toast.LENGTH_LONG).show()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Biodata Diri", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Nama Lengkap")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = controller.firstName,
                    onValueChange = { controller.firstName = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nama Depan *") }
                )
                OutlinedTextField(
                    value = controller.lastName,
                    onValueChange = { controller.lastName = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nama Belakang") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Jenis Kelamin *")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = controller.gender == "Laki-laki", onClick = { controller.gender = "Laki-laki" })
                Text("Laki-laki")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = controller.gender == "Perempuan", onClick = { controller.gender = "Perempuan" })
                Text("Perempuan")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Kolom Alamat dengan Tombol Deteksi Lokasi
            OutlinedTextField(
                value = controller.address,
                onValueChange = { controller.address = it },
                label = { Text("Alamat Lengkap *") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (controller.address.isNotEmpty()) {
                        // Tampilkan tombol silang jika ada teks untuk memudahkan hapus
                        IconButton(onClick = { controller.address = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus")
                        }
                    } else {
                        // Tampilkan tombol GPS jika kosong
                        IconButton(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    fetchCurrentLocation()
                                } else {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            },
                            enabled = !isGettingLocation
                        ) {
                            if (isGettingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.LocationOn, contentDescription = "Ambil Lokasi", tint = ResqBlue)
                            }
                        }
                    }
                }
            )
            if (controller.address.isEmpty()) {
                Text(
                    text = "Klik ikon lokasi untuk mendeteksi alamat otomatis",
                    fontSize = 10.sp,
                    color = ResqBlue,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = controller.phone, onValueChange = { controller.phone = it }, label = { Text("Nomor Telepon *") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = controller.email, onValueChange = { controller.email = it }, label = { Text("Alamat Email *") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = controller.password, onValueChange = { controller.password = it }, label = { Text("Kata Sandi (Min 6 Karakter) *") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { controller.currentSignUpStep = 2 },
                enabled = controller.isStep1Valid(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue), shape = RoundedCornerShape(8.dp)
            ) { Text("Lanjutkan", fontSize = 16.sp) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}