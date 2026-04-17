package com.uxonauts.resq.views.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtikelDetailScreen(navController: NavController, articleId: String) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var judul by remember { mutableStateOf("") }
    var konten by remember { mutableStateOf("") }
    var gambarUrl by remember { mutableStateOf("") }
    var tglPublish by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(articleId) {
        firestore.collection("articles").document(articleId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    judul = doc.getString("judul") ?: ""
                    konten = doc.getString("konten") ?: ""
                    gambarUrl = doc.getString("gambarUrl") ?: ""
                    val ts = doc.getTimestamp("tglPublish")
                    tglPublish = ts?.toDate()?.let {
                        SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(it)
                    } ?: ""
                }
                loading = false
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Artikel", fontWeight = FontWeight.Bold) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Gambar besar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (gambarUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(gambarUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Image, null, tint = Color.Gray,
                            modifier = Modifier.size(64.dp))
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    judul,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null,
                        modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(Modifier.width(6.dp))
                    Text(tglPublish, fontSize = 13.sp, color = Color.Gray)
                }

                Spacer(Modifier.height(20.dp))

                // Konten artikel — justified
                Text(
                    konten,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Justify
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}