package com.uxonauts.resq.views.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

data class ArtikelItem(
    val id: String = "",
    val judul: String = "",
    val konten: String = "",
    val gambarUrl: String = "",
    val tglPublish: com.google.firebase.Timestamp? = null,
    val type: String = "article"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtikelListScreen(navController: NavController) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var artikelList by remember { mutableStateOf<List<ArtikelItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        firestore.collection("articles")
            .whereEqualTo("type", "article")
            .orderBy("tglPublish", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    artikelList = snap.documents.map { doc ->
                        ArtikelItem(
                            id = doc.id,
                            judul = doc.getString("judul") ?: "",
                            konten = doc.getString("konten") ?: "",
                            gambarUrl = doc.getString("gambarUrl") ?: "",
                            tglPublish = doc.getTimestamp("tglPublish"),
                            type = doc.getString("type") ?: "article"
                        )
                    }
                    loading = false
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Artikel", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
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
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(artikelList) { artikel ->
                    ArtikelGridCard(artikel) {
                        navController.navigate("artikel_detail/${artikel.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun ArtikelGridCard(artikel: ArtikelItem, onClick: () -> Unit) {
    val dateText = artikel.tglPublish?.toDate()?.let {
        SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(it)
    } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Icon(Icons.Default.Image, null, tint = Color.Gray,
                        modifier = Modifier.size(40.dp))
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    artikel.judul,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null,
                        modifier = Modifier.size(10.dp), tint = Color.Gray)
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
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0084FF)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Lihat Lebih Lanjut", fontSize = 12.sp)
                }
            }
        }
    }
}