package com.uxonauts.resq.views.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.uxonauts.resq.models.KategoriLaporan
import java.text.SimpleDateFormat
import java.util.Locale

data class HomeArtikel(
    val id: String = "",
    val judul: String = "",
    val konten: String = "",
    val gambarUrl: String = "",
    val tglPublish: Timestamp? = null,
    val type: String = "article"
)

data class HomeBanner(
    val id: String = "",
    val judul: String = "",
    val gambarUrl: String = "",
    val konten: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSosClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLaporanClick: () -> Unit = {},
    onLaporanCategoryClick: (String) -> Unit = {},
    onArtikelListClick: () -> Unit = {},
    onArtikelDetailClick: (String) -> Unit = {},
    onRiwayatClick: () -> Unit = {},
    onNotifikasiClick: () -> Unit = {}
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var userName by remember { mutableStateOf("Pengguna") }
    var searchQuery by remember { mutableStateOf("") }

    var articles by remember { mutableStateOf<List<HomeArtikel>>(emptyList()) }
    var banners by remember { mutableStateOf<List<HomeBanner>>(emptyList()) }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val bannerWidth = screenWidthDp - 32.dp // minus horizontal padding

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                userName = doc.getString("namaLengkap") ?: "Pengguna"
            }
    }

    LaunchedEffect(Unit) {
        firestore.collection("articles")
            .orderBy("tglPublish", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    val allDocs = snap.documents.map { doc ->
                        HomeArtikel(
                            id = doc.id,
                            judul = doc.getString("judul") ?: "",
                            konten = doc.getString("konten") ?: "",
                            gambarUrl = doc.getString("gambarUrl") ?: "",
                            tglPublish = doc.getTimestamp("tglPublish"),
                            type = doc.getString("type") ?: "article"
                        )
                    }
                    articles = allDocs.filter { it.type == "article" }
                    banners = allDocs.filter { it.type == "banner" }.map {
                        HomeBanner(it.id, it.judul, it.gambarUrl, it.konten)
                    }
                }
            }
    }
    val kategoriList = remember { KategoriLaporan.list }

    Scaffold(
        bottomBar = {
            HomeBottomBar(
                onHomeClick = {},
                onProfileClick = onProfileClick,
                selectedTab = "home"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSosClick,
                shape = CircleShape,
                containerColor = Color(0xFFF44336),
                contentColor = Color.White,
                modifier = Modifier
                    .size(80.dp)
                    .offset(y = 40.dp)
            ) {
                Text("SOS", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = Color(0xFFFBFBFB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Text("Halo,", fontSize = 14.sp, color = Color.Gray)
            Text(userName, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari artikel...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, null, tint = Color(0xFF0084FF))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0084FF),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onRiwayatClick) {
                    Icon(
                        Icons.Default.Description, "Riwayat",
                        tint = Color(0xFF0084FF),
                        modifier = Modifier.size(26.dp)
                    )
                }
                IconButton(onClick = onNotifikasiClick) {
                    Icon(
                        Icons.Outlined.Notifications, "Notifikasi",
                        tint = Color(0xFF0084FF),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            if (banners.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(banners) { banner ->
                        Card(
                            modifier = Modifier
                                .width(bannerWidth)
                                .height(180.dp)
                                .clickable { onArtikelDetailClick(banner.id) },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (banner.gambarUrl.isNotEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(banner.gambarUrl),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF0084FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            banner.judul,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Layanan Laporan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onLaporanClick) {
                    Text("Lihat Semua", color = Color(0xFF0084FF), fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            val homeKategori = listOf(
                kategoriList.find { it.id == "kejahatan" },
                kategoriList.find { it.id == "lalulintas" },
                kategoriList.find { it.id == "infrastruktur" }
                    ?: kategoriList.find { it.id == "publik" }
                    ?: kategoriList.getOrNull(2),
                kategoriList.find { it.id == "keluhan" }
                    ?: kategoriList.find { it.id == "bencana" }
                    ?: kategoriList.getOrNull(3)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                homeKategori.forEachIndexed { index, kat ->
                    if (kat != null) {
                        LaporanIconCard(
                            label = kat.nama,
                            icon = iconForKategori(kat.id),
                            color = colorForIndex(index),
                            modifier = Modifier.weight(1f)
                        ) {
                            onLaporanCategoryClick(kat.nama)
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    if (index < homeKategori.size - 1) {
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Artikel", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onArtikelListClick) {
                    Text("Lihat Semua", color = Color(0xFF0084FF), fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))

            val filteredArticles = if (searchQuery.isBlank()) articles
            else articles.filter {
                it.judul.contains(searchQuery, ignoreCase = true) ||
                        it.konten.contains(searchQuery, ignoreCase = true)
            }

            if (filteredArticles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchQuery.isBlank()) "Belum ada artikel"
                        else "Tidak ditemukan artikel untuk \"$searchQuery\"",
                        color = Color.Gray, fontSize = 13.sp
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredArticles) { artikel ->
                        HomeArtikelCard(artikel) {
                            onArtikelDetailClick(artikel.id)
                        }
                    }
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}
private fun iconForKategori(id: String): ImageVector {
    return when (id) {
        "kejahatan" -> Icons.Default.LocalPolice
        "lalulintas" -> Icons.Default.DirectionsCar
        "publik" -> Icons.Default.Business
        "siber" -> Icons.Default.Computer
        "bencana" -> Icons.Default.Cloud
        "keluhan" -> Icons.Default.Description
        "infrastruktur" -> Icons.Default.Cloud  // fallback
        else -> Icons.Default.Description
    }
}

private fun colorForIndex(index: Int): Color {
    return when (index) {
        0 -> Color(0xFF1565C0)
        1 -> Color(0xFFE65100)
        2 -> Color(0xFF2E7D32)
        3 -> Color(0xFF6A1B9A)
        else -> Color.Gray
    }
}

@Composable
private fun LaporanIconCard(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun HomeArtikelCard(artikel: HomeArtikel, onClick: () -> Unit) {
    val dateText = artikel.tglPublish?.toDate()?.let {
        SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(it)
    } ?: ""

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(280.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (artikel.gambarUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(artikel.gambarUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Image, null,
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(10.dp)
            ) {
                Text(
                    artikel.judul,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday, null,
                        modifier = Modifier.size(10.dp), tint = Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(dateText, fontSize = 10.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    artikel.konten,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun HomeBottomBar(
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    selectedTab: String = "home"
) {
    Surface(
        color = Color.White,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = if (selectedTab == "home")
                        Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    tint = Color(0xFF0084FF),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = if (selectedTab == "profile")
                        Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF0084FF),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}