package com.uxonauts.resq.views.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

data class RiwayatItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val status: String,
    val timestamp: Timestamp?,
    val type: String // "report" atau "sos"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatScreen(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val firestore = remember { FirebaseFirestore.getInstance() }
    var items by remember { mutableStateOf<List<RiwayatItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf("Semua") }

    val dateFormat = remember { SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id", "ID")) }

    LaunchedEffect(uid) {
        if (uid.isEmpty()) return@LaunchedEffect
        val allItems = mutableListOf<RiwayatItem>()

        // Fetch laporan non-darurat milik user ini
        firestore.collection("reports")
            .whereEqualTo("userId", uid)
            .orderBy("tanggalLapor", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                for (doc in snap.documents) {
                    allItems.add(
                        RiwayatItem(
                            id = doc.id,
                            title = doc.getString("subJenis") ?: doc.getString("jenisLaporan") ?: "-",
                            subtitle = doc.getString("judul") ?: "-",
                            status = doc.getString("status") ?: "Menunggu",
                            timestamp = doc.getTimestamp("tanggalLapor"),
                            type = "report"
                        )
                    )
                }

                // Fetch SOS milik user ini
                firestore.collection("sos_alerts")
                    .whereEqualTo("userId", uid)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { sosSnap ->
                        for (doc in sosSnap.documents) {
                            val statusSos = doc.getString("status") ?: "active"
                            val statusLabel = when (statusSos) {
                                "active" -> "Menunggu"
                                "accepted" -> "Diterima"
                                "arrived" -> "Petugas Tiba"
                                "completed" -> "Selesai"
                                else -> statusSos
                            }
                            allItems.add(
                                RiwayatItem(
                                    id = doc.id,
                                    title = "SOS ${doc.getString("category") ?: ""}",
                                    subtitle = doc.getString("address") ?: doc.getString("location") ?: "-",
                                    status = statusLabel,
                                    timestamp = doc.getTimestamp("timestamp"),
                                    type = "sos"
                                )
                            )
                        }
                        // Sort combined by timestamp desc
                        items = allItems.sortedByDescending { it.timestamp?.seconds ?: 0 }
                        loading = false
                    }
                    .addOnFailureListener { loading = false }
            }
            .addOnFailureListener { loading = false }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Riwayat", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Tab filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFF6FF))
                    .padding(4.dp)
            ) {
                listOf("Semua", "Laporan", "SOS").forEach { tab ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == tab) Color(0xFF0084FF) else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            tab,
                            color = if (selectedTab == tab) Color.White else Color(0xFF0084FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0084FF))
                }
            } else {
                val filtered = when (selectedTab) {
                    "Laporan" -> items.filter { it.type == "report" }
                    "SOS" -> items.filter { it.type == "sos" }
                    else -> items
                }

                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada riwayat", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filtered) { item ->
                            RiwayatCard(item, dateFormat) {
                                if (item.type == "report") {
                                    navController.navigate("progress_laporan/${item.id}")
                                }
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RiwayatCard(
    item: RiwayatItem,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val isSos = item.type == "sos"
    val statusColor = when (item.status) {
        "Menunggu" -> Color(0xFFFFC107)
        "Diterima", "Diproses", "Sedang Diproses" -> Color(0xFF2196F3)
        "Selesai" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
    val dateText = item.timestamp?.toDate()?.let { dateFormat.format(it) } ?: "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSos) Color(0xFFF44336) else Color(0xFF0084FF)),
                contentAlignment = Alignment.Center
            ) {
                if (isSos) {
                    Text("SOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.Description, null, tint = Color.White,
                        modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(item.subtitle, fontSize = 12.sp, color = Color.DarkGray,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(dateText, fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(item.status, fontSize = 11.sp, color = statusColor,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}