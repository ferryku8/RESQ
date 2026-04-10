package com.uxonauts.resq.views.profile

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.uxonauts.resq.controllers.ProfileController
import com.uxonauts.resq.views.ui.theme.ResqBlue
import com.uxonauts.resq.views.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    controller: ProfileController = viewModel(),
    onSosClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var showSettingsSheet by remember { mutableStateOf(false) }

    // Refresh data setiap kali screen ini aktif (misal habis kembali dari edit)
    LaunchedEffect(Unit) {
        controller.fetchProfile()
    }

    // Toast messages
    LaunchedEffect(controller.successMessage, controller.errorMessage) {
        controller.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            controller.clearMessages()
        }
        controller.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            controller.clearMessages()
        }
    }

    Scaffold(
        bottomBar = {
            com.uxonauts.resq.views.home.HomeBottomBar(
                onHomeClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onProfileClick = {},
                selectedTab = "profile"
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
                .padding(horizontal = 24.dp)
        ) {
            // Icon Setting kanan atas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { showSettingsSheet = true }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = ResqBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Foto Profil
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .border(3.dp, ResqBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (controller.profileImageUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(controller.profileImageUrl),
                            contentDescription = "Foto Profil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp),
                            tint = Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                controller.user.namaLengkap.ifEmpty { "Pengguna" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Section Biodata Diri
            ExpandableSection(
                title = "Biodata Diri",
                expanded = controller.biodataExpanded,
                onToggle = { controller.biodataExpanded = !controller.biodataExpanded }
            ) {
                InfoRow("Nama Lengkap", controller.user.namaLengkap)
                InfoRow("Email", controller.user.email)
                InfoRow("No. Telepon", controller.user.noTelepon)
                InfoRow("Jenis Kelamin", controller.user.jenisKelamin)
                InfoRow("Alamat", controller.user.alamat)
            }

            Spacer(Modifier.height(12.dp))

            // Section Informasi Kesehatan
            ExpandableSection(
                title = "Informasi Kesehatan",
                expanded = controller.medicalExpanded,
                onToggle = { controller.medicalExpanded = !controller.medicalExpanded }
            ) {
                InfoRow(
                    "Golongan Darah",
                    controller.medicalInfo.golDarah.ifEmpty { "-" }
                )
                InfoRow(
                    "Tinggi Badan",
                    if (controller.medicalInfo.tinggiBadan > 0)
                        "${controller.medicalInfo.tinggiBadan} cm" else "-"
                )
                InfoRow(
                    "Berat Badan",
                    if (controller.medicalInfo.beratBadan > 0)
                        "${controller.medicalInfo.beratBadan} kg" else "-"
                )
                InfoRow(
                    "Riwayat Penyakit",
                    controller.medicalInfo.riwayatPenyakit.ifEmpty { "-" }
                )
                InfoRow(
                    "Obat Rutin",
                    controller.medicalInfo.obatRutin.ifEmpty { "-" }
                )
                InfoRow(
                    "Alergi",
                    controller.medicalInfo.alergi.ifEmpty { "-" }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Section Nomor Darurat
            ExpandableSection(
                title = "Nomor Darurat",
                expanded = controller.emergencyExpanded,
                onToggle = { controller.emergencyExpanded = !controller.emergencyExpanded }
            ) {
                if (controller.emergencyContacts.isEmpty()) {
                    Text(
                        "Belum ada kontak darurat",
                        color = TextGray,
                        fontSize = 13.sp
                    )
                } else {
                    controller.emergencyContacts.forEach { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = ResqBlue
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact.namaLengkap, fontWeight = FontWeight.Bold)
                                Text(
                                    "${contact.hubungan} • ${contact.noTelepon}",
                                    fontSize = 12.sp,
                                    color = TextGray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }

    // Bottom Sheet Settings
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    "Pengaturan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                Spacer(Modifier.height(8.dp))

                SettingItem(
                    icon = Icons.Default.Edit,
                    label = "Edit Profil",
                    color = ResqBlue
                ) {
                    showSettingsSheet = false
                    navController.navigate("edit_profile")
                }

                SettingItem(
                    icon = Icons.Default.Logout,
                    label = "Logout",
                    color = Color.Red
                ) {
                    showSettingsSheet = false
                    controller.logout(navController)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, fontSize = 12.sp, color = TextGray)
        Text(
            value.ifEmpty { "-" },
            fontSize = 15.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp), content = content)
            }
        }
    }
}