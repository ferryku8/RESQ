package com.uxonauts.resq.views.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSosClick: () -> Unit = {}) { // Tambahkan parameter onSosClick
    Scaffold(
        bottomBar = { HomeBottomBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSosClick, // Gunakan parameter onSosClick di sini
                shape = CircleShape,
                containerColor = Color(0xFFF44336), // Warna Merah SOS
                contentColor = Color.White,
                modifier = Modifier
                    .size(80.dp)
                    .offset(y = 40.dp) // Membuat tombol sedikit turun agar menimpa BottomBar
            ) {
                Text("SOS", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = Color(0xFFFBFBFB) // Sesuai warna background Resq
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            HomeTopBar()

            Spacer(modifier = Modifier.height(24.dp))

            // Banner (Placeholder untuk gambar Ambulans)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                // TODO: Ganti dengan Image asli ambulans jika gambar sudah ditambahkan ke drawable
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = "Ambulans",
                    tint = Color.Gray,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            LaporanSection()

            Spacer(modifier = Modifier.height(24.dp))
            ArtikelSection()

            Spacer(modifier = Modifier.height(80.dp)) // Ruang ekstra di bawah untuk FAB
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search", color = Color.Gray) },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color(0xFFB3D9FF))
            },
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF0084FF),
                unfocusedBorderColor = Color(0xFF0084FF),
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            imageVector = Icons.Outlined.Article,
            contentDescription = "Dokumen",
            tint = Color(0xFF0084FF),
            modifier = Modifier.size(32.dp).clickable {  }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifikasi",
            tint = Color(0xFF0084FF),
            modifier = Modifier.size(32.dp).clickable {  }
        )
    }
}

@Composable
fun LaporanSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Laporan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
        Text(
            "Lihat semua",
            fontSize = 14.sp,
            color = Color(0xFFB3D9FF),
            modifier = Modifier.clickable {  }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Ikon-ikon disesuaikan dengan yang paling mirip dengan desain Anda
        LaporanIconCard(Icons.Default.LocalPolice)
        LaporanIconCard(Icons.Default.Traffic)
        LaporanIconCard(Icons.Default.Park)
        LaporanIconCard(Icons.Default.SmartToy)
    }
}

@Composable
fun LaporanIconCard(icon: ImageVector) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .size(75.dp)
            .clickable {  }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF0084FF),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun ArtikelSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Artikel", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
        Text(
            "Lihat semua",
            fontSize = 14.sp,
            color = Color(0xFFB3D9FF),
            modifier = Modifier.clickable {  }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(3) { index ->
            ArtikelCard(index)
        }
    }
}

@Composable
fun ArtikelCard(index: Int) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(260.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Placeholder Gambar Artikel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            val judul = if (index % 2 == 0)
                "Pertolongan Pertama Cepat untuk Luka Bakar Ringan di Rumah"
            else
                "Hadapi Banjir: Langkah Persiapan Kunci Sebelum & Saat Air Naik"

            Text(
                text = judul,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF222222)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text("22 April 2025", fontSize = 10.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Luka bakar ringan sering terjadi. Ketahui langkah pendinginan dan perawatan yang tepat untuk mencegah infeksi...",
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Navigasi ke detail artikel */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0084FF)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Lihat Lebih Lanjut", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HomeBottomBar() {
    BottomAppBar(
        containerColor = Color.White,
        contentColor = Color.Gray,
        tonalElevation = 16.dp,
        modifier = Modifier.height(70.dp),
        contentPadding = PaddingValues(horizontal = 48.dp)
    ) {
        IconButton(onClick = { /* Ke Home */ }) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = Color(0xFF0084FF),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // Jarak melompati area tengah (tempat FAB)

        IconButton(onClick = { /* Ke Profil */ }) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Profile",
                tint = Color(0xFF0084FF),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}