package com.uxonauts.resq.views.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.uxonauts.resq.controllers.Article
import com.uxonauts.resq.controllers.ArticleController
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSosClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLaporanClick: () -> Unit = {}
) {
    val articleController: ArticleController = viewModel()

    LaunchedEffect(Unit) {
        articleController.fetchAll()
    }

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

            BannerSection(banners = articleController.banners)

            Spacer(modifier = Modifier.height(24.dp))
            LaporanSection(onLaporanClick = onLaporanClick)

            Spacer(modifier = Modifier.height(24.dp))
            ArtikelSection(articles = articleController.articles)

            Spacer(modifier = Modifier.height(80.dp))
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
            modifier = Modifier
                .size(32.dp)
                .clickable { }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifikasi",
            tint = Color(0xFF0084FF),
            modifier = Modifier
                .size(32.dp)
                .clickable { }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerSection(banners: List<Article>) {
    if (banners.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = "Banner",
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { banners.size })

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) { page ->
            val banner = banners[page]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                if (banner.gambarUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(banner.gambarUrl),
                        contentDescription = banner.judul,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.65f)
                                ),
                                startY = 200f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        banner.judul,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (banner.konten.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            banner.konten,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (banners.size > 1) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(banners.size) { i ->
                    val selected = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (selected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) Color(0xFF0084FF)
                                else Color(0xFFB3D9FF)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun LaporanSection(onLaporanClick: () -> Unit = {}) {
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
            modifier = Modifier.clickable { onLaporanClick() }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LaporanIconCard(Icons.Default.LocalPolice, onLaporanClick)
        LaporanIconCard(Icons.Default.Traffic, onLaporanClick)
        LaporanIconCard(Icons.Default.Park, onLaporanClick)
        LaporanIconCard(Icons.Default.SmartToy, onLaporanClick)
    }
}

@Composable
fun LaporanIconCard(icon: ImageVector, onClick: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .size(75.dp)
            .clickable { onClick() }
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
fun ArtikelSection(articles: List<Article>) {
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
            modifier = Modifier.clickable { }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (articles.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Belum ada artikel",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(articles) { article ->
                ArtikelCard(article)
            }
        }
    }
}

@Composable
fun ArtikelCard(article: Article) {
    val dateText = article.tglPublish?.toDate()?.let {
        SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(it)
    } ?: "-"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(260.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (article.gambarUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(article.gambarUrl),
                        contentDescription = article.judul,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = article.judul.ifEmpty { "-" },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF222222)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(dateText, fontSize = 10.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = article.konten.ifEmpty { "Tidak ada deskripsi" },
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
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
                        Icons.Filled.Home
                    else
                        androidx.compose.material.icons.Icons.Outlined.Home,
                    contentDescription = "Home",
                    tint = Color(0xFF0084FF),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = if (selectedTab == "profile")
                        Icons.Filled.Person
                    else
                        androidx.compose.material.icons.Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF0084FF),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}