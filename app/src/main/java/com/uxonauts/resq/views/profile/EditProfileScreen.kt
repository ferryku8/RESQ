package com.uxonauts.resq.views.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.uxonauts.resq.controllers.ProfileController
import com.uxonauts.resq.views.ui.theme.ResqBlue
import com.uxonauts.resq.views.ui.theme.TextGray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    controller: ProfileController = viewModel()
) {
    val context = LocalContext.current
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Helper bikin file temporary untuk hasil kamera
    fun createTempImageUri(ctx: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "PROFILE_${timeStamp}_"
        val storageDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        return FileProvider.getUriForFile(
            ctx,
            "${ctx.packageName}.provider",
            image
        )
    }

    // Launcher Galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { controller.uploadProfilePhoto(it) } }

    // Launcher Kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            controller.uploadProfilePhoto(tempCameraUri!!)
        }
    }

    // Launcher izin kamera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempCameraUri = createTempImageUri(context)
            cameraLauncher.launch(tempCameraUri!!)
        } else {
            Toast.makeText(context, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }
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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profil", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Foto Profil
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .border(3.dp, ResqBlue, CircleShape)
                            .clickable { showPhotoSourceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (controller.isUploadingPhoto) {
                            CircularProgressIndicator(color = ResqBlue)
                        } else if (controller.profileImageUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(controller.profileImageUrl),
                                contentDescription = "Foto Profil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(70.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ResqBlue)
                            .clickable { showPhotoSourceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt, null,
                            tint = Color.White, modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Section Biodata
            SectionTitle("Biodata Diri")
            OutlinedTextField(
                value = controller.editNamaLengkap,
                onValueChange = { controller.editNamaLengkap = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = controller.editNoTelepon,
                onValueChange = { controller.editNoTelepon = it },
                label = { Text("No. Telepon") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = controller.editAlamat,
                onValueChange = { controller.editAlamat = it },
                label = { Text("Alamat") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { controller.saveBiodata() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue)
            ) { Text("Simpan Biodata") }

            Spacer(Modifier.height(24.dp))

            // Section Medis
            SectionTitle("Informasi Kesehatan")
            OutlinedTextField(
                value = controller.editGolDarah,
                onValueChange = { controller.editGolDarah = it },
                label = { Text("Golongan Darah") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = controller.editTinggi,
                    onValueChange = { controller.editTinggi = it },
                    label = { Text("Tinggi (cm)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = controller.editBerat,
                    onValueChange = { controller.editBerat = it },
                    label = { Text("Berat (kg)") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = controller.editRiwayat,
                onValueChange = { controller.editRiwayat = it },
                label = { Text("Riwayat Penyakit") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = controller.editObatRutin,
                onValueChange = { controller.editObatRutin = it },
                label = { Text("Obat Rutin") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = controller.editAlergi,
                onValueChange = { controller.editAlergi = it },
                label = { Text("Alergi") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { controller.saveMedicalInfo() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue)
            ) { Text("Simpan Kesehatan") }

            Spacer(Modifier.height(24.dp))

            // Section Kontak Darurat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Nomor Darurat",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { showAddContactDialog = true }) {
                    Text("+ Tambah", color = ResqBlue, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))

            if (controller.emergencyContacts.isEmpty()) {
                Text("Belum ada kontak darurat", color = TextGray, fontSize = 13.sp)
            } else {
                controller.emergencyContacts.forEach { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.namaLengkap, fontWeight = FontWeight.Bold)
                            Text(
                                "${contact.hubungan} • ${contact.noTelepon}",
                                fontSize = 12.sp, color = TextGray
                            )
                        }
                        IconButton(onClick = {
                            controller.deleteEmergencyContact(contact.contactId)
                        }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    // Dialog Tambah Kontak Darurat
    if (showAddContactDialog) {
        AddContactDialog(
            onDismiss = { showAddContactDialog = false },
            onAdd = { nama, hubungan, telepon ->
                controller.addEmergencyContact(nama, hubungan, telepon)
                showAddContactDialog = false
            }
        )
    }

    // Dialog Pilih Sumber Foto
    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("Pilih Sumber Foto") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Ambil Foto (Kamera)") },
                        leadingContent = {
                            Icon(Icons.Default.CameraAlt, null, tint = ResqBlue)
                        },
                        modifier = Modifier.clickable {
                            showPhotoSourceDialog = false
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                tempCameraUri = createTempImageUri(context)
                                cameraLauncher.launch(tempCameraUri!!)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Pilih dari Galeri") },
                        leadingContent = {
                            Icon(Icons.Default.PhotoLibrary, null, tint = ResqBlue)
                        },
                        modifier = Modifier.clickable {
                            showPhotoSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoSourceDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var hubungan by remember { mutableStateOf("Keluarga") }
    var telepon by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val hubunganOptions = listOf("Keluarga", "Pasangan", "Teman", "Lainnya")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kontak Darurat") },
        text = {
            Column {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = hubungan,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hubungan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        hubunganOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = { hubungan = opt; expanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = telepon,
                    onValueChange = { telepon = it },
                    label = { Text("No. Telepon") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(nama, hubungan, telepon) },
                enabled = nama.isNotBlank() && telepon.isNotBlank()
            ) { Text("Tambah", color = ResqBlue, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}