package com.uxonauts.resq.views.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.uxonauts.resq.controllers.HomeController
import com.uxonauts.resq.views.ui.theme.*

// Data Class untuk menampung info Artikel Sementara
data class ArticleData(
    val title: String,
    val date: String,
    val excerpt: String,
    val imageUrl: String
)

val sampleArticles = listOf(
    ArticleData(
        title = "Pertolongan Pertama Cepat untuk Luka Bakar Ringan di Rumah",
        date = "22 April 2025",
        excerpt = "Luka bakar ringan sering terjadi. Ketahui langkah pendinginan dan perawatan yang tepat untuk mencegah infeksi dan...",
        imageUrl = "https://images.unsplash.com/photo-1584036561566-baf8f5f1b144?q=80&w=2832&auto=format&fit=crop"
    ),
    ArticleData(
        title = "Hadapi Banjir: Langkah Persiapan Kunci Sebelum & Saat Air Naik",
        date = "21 April 2025",
        excerpt = "Banjir bisa datang tiba-tiba, terutama di musim penghujan. Siapkan diri dan keluarga Anda dengan langkah persiapan ini...",
        imageUrl = "https://images.unsplash.com/photo-1527018601619-a508a2be00cd?q=80&w=3273&auto=format&fit=crop"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeController = viewModel()) {
    // Menggunakan Box agar Bottom Navigation bisa melayang (overlap) konten scrollable
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ResqBackground)
    ) {
        // Konten Utama yang bisa di-scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp) // Memberi jarak agar tidak tertutup bottom bar
        ) {
            TopBarSection()
            BannerSection()
            LaporanSection()
            ArtikelSection()
        }

        // Custom Bottom Navigation
        BottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onSosClick = { viewModel.triggerSos() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            placeholder = { Text("Search", color = Color.LightGray) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color.LightGray) },
            shape = RoundedCornerShape(25.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = ResqBlue,
                focusedBorderColor = ResqBlue,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(Icons.Filled.Article, contentDescription = "Dokumen", tint = ResqBlue, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Icon(Icons.Filled.Notifications, contentDescription = "Notifikasi", tint = ResqBlue, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun BannerSection() {
    Image(
        painter = rememberAsyncImagePainter("https://images.unsplash.com/photo-1585842378054-ee2e52f94ba2?q=80&w=3264&auto=format&fit=crop"), // Gambar Ambulans
        contentDescription = "Banner Ambulance",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun LaporanSection() {
    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Laporan", fontSize = 20.sp, color = TextBlack)
            Text("Lihat semua", fontSize = 14.sp, color = ResqBlue.copy(alpha = 0.5f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            val icons = listOf(
                Icons.Filled.LocalPolice,
                Icons.Filled.Traffic,
                Icons.Filled.Park,
                Icons.Filled.Computer
            )
            items(icons) { icon ->
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .clickable { /* Handle klik laporan */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = ResqBlue, modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

@Composable
fun ArtikelSection() {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Artikel", fontSize = 20.sp, color = TextBlack)
            Text("Lihat semua", fontSize = 14.sp, color = ResqBlue.copy(alpha = 0.5f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(sampleArticles) { article ->
                ArtikelCard(article)
            }
        }
    }
}

@Composable
fun ArtikelCard(article: ArticleData) {
    Card(
        modifier = Modifier.width(280.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = rememberAsyncImagePainter(article.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = article.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack,
                maxLines = 2,
                lineHeight = 18.sp,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = TextGray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(article.date, fontSize = 10.sp, color = TextGray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.excerpt,
                fontSize = 11.sp,
                color = TextGray,
                maxLines = 3,
                lineHeight = 16.sp,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Navigasi ke detail artikel */ },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue)
            ) {
                Text("Lihat Lebih Lanjut", fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(modifier: Modifier = Modifier, onSosClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp) // Total tinggi untuk menampung tombol SOS
    ) {
        // Bar Putih Melengkung
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Handle Tab Home */ }) {
                    Icon(Icons.Filled.Home, contentDescription = "Home", tint = ResqBlue, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(80.dp)) // Jarak lega untuk lingkaran SOS di tengah
                IconButton(onClick = { /* Handle Tab Profile */ }) {
                    Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = ResqBlue, modifier = Modifier.size(36.dp))
                }
            }
        }

        // Tombol SOS Melayang
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .size(90.dp)
                .shadow(4.dp, CircleShape)
                .background(ResqRed, CircleShape)
                .clickable { onSosClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("SOS", color = Color.White, fontSize = 28.sp)
        }
    }
}