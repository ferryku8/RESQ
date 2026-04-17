package com.uxonauts.resq.views.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

private val USER_STAGES = listOf("Diterima", "Diverifikasi", "Ditindaklanjuti", "Selesai")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressLaporanScreen(navController: NavController, reportId: String) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy - HH.mm 'WIB'", Locale("id", "ID")) }
    val timelineFormat = remember { SimpleDateFormat("[dd/MM/yyyy HH:mm]", Locale("id", "ID")) }

    var loading by remember { mutableStateOf(true) }
    var subJenis by remember { mutableStateOf("") }
    var judul by remember { mutableStateOf("") }
    var kronologi by remember { mutableStateOf("") }
    var lokasi by remember { mutableStateOf("") }
    var tanggalKejadian by remember { mutableStateOf("") }
    var waktuKejadian by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Menunggu") }
    var photos by remember { mutableStateOf<List<String>>(emptyList()) }
    var details by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var acceptedByName by remember { mutableStateOf("") }
    var acceptedByKontak by remember { mutableStateOf("") }
    var progressNotes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var tanggalLapor by remember { mutableStateOf<Timestamp?>(null) }

    LaunchedEffect(reportId) {
        firestore.collection("reports").document(reportId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    subJenis = snapshot.getString("subJenis") ?: "-"
                    judul = snapshot.getString("judul") ?: "-"
                    kronologi = snapshot.getString("kronologi") ?: "-"
                    lokasi = snapshot.getString("lokasi") ?: "-"
                    tanggalKejadian = snapshot.getString("tanggalKejadian") ?: "-"
                    waktuKejadian = snapshot.getString("waktuKejadian") ?: "-"
                    status = snapshot.getString("status") ?: "Menunggu"
                    tanggalLapor = snapshot.getTimestamp("tanggalLapor")
                    @Suppress("UNCHECKED_CAST")
                    photos = (snapshot.get("photos") as? List<String>) ?: emptyList()
                    @Suppress("UNCHECKED_CAST")
                    details = (snapshot.get("details") as? Map<String, Any>) ?: emptyMap()
                    acceptedByName = snapshot.getString("acceptedByName") ?: ""
                    @Suppress("UNCHECKED_CAST")
                    progressNotes = (snapshot.get("progressNotes") as? List<Map<String, Any>>) ?: emptyList()

                    // Fetch kontak petugas
                    val acceptedBy = snapshot.getString("acceptedBy") ?: ""
                    if (acceptedBy.isNotEmpty()) {
                        firestore.collection("petugas").document(acceptedBy).get()
                            .addOnSuccessListener { doc ->
                                acceptedByKontak = doc.getString("noTelepon") ?: ""
                            }
                    }

                    loading = false
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Progress Laporan", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFFBFBFB))
            )
        },
        containerColor = Color(0xFFFBFBFB)
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0084FF))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            ProgressStepperUser(status)
            Spacer(Modifier.height(20.dp))

            SectionTitle("Informasi Identitas Laporan:")
            KVRow("Nomor Laporan", "RESQ-${reportId.take(8).uppercase()}")
            KVRow("Jenis Laporan", subJenis)
            KVRow("Judul Laporan", judul)
            KVRow("Tanggal Dilaporkan", tanggalLapor?.toDate()?.let { dateFormat.format(it) } ?: "-")

            Spacer(Modifier.height(16.dp))
            SectionTitle("Status Terkini:")
            KVRow("Status", status)
            val lastTs = progressNotes.lastOrNull()?.get("timestamp") as? Timestamp
            KVRow("Update Terakhir", lastTs?.toDate()?.let { dateFormat.format(it) }
                ?: tanggalLapor?.toDate()?.let { dateFormat.format(it) } ?: "-")

            Spacer(Modifier.height(16.dp))
            SectionTitle("Ringkasan Laporan Awal:")
            KVRow("Tanggal & Waktu Kejadian", "$tanggalKejadian $waktuKejadian")
            KVRow("Lokasi Kejadian", lokasi)
            details.forEach { (k, v) ->
                val s = v.toString()
                if (s.isNotBlank() && s != "false" && s != "0") KVRow(formatLbl(k), s)
            }
            KVRowML("Kronologi Singkat", kronologi)

            if (photos.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Text("Bukti Singkat", Modifier.width(140.dp), fontSize = 13.sp)
                    Text(":", fontSize = 13.sp)
                    Spacer(Modifier.width(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(photos) { url ->
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("Riwayat Tindak Lanjut / Timeline:")
            TLItem(tanggalLapor?.toDate()?.let { timelineFormat.format(it) } ?: "[-]",
                "Laporan berhasil dikirim dan diterima sistem")
            progressNotes.forEach { note ->
                val ts = note["timestamp"] as? Timestamp
                TLItem(ts?.toDate()?.let { timelineFormat.format(it) } ?: "[-]",
                    note["note"] as? String ?: "")
            }

            if (acceptedByName.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                SectionTitle("Kontak Petugas/Unit")
                KVRow("Ditangani Oleh", acceptedByName)
                if (acceptedByKontak.isNotEmpty()) KVRow("Kontak", acceptedByKontak)
            }

            Spacer(Modifier.height(24.dp))

            // Tombol Selesai (jika status sudah selesai, tampilkan banner hijau)
            if (status == "Selesai") {
                Box(
                    Modifier.fillMaxWidth().background(Color(0xFFE8F5E9), RoundedCornerShape(14.dp)).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50))
                        Spacer(Modifier.width(8.dp))
                        Text("Laporan Selesai Ditangani", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable private fun ProgressStepperUser(currentStatus: String) {
    val idx = when (currentStatus) {
        "Diterima" -> 0; "Diverifikasi" -> 1; "Ditindaklanjuti", "Diproses", "Sedang Diproses" -> 2; "Selesai" -> 3; else -> -1
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        USER_STAGES.forEachIndexed { i, s ->
            val active = i <= idx
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f, false)) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(if (active) Color(0xFF0084FF) else Color(0xFFB3D9FF).copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center) {
                    Text("${i + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(s, fontSize = 10.sp, color = if (active) Color(0xFF0084FF) else Color.Gray, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
            }
            if (i < USER_STAGES.size - 1) {
                Box(Modifier.weight(1f).height(2.dp).padding(bottom = 16.dp).background(if (i < idx) Color(0xFF0084FF) else Color(0xFFB3D9FF).copy(alpha = 0.5f)))
            }
        }
    }
}

@Composable private fun SectionTitle(t: String) { Text(t, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp)) }
@Composable private fun KVRow(l: String, v: String) { Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text(l, Modifier.width(140.dp), fontSize = 13.sp); Text(":", fontSize = 13.sp); Spacer(Modifier.width(8.dp)); Text(v, fontSize = 13.sp, modifier = Modifier.weight(1f)) } }
@Composable private fun KVRowML(l: String, v: String) { Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) { Text(l, Modifier.width(140.dp), fontSize = 13.sp); Text(":", fontSize = 13.sp); Spacer(Modifier.width(8.dp)); Text(v, fontSize = 13.sp, modifier = Modifier.weight(1f)) } }
@Composable private fun TLItem(ts: String, t: String) { Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) { Text(ts, Modifier.width(140.dp), fontSize = 12.sp, fontFamily = FontFamily.Monospace); Text(":", fontSize = 12.sp); Spacer(Modifier.width(8.dp)); Text(t, fontSize = 13.sp, modifier = Modifier.weight(1f)) } }
private fun formatLbl(k: String) = k.replace(Regex("([a-z])([A-Z])"), "$1 $2").replaceFirstChar { it.uppercase() }