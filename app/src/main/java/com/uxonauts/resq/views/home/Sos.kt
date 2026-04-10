package com.uxonauts.resq.views.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Mengatur status/langkah dalam flow SOS
enum class SosStep {
    CATEGORIES,
    TRACKING_MAP
}

// Model data untuk simulasi profil pengguna
data class UserProfile(
    val name: String,
    val bloodType: String,
    val allergies: String,
    val medicalHistory: String
)

@Composable
fun SosSystemFlow(
    onNavigateBack: () -> Unit,
    // Gunakan FirebaseFirestore secara langsung atau melalui AuthController/ViewModel
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    userId: String // ID pengguna yang sedang login
) {
    val context = LocalContext.current
    var userLat by remember { mutableStateOf(0.0) }
    var userLng by remember { mutableStateOf(0.0) }
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var currentStep by remember { mutableStateOf(SosStep.CATEGORIES) }
    var selectedCategory by remember { mutableStateOf("") }
    var showFingerprintDialog by remember { mutableStateOf(false) }

    // State untuk proses loading lokasi
    var isFetchingLocation by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // State untuk data dari Firestore
    var userName by remember { mutableStateOf("Memuat...") }
    var bloodType by remember { mutableStateOf("-") }
    var allergies by remember { mutableStateOf("-") }
    var medicalHistory by remember { mutableStateOf("-") }

    // Efek untuk mengambil data dari Firestore saat komponen dimuat
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            // Ambil data User Profile
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        userName = document.getString("namaLengkap")
                            ?: document.getString("fullName")
                                    ?: "Nama Tidak Ditemukan"
                    } else {
                        userName = "Nama Tidak Ditemukan"
                    }
                }
                .addOnFailureListener {
                    userName = "Nama Tidak Ditemukan"
                }

            // Ambil data Medical Info
            firestore.collection("medical_info").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        bloodType = document.getString("golDarah")
                            ?: document.getString("bloodType")
                                    ?: "-"
                        allergies = document.getString("alergi")
                            ?: document.getString("allergies")
                                    ?: "-"
                        medicalHistory = document.getString("riwayatPenyakit")
                            ?: document.getString("medicalConditions")
                                    ?: "-"
                    }
                }
                .addOnFailureListener {
                    bloodType = "-"
                    allergies = "-"
                    medicalHistory = "-"
                }
        }
    }

    val currentUserData = UserProfile(
        name = userName,
        bloodType = bloodType,
        allergies = allergies,
        medicalHistory = medicalHistory
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Navigasi antar layar SOS
        when (currentStep) {
            SosStep.CATEGORIES -> {
                SosCategoryScreen(
                    onBackClick = onNavigateBack,
                    onCategoryClick = { category ->
                        selectedCategory = category
                        showFingerprintDialog = true
                    }
                )
            }
            SosStep.TRACKING_MAP -> {
                SosMapScreen(
                    category = selectedCategory,
                    location = currentLocation,
                    latitude = userLat,
                    longitude = userLng,
                    userProfile = currentUserData,
                    onBackClick = { currentStep = SosStep.CATEGORIES }
                )
            }
        }

        // Bottom Sheet untuk Sidik Jari
        if (showFingerprintDialog) {
            FingerprintBottomSheet(
                onDismiss = { showFingerprintDialog = false },
                onAuthenticated = {
                    showFingerprintDialog = false
                    isFetchingLocation = true

                    val hasPerm = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasPerm) {
                        isFetchingLocation = false
                        android.widget.Toast.makeText(context,
                            "Izin lokasi diperlukan untuk SOS",
                            android.widget.Toast.LENGTH_LONG).show()
                        return@FingerprintBottomSheet
                    }

                    try {
                        val cts = CancellationTokenSource()
                        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    userLat = location.latitude
                                    userLng = location.longitude

                                    // Reverse geocode jadi alamat
                                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        val addressText = try {
                                            val geocoder = Geocoder(context, Locale("id", "ID"))
                                            @Suppress("DEPRECATION")
                                            val list = geocoder.getFromLocation(userLat, userLng, 1)
                                            list?.firstOrNull()?.getAddressLine(0)
                                                ?: "Lat: $userLat, Lng: $userLng"
                                        } catch (e: Exception) {
                                            "Lat: $userLat, Lng: $userLng"
                                        }

                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            currentLocation = addressText
                                            isFetchingLocation = false
                                            currentStep = SosStep.TRACKING_MAP

                                            val sosData = hashMapOf(
                                                "userId" to userId,
                                                "category" to selectedCategory,
                                                "location" to currentLocation,
                                                "latitude" to userLat,
                                                "longitude" to userLng,
                                                "timestamp" to com.google.firebase.Timestamp.now(),
                                                "status" to "active"
                                            )
                                            firestore.collection("sos_alerts").add(sosData)
                                        }
                                    }
                                } else {
                                    isFetchingLocation = false
                                    android.widget.Toast.makeText(context,
                                        "Lokasi tidak ditemukan. Aktifkan GPS.",
                                        android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                            .addOnFailureListener {
                                isFetchingLocation = false
                                android.widget.Toast.makeText(context,
                                    "Gagal mengambil lokasi: ${it.message}",
                                    android.widget.Toast.LENGTH_LONG).show()
                            }
                    } catch (e: SecurityException) {
                        isFetchingLocation = false
                    }
                }
            )
        }

        // Overlay Loading Mengambil Lokasi
        if (isFetchingLocation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFFF44336))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Mendeteksi Lokasi Darurat...",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mencari titik GPS Anda saat ini untuk dikirim ke petugas.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FingerprintBottomSheet(
    onDismiss: () -> Unit,
    onAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var statusMessage by remember { mutableStateOf("Letakkan jari Anda pada sensor untuk memverifikasi laporan darurat ini.") }
    var isError by remember { mutableStateOf(false) }

    fun triggerBiometric() {
        if (activity == null) {
            Toast.makeText(context, "Aktivitas tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(context)
                val biometricPrompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult
                        ) {
                            super.onAuthenticationSucceeded(result)
                            Toast.makeText(
                                context,
                                "Verifikasi Berhasil!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onAuthenticated()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            statusMessage = "Verifikasi gagal, coba lagi."
                            isError = true
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                errorCode != BiometricPrompt.ERROR_CANCELED
                            ) {
                                statusMessage = "Error: $errString"
                                isError = true
                                Toast.makeText(
                                    context,
                                    "Error: $errString",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Verifikasi SOS Darurat")
                    .setSubtitle("Sentuh sensor sidik jari untuk mengirim laporan")
                    .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                statusMessage = "Perangkat tidak memiliki sensor biometrik"
                isError = true
                Toast.makeText(context, statusMessage, Toast.LENGTH_LONG).show()
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                statusMessage = "Sensor biometrik tidak tersedia"
                isError = true
                Toast.makeText(context, statusMessage, Toast.LENGTH_LONG).show()
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                statusMessage = "Belum ada sidik jari terdaftar di perangkat"
                isError = true
                Toast.makeText(
                    context,
                    "Silakan daftarkan sidik jari di Settings perangkat",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                statusMessage = "Biometrik tidak dapat digunakan"
                isError = true
                Toast.makeText(context, statusMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Trigger biometric prompt otomatis saat sheet muncul
    LaunchedEffect(Unit) {
        triggerBiometric()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 56.dp, top = 8.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Verifikasi Sidik Jari",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage,
                fontSize = 14.sp,
                color = if (isError) Color.Red else Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Tombol untuk retry manual kalau gagal
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(if (isError) Color.Red else Color(0xFF0084FF))
                    .clickable { triggerBiometric() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Pemindai Sidik Jari",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Ketuk untuk coba lagi",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SosCategoryItem(category: SosCategory, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFEE3131))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = Color.White,
                modifier = Modifier.size(72.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = category.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FingerprintBottomSheet(
    onDismiss: () -> Unit,
    onAuthenticated: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 56.dp, top = 8.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Scan Sidik Jari Anda",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Letakkan jari Anda pada sensor untuk memverifikasi laporan darurat ini.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Tombol Simulasi Fingerprint
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0084FF))
                    .clickable { onAuthenticated() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Pemindai Sidik Jari",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SosMapScreen(
    category: String,
    location: String,
    latitude: Double,
    longitude: Double,
    userProfile: UserProfile,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val currentTime = SimpleDateFormat("HH:mm 'WIB'", Locale("id", "ID")).format(Date())

    Box(modifier = Modifier.fillMaxSize()) {

        // Peta OpenStreetMap
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                Configuration.getInstance().load(
                    ctx, PreferenceManager.getDefaultSharedPreferences(ctx)
                )
                Configuration.getInstance().userAgentValue = ctx.packageName

                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(17.0)
                    val point = GeoPoint(latitude, longitude)
                    controller.setCenter(point)

                    val marker = Marker(this)
                    marker.position = point
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Lokasi Darurat Anda"
                    overlays.add(marker)
                }
            }
        )

        // Tombol Kembali
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp)
                .background(Color.White, CircleShape)
                .size(48.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.Black)
        }

        // Label Lokasi Melayang
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Lokasi Anda Ditemukan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        // 2. Kartu Informasi di Bagian Bawah
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header Profil Petugas (Ini adalah Petugas, bukan Pengguna)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ImagePlaceholder()
                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(userProfile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalPolice, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Petugas Medis/Polisi - 5 min", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }

                    Icon(Icons.Default.Call, contentDescription = "Telepon", tint = Color(0xFF0084FF), modifier = Modifier.size(28.dp).clickable {  })
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.ChatBubble, contentDescription = "Chat", tint = Color(0xFF0084FF), modifier = Modifier.size(28.dp).clickable {  })
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detail Kejadian & Lokasi Pengguna
                Text("$category - $currentTime", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(location, fontSize = 15.sp, color = Color.DarkGray, lineHeight = 20.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Info Medis Pengguna
                Text("Info Darurat Korban: ${userProfile.name}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                MedicalInfoRow("Golongan Darah", userProfile.bloodType)
                MedicalInfoRow("Alergi", userProfile.allergies)
                MedicalInfoRow("Riwayat Penyakit", userProfile.medicalHistory)

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun MedicalInfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, modifier = Modifier.width(140.dp), fontSize = 15.sp, color = Color.Gray)
        Text(text = ": ", fontSize = 15.sp, color = Color.Black)
        Text(text = value, fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ImagePlaceholder() {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.LocalPolice, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
    }
}

data class SosCategory(val name: String, val icon: ImageVector)