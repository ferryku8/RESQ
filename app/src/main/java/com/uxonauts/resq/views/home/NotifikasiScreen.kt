package com.uxonauts.resq.views.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Locale

data class NotifItem(
    val id: String,
    val title: String,
    val date: String,
    val body: String,
    val type: String // "sos_contact" atau "report_update"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifikasiScreen(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val firestore = remember { FirebaseFirestore.getInstance() }
    var notifs by remember { mutableStateOf<List<NotifItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy - HH.mm 'WIB'", Locale("id", "ID")) }

    LaunchedEffect(uid) {
        if (uid.isEmpty()) return@LaunchedEffect
        val allNotifs = mutableListOf<NotifItem>()

        // 1. Notifikasi SOS dari kontak darurat
        firestore.collection("emergency_notifications")
            .whereEqualTo("targetUserId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                for (doc in snap.documents) {
                    val senderName = doc.getString("senderName") ?: "Seseorang"
                    val category = doc.getString("category") ?: "SOS"
                    val ts = doc.getTimestamp("timestamp")
                    val dateStr = ts?.toDate()?.let { dateFormat.format(it) } ?: ""
                    allNotifs.add(
                        NotifItem(
                            id = doc.id,
                            title = "Peringatan SOS: $senderName Butuh Bantuan",
                            date = dateStr,
                            body = "$senderName telah mengaktifkan SOS Darurat ($category). Segera buka RESQ untuk melihat lokasi dan statusnya.",
                            type = "sos_contact"
                        )
                    )
                }

                // 2. Update progress laporan milik user ini
                firestore.collection("reports")
                    .whereEqualTo("userId", uid)
                    .orderBy("tanggalLapor", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { reportSnap ->
                        for (doc in reportSnap.documents) {
                            val reportId = doc.id
                            val judulLaporan = doc.getString("judul") ?: ""
                            val subJenis = doc.getString("subJenis") ?: ""
                            val status = doc.getString("status") ?: "Menunggu"
                            val nomorLaporan = "RESQ-${reportId.take(8).uppercase()}"

                            @Suppress("UNCHECKED_CAST")
                            val progressNotes = (doc.get("progressNotes") as? List<Map<String, Any>>) ?: emptyList()

                            // Notif untuk status terima
                            if (status != "Menunggu") {
                                val tanggalLapor = doc.getTimestamp("tanggalLapor")
                                val dateStr = tanggalLapor?.toDate()?.let { dateFormat.format(it) } ?: ""
                                allNotifs.add(
                                    NotifItem(
                                        id = "${reportId}_accepted",
                                        title = "Laporan $subJenis Diterima",
                                        date = dateStr,
                                        body = "Laporan Anda $nomorLaporan telah kami terima. Anda bisa cek progressnya di Riwayat Laporan.",
                                        type = "report_update"
                                    )
                                )
                            }

                            // Notif untuk setiap progress update (kecuali yang pertama "diterima")
                            progressNotes.forEachIndexed { index, note ->
                                if (index > 0) {
                                    val noteText = note["note"] as? String ?: ""
                                    val stage = note["stage"] as? String ?: ""
                                    val ts = note["timestamp"] as? Timestamp
                                    val dateStr = ts?.toDate()?.let { dateFormat.format(it) } ?: ""
                                    allNotifs.add(
                                        NotifItem(
                                            id = "${reportId}_progress_$index",
                                            title = "Update Laporan $nomorLaporan",
                                            date = dateStr,
                                            body = "Laporan Anda mengenai \"$judulLaporan\" kini statusnya: $noteText",
                                            type = "report_update"
                                        )
                                    )
                                }
                            }
                        }

                        // Sort semua by date string (newest first) — ini simplified, idealnya pakai Timestamp
                        notifs = allNotifs
                        loading = false
                    }
                    .addOnFailureListener { loading = false }
            }
            .addOnFailureListener { loading = false }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifikasi", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFBFBFB)
                )
            )
        },
        containerColor = Color(0xFFFBFBFB)
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0084FF))
            }
        } else if (notifs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Belum ada notifikasi", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(notifs) { notif ->
                    NotifCard(notif)
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun NotifCard(notif: NotifItem) {
    val isSos = notif.type == "sos_contact"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isSos) Color(0xFFF44336) else Color(0xFF0084FF)),
                contentAlignment = Alignment.Center
            ) {
                if (isSos) {
                    Text("SOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                } else {
                    Icon(Icons.Default.Shield, null, tint = Color.White,
                        modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notif.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(notif.date, fontSize = 11.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text(
                    notif.body,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}