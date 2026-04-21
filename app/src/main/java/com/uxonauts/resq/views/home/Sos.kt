package com.uxonauts.resq.views.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.FirebaseFirestore
import com.uxonauts.resq.utils.CategoryRoleMapper
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SosStep {
    CATEGORIES,
    TRACKING_MAP
}

data class UserProfile(
    val name: String,
    val bloodType: String,
    val allergies: String,
    val medicalHistory: String
)

data class SosCategory(val name: String, val icon: ImageVector)

data class RespondingPetugas(
    val uid: String, val name: String, val role: Int,
    val lat: Double, val lng: Double, val status: String
)

private fun notifyEmergencyContacts(
    firestore: FirebaseFirestore,
    senderUserId: String,
    senderName: String,
    category: String,
    address: String,
    latitude: Double,
    longitude: Double,
    alertId: String
) {
    firestore.collection("emergency_contacts")
        .whereEqualTo("userId", senderUserId)
        .get()
        .addOnSuccessListener { contactsSnap ->
            for (contactDoc in contactsSnap.documents) {
                val contactPhone = contactDoc.getString("noTelepon") ?: continue
                val contactName = contactDoc.getString("namaLengkap") ?: ""
                val hubungan = contactDoc.getString("hubungan") ?: ""

                val normalizedPhone = contactPhone.replace(" ", "")
                    .replace("-", "")
                    .trim()

                firestore.collection("users")
                    .whereEqualTo("noTelepon", normalizedPhone)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { userSnap ->
                        if (!userSnap.isEmpty) {
                            val targetDoc = userSnap.documents[0]
                            val targetUserId = targetDoc.id

                            if (targetUserId == senderUserId) return@addOnSuccessListener

                            val notifData = hashMapOf(
                                "alertId" to alertId,
                                "targetUserId" to targetUserId,
                                "senderUserId" to senderUserId,
                                "senderName" to senderName,
                                "senderPhone" to normalizedPhone,
                                "contactName" to contactName,
                                "hubungan" to hubungan,
                                "category" to category,
                                "address" to address,
                                "latitude" to latitude,
                                "longitude" to longitude,
                                "read" to false,
                                "timestamp" to com.google.firebase.Timestamp.now()
                            )
                            firestore.collection("emergency_notifications")
                                .add(notifData)
                        }
                    }
            }
        }
}

private fun authenticateWithBiometric(
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val activity = context as? FragmentActivity
    if (activity == null) {
        onError("Aktivitas tidak valid")
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
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Verifikasi gagal, coba lagi.")
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                            errorCode != BiometricPrompt.ERROR_CANCELED
                        ) {
                            onError("Error: $errString")
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
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> onError("Perangkat tidak memiliki sensor biometrik")
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> onError("Sensor biometrik tidak tersedia")
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> onError("Belum ada sidik jari terdaftar di perangkat")
        else -> onError("Biometrik tidak dapat digunakan")
    }
}

