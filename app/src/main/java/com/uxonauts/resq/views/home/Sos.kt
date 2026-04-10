package com.uxonauts.resq.views.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
                    isFetchingLocation = true // Memulai proses ambil lokasi

                    // Simulasi pengambilan GPS / Lokasi selama 2 detik
                    // Di aplikasi asli, gunakan FusedLocationProviderClient di sini
                    coroutineScope.launch {
                        delay(2000)
                        currentLocation = "Jl. Sudirman No. 45, Binjai, Sumatera Utara"
                        isFetchingLocation = false
                        currentStep = SosStep.TRACKING_MAP

                        // Di sini Anda juga bisa mengirim data SOS ke Firestore
                        val sosData = hashMapOf(
                            "userId" to userId,
                            "category" to selectedCategory,
                            "location" to currentLocation,
                            "timestamp" to com.google.firebase.Timestamp.now(),
                            "status" to "active"
                        )
                        firestore.collection("sos_alerts").add(sosData)
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
fun SosCategoryScreen(
    onBackClick: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SOS", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFBFBFB)
                )
            )
        },
        containerColor = Color(0xFFFBFBFB)
    ) { paddingValues ->
        val categories = listOf(
            SosCategory("Kecelakaan", Icons.Default.CarCrash),
            SosCategory("Kebakaran", Icons.Default.LocalFireDepartment),
            SosCategory("Darurat Medis", Icons.Default.Add),
            SosCategory("Tindak Kriminal", Icons.Default.SportsMartialArts),
            SosCategory("Bencana Alam", Icons.Default.BrokenImage),
            SosCategory("Orang Hilang", Icons.Default.PersonSearch)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            items(categories) { category ->
                SosCategoryItem(category = category, onClick = { onCategoryClick(category.name) })
            }
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
    userProfile: UserProfile,
    onBackClick: () -> Unit
) {
    val currentTime = SimpleDateFormat("HH:mm 'WIB'", Locale("id", "ID")).format(Date())

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. Placeholder Latar Belakang Peta (Grid & Jalur)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8EAED))
        ) {
            val gridSpace = 120f
            for (i in 0..size.width.toInt() step gridSpace.toInt()) {
                drawLine(Color.White, Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height), strokeWidth = 8f)
            }
            for (i in 0..size.height.toInt() step gridSpace.toInt()) {
                drawLine(Color.White, Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()), strokeWidth = 8f)
            }

            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            val startPoint = Offset(size.width * 0.4f, size.height * 0.2f)
            val middlePoint = Offset(size.width * 0.4f, size.height * 0.4f)
            val endPoint = Offset(size.width * 0.7f, size.height * 0.6f)

            drawLine(Color(0xFF0084FF), startPoint, middlePoint, strokeWidth = 10f, pathEffect = pathEffect)
            drawLine(Color(0xFF0084FF), middlePoint, Offset(size.width * 0.7f, size.height * 0.4f), strokeWidth = 10f, pathEffect = pathEffect)
            drawLine(Color(0xFF0084FF), Offset(size.width * 0.7f, size.height * 0.4f), endPoint, strokeWidth = 10f, pathEffect = pathEffect)

            // Marker Tujuan (Lokasi Pengguna - Merah)
            drawCircle(Color(0xFFF44336).copy(alpha = 0.2f), radius = 120f, center = startPoint)
            drawCircle(Color(0xFFF44336), radius = 24f, center = startPoint)
            drawCircle(Color.White, radius = 8f, center = startPoint)

            // Marker Bantuan (Lokasi Polisi/Ambulan - Biru)
            drawCircle(Color(0xFF0084FF).copy(alpha = 0.2f), radius = 160f, center = endPoint)
            drawCircle(Color(0xFF0084FF), radius = 24f, center = endPoint)
            drawCircle(Color.White, radius = 8f, center = endPoint)
        }

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
                        Text("${userProfile.name}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
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
                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
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