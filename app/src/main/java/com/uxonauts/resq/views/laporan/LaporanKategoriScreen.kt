package com.uxonauts.resq.views.laporan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.uxonauts.resq.models.KategoriLaporan
import com.uxonauts.resq.models.LaporanKategori

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanKategoriScreen(
    navController: NavController,
    preselectedCategory: String = "",
    onKategoriSelected: (String, String) -> Unit
) {
    var expandedKategori by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(preselectedCategory) {
        if (preselectedCategory.isNotEmpty()) {
            val matched = KategoriLaporan.list.find { kat ->
                kat.nama.contains(preselectedCategory, ignoreCase = true) ||
                        preselectedCategory.contains(kat.nama, ignoreCase = true)
            }
            if (matched != null) {
                expandedKategori = matched.id
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Laporan", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            KategoriLaporan.list.forEach { kat ->
                KategoriAccordion(
                    kategori = kat,
                    icon = iconForKategori(kat.id),
                    expanded = expandedKategori == kat.id,
                    onToggle = {
                        expandedKategori = if (expandedKategori == kat.id) null else kat.id
                    },
                    onSubClick = { sub ->
                        onKategoriSelected(kat.nama, sub)
                    }
                )
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun KategoriAccordion(
    kategori: LaporanKategori,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSubClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = Color(0xFF0084FF), modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    kategori.nama,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp, end = 16.dp, bottom = 16.dp
                    )
                ) {
                    val chunked = kategori.subKategori.chunked(3)
                    chunked.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { sub ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF0F8FF))
                                        .clickable { onSubClick(sub) }
                                        .padding(vertical = 16.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        sub,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF0084FF),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            repeat(3 - row.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
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
        else -> Icons.Default.Description
    }
}