@Composable
fun SosSystemFlow(
    onNavigateBack: () -> Unit,
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    userId: String
) {
    val context = LocalContext.current
    var userLat by remember { mutableStateOf(0.0) }
    var userLng by remember { mutableStateOf(0.0) }
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var currentStep by remember { mutableStateOf(SosStep.CATEGORIES) }
    var selectedCategory by remember { mutableStateOf("") }
    var currentAlertId by remember { mutableStateOf("") }

    var isFetchingLocation by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("Memuat...") }
    var userPhone by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("-") }
    var allergies by remember { mutableStateOf("-") }
    var medicalHistory by remember { mutableStateOf("-") }

    var respondingList by remember { mutableStateOf<List<RespondingPetugas>>(emptyList()) }
    var alertStatus by remember { mutableStateOf("active") }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        userName = document.getString("namaLengkap")
                            ?: document.getString("fullName")
                                    ?: "Pengguna"
                        userPhone = document.getString("noTelepon") ?: ""
                    } else {
                        userName = "Pengguna"
                    }
                }
                .addOnFailureListener {
                    userName = "Pengguna"
                }

            firestore.collection("medical_info").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        bloodType = document.getString("golDarah")
                            ?: document.getString("bloodType") ?: "-"
                        allergies = document.getString("alergi")
                            ?: document.getString("allergies") ?: "-"
                        medicalHistory = document.getString("riwayatPenyakit")
                            ?: document.getString("medicalConditions") ?: "-"
                    }
                }
                .addOnFailureListener {
                    bloodType = "-"
                    allergies = "-"
                    medicalHistory = "-"
                }
        }
    }

    LaunchedEffect(currentAlertId) {
        if (currentAlertId.isNotEmpty()) {
            firestore.collection("sos_alerts").document(currentAlertId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                    alertStatus = snapshot.getString("status") ?: "active"

                    @Suppress("UNCHECKED_CAST")
                    val respMap = snapshot.get("respondingPetugas") as? Map<String, Map<String, Any>>
                    if (respMap != null) {
                        respondingList = respMap.map { (uid, data) ->
                            RespondingPetugas(
                                uid = uid,
                                name = (data["name"] as? String) ?: "Petugas",
                                role = ((data["role"] as? Long) ?: 2L).toInt(),
                                lat = (data["lat"] as? Double) ?: 0.0,
                                lng = (data["lng"] as? Double) ?: 0.0,
                                status = (data["status"] as? String) ?: "on_the_way"
                            )
                        }
                    }

                    if (alertStatus == "completed") {
                        Toast.makeText(context, "Laporan SOS telah diselesaikan", Toast.LENGTH_LONG).show()
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(2000)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                currentStep = SosStep.CATEGORIES
                                currentAlertId = ""
                                onNavigateBack()
                            }
                        }
                    }
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
        when (currentStep) {
            SosStep.CATEGORIES -> {
                SosCategoryScreen(
                    onBackClick = onNavigateBack,
                    onCategoryClick = { category ->
                        selectedCategory = category
                        authenticateWithBiometric(
                            context = context,
                            onSuccess = {
                                Toast.makeText(context, "Verifikasi Berhasil!", Toast.LENGTH_SHORT).show()
                                isFetchingLocation = true

                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                                if (!hasPerm) {
                                    isFetchingLocation = false
                                    Toast.makeText(context, "Izin lokasi diperlukan untuk SOS", Toast.LENGTH_LONG).show()
                                    return@authenticateWithBiometric
                                }

                                try {
                                    val cts = CancellationTokenSource()
                                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                                        .addOnSuccessListener { location ->
                                            if (location != null) {
                                                userLat = location.latitude
                                                userLng = location.longitude

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

                                                        val sosData = hashMapOf(
                                                            "userId" to userId,
                                                            "userName" to userName,
                                                            "userPhone" to userPhone,
                                                            "category" to selectedCategory,
                                                            "location" to currentLocation,
                                                            "address" to currentLocation,
                                                            "latitude" to userLat,
                                                            "longitude" to userLng,
                                                            "timestamp" to com.google.firebase.Timestamp.now(),
                                                            "status" to "active",
                                                            "targetRoles" to CategoryRoleMapper.getTargetRoles(selectedCategory),
                                                            "medicalInfo" to mapOf(
                                                                "bloodType" to bloodType,
                                                                "allergies" to allergies,
                                                                "medicalHistory" to medicalHistory
                                                            ),
                                                            "respondingPetugas" to emptyMap<String, Any>()
                                                        )

                                                        firestore.collection("sos_alerts").add(sosData)
                                                            .addOnSuccessListener { docRef ->
                                                                currentAlertId = docRef.id

                                                                notifyEmergencyContacts(
                                                                    firestore = firestore,
                                                                    senderUserId = userId,
                                                                    senderName = userName,
                                                                    category = selectedCategory,
                                                                    address = currentLocation,
                                                                    latitude = userLat,
                                                                    longitude = userLng,
                                                                    alertId = docRef.id
                                                                )

                                                                currentStep = SosStep.TRACKING_MAP
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Toast.makeText(context,
                                                                    "Gagal kirim SOS: ${e.message}",
                                                                    Toast.LENGTH_LONG).show()
                                                            }
                                                    }
                                                }
                                            } else {
                                                isFetchingLocation = false
                                                Toast.makeText(context,
                                                    "Lokasi tidak ditemukan. Aktifkan GPS.",
                                                    Toast.LENGTH_LONG).show()
                                            }
                                        }
                                        .addOnFailureListener {
                                            isFetchingLocation = false
                                            Toast.makeText(context,
                                                "Gagal mengambil lokasi: ${it.message}",
                                                Toast.LENGTH_LONG).show()
                                        }
                                } catch (e: SecurityException) {
                                    isFetchingLocation = false
                                }
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        )
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
                    respondingPetugas = respondingList,
                    alertStatus = alertStatus,
                    onBackClick = {
                        currentStep = SosStep.CATEGORIES
                        currentAlertId = ""
                    }
                )
            }
        }

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
                        Text("Mendeteksi Lokasi Darurat...",
                            fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Mencari titik GPS Anda saat ini untuk dikirim ke petugas.",
                            fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
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

@Composable
fun SosMapScreen(
    category: String,
    location: String,
    latitude: Double,
    longitude: Double,
    userProfile: UserProfile,
    respondingPetugas: List<RespondingPetugas> = emptyList(),
    alertStatus: String = "active",
    onBackClick: () -> Unit
) {
    val currentTime = SimpleDateFormat("HH:mm 'WIB'", Locale("id", "ID")).format(Date())
    val statusText = when (alertStatus) {
        "active" -> "Menunggu petugas..."
        "accepted" -> "${respondingPetugas.size} petugas merespons"
        "arrived" -> "Petugas telah tiba"
        "completed" -> "Selesai ditangani"
        else -> "Aktif"
    }

    var hasCenteredMap by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    controller.setCenter(GeoPoint(latitude, longitude))
                }
            },
            update = { mv ->
                mv.overlays.clear()

                // Marker user (merah)
                val userMarker = Marker(mv)
                userMarker.position = GeoPoint(latitude, longitude)
                userMarker.title = "Lokasi Anda"
                userMarker.icon = com.uxonauts.resq.utils.MapHelpers.createPinMarker(
                    mv.context, android.graphics.Color.parseColor("#F44336")
                )
                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mv.overlays.add(userMarker)

                // Marker SEMUA petugas — warna berdasarkan role
                respondingPetugas.forEach { p ->
                    if (p.lat != 0.0 && p.lng != 0.0) {
                        val color = when (p.role) {
                            2 -> "#0084FF"
                            3 -> "#4CAF50"
                            4 -> "#FF9800"
                            else -> "#9C27B0"
                        }
                        val marker = Marker(mv)
                        marker.position = GeoPoint(p.lat, p.lng)
                        marker.title = "${petugasRoleLabel(p.role)}: ${p.name}"
                        marker.icon = com.uxonauts.resq.utils.MapHelpers.createDotMarker(
                            mv.context, android.graphics.Color.parseColor(color)
                        )
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        mv.overlays.add(marker)
                    }
                }

                // Center map to show all markers
                if (!hasCenteredMap && respondingPetugas.any { it.lat != 0.0 }) {
                    val allLats = listOf(latitude) + respondingPetugas.map { it.lat }
                    val allLngs = listOf(longitude) + respondingPetugas.map { it.lng }
                    val cLat = allLats.average()
                    val cLng = allLngs.average()
                    mv.controller.setCenter(GeoPoint(cLat, cLng))
                    mv.controller.setZoom(14.0)
                    hasCenteredMap = true
                }
                mv.invalidate()
            }
        )

        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 40.dp, start = 16.dp).background(Color.White, CircleShape).size(48.dp)
        ) {
            Icon(Icons.Default.ArrowBack, "Kembali", tint = Color.Black)
        }

        // Status badge
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(statusText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Bottom card
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                // Daftar petugas yang merespons
                // Di dalam bottom card, ganti bagian daftar petugas:
                if (respondingPetugas.isEmpty()) {
                    Text("Menunggu petugas merespons...", fontSize = 14.sp, color = Color.Gray)
                } else {
                    Text("Petugas yang Merespons (${respondingPetugas.size})",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    respondingPetugas.forEach { p ->
                        val roleColor = when (p.role) {
                            2 -> Color(0xFF0084FF); 3 -> Color(0xFF4CAF50)
                            4 -> Color(0xFFFF9800); else -> Color.Gray
                        }
                        val pStatusLabel = when (p.status) {
                            "on_the_way" -> "Menuju lokasi"
                            "arrived" -> "Sudah tiba"
                            "completed" -> "Selesai"
                            else -> p.status
                        }

                        // Hitung jarak per petugas
                        val distText = if (p.lat != 0.0 && latitude != 0.0) {
                            val dist = haversineDistance(p.lat, p.lng, latitude, longitude)
                            if (dist < 1000) "${dist.toInt()} m"
                            else String.format("%.1f km", dist / 1000)
                        } else ""

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                .background(roleColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(roleColor))
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(petugasRoleLabel(p.role), fontSize = 12.sp,
                                        color = roleColor, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(6.dp))
                                    Text(p.name, fontSize = 12.sp)
                                }
                                Row {
                                    Text(pStatusLabel, fontSize = 11.sp, color = Color.Gray)
                                    if (distText.isNotEmpty() && p.status != "completed") {
                                        Text(" • $distText", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                            // Status badge
                            val badgeColor = when (p.status) {
                                "arrived" -> Color(0xFF2196F3)
                                "completed" -> Color(0xFF4CAF50)
                                else -> Color.Gray
                            }
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    when (p.status) {
                                        "on_the_way" -> "Menuju"
                                        "arrived" -> "Tiba"
                                        "completed" -> "Selesai"
                                        else -> p.status
                                    },
                                    fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("$category - $currentTime", fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(location, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 18.sp)
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(Modifier.height(12.dp))
                Text("Info Korban: ${userProfile.name}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                MedicalInfoRow("Golongan Darah", userProfile.bloodType)
                MedicalInfoRow("Alergi", userProfile.allergies)
                MedicalInfoRow("Riwayat Penyakit", userProfile.medicalHistory)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
private fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371000.0 // radius bumi dalam meter
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

@Composable
fun MedicalInfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, modifier = Modifier.width(140.dp),
            fontSize = 15.sp, color = Color.Gray)
        Text(text = ": ", fontSize = 15.sp, color = Color.Black)
        Text(text = value, fontSize = 15.sp, color = Color.Black,
            fontWeight = FontWeight.Medium)
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
        Icon(Icons.Default.LocalPolice, contentDescription = null,
            tint = Color.Gray, modifier = Modifier.size(36.dp))
    }
}

private fun petugasRoleLabel(role: Int): String = when (role) {
    2 -> "Polisi"; 3 -> "Medis"; 4 -> "Damkar"; else -> "Petugas"
}