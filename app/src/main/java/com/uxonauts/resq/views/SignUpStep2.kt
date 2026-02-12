package com.uxonauts.resq.views

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CreditCard
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.uxonauts.resq.controllers.AuthController
import com.uxonauts.resq.views.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SignUpStep2(controller: AuthController) {
    val context = LocalContext.current
    var showSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Fungsi untuk membuat file sementara bagi hasil foto kamera
    fun createTempImageUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

        // Pastikan authority sama dengan applicationId di build.gradle + ".provider"
        // Anda perlu mendaftarkan provider ini di AndroidManifest.xml (lihat instruksi di bawah)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            image
        )
    }

    // Launcher Galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { controller.validateKtpImage(context, it) }
    }

    // Launcher Kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            controller.validateKtpImage(context, tempCameraUri!!)
        }
    }

    // Launcher Izin Kamera
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

    // Dialog Pilihan Sumber Gambar
    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Pilih Sumber Foto") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Ambil Foto (Kamera)") },
                        leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showSourceDialog = false
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
                        leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSourceDialog = false }) { Text("Batal") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Verifikasi Identitas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Verifikasi KTP membantu kami memastikan setiap pengguna terdaftar secara valid. Foto KTP wajib diunggah.",
            textAlign = TextAlign.Center, color = TextGray, fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        controller.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .border(2.dp, if(controller.isStep2Valid()) ResqBlue else ResqLightBlue, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = !controller.isKtpValidating) {
                    showSourceDialog = true // Munculkan dialog pilihan
                },
            contentAlignment = Alignment.Center
        ) {
            if (controller.isKtpValidating) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = ResqBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Memindai KTP...", fontSize = 12.sp, color = TextGray)
                }
            } else if (controller.ktpImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(controller.ktpImageUri),
                    contentDescription = "Preview KTP",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(64.dp), tint = ResqBlue)
                    Text("Ketuk untuk Pilih / Ambil Foto KTP", color = ResqBlue)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { controller.currentSignUpStep = 3 },
            enabled = controller.isStep2Valid(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Lanjutkan", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}