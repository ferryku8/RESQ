package com.uxonauts.resq.views.laporan

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.uxonauts.resq.controllers.ReportController
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanFormScreen(
    navController: NavController,
    jenisLaporan: String,
    subJenis: String,
    controller: ReportController = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        controller.setKategori(jenisLaporan, subJenis)
    }

    LaunchedEffect(controller.successMessage, controller.errorMessage) {
        controller.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            controller.clearMessages()
        }
        controller.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            controller.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Laporan", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (controller.currentStep > 1) controller.currentStep--
                        else navController.popBackStack()
                    }) {
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            StepIndicator(currentStep = controller.currentStep, totalSteps = 4)
            Spacer(Modifier.height(24.dp))

            when (controller.currentStep) {
                1 -> Step1InformasiPelapor(controller)
                2 -> Step2DetailSpesifik(controller)
                3 -> Step3InfoTambahan(controller)
                4 -> Step4Konfirmasi(controller)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    when (controller.currentStep) {
                        1 -> if (controller.isStep1Valid()) controller.currentStep = 2
                        else Toast.makeText(context, "Lengkapi semua field", Toast.LENGTH_SHORT).show()
                        2 -> if (controller.isStep2Valid()) controller.currentStep = 3
                        else Toast.makeText(context, "Lengkapi detail kendaraan", Toast.LENGTH_SHORT).show()
                        3 -> controller.currentStep = 4
                        4 -> {
                            controller.submitReport {
                                navController.popBackStack()
                                navController.popBackStack() // kembali ke home
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0084FF)),
                shape = RoundedCornerShape(12.dp),
                enabled = !controller.isLoading
            ) {
                if (controller.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        if (controller.currentStep == 4) "Kirim Laporan" else "Lanjutkan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            val active = i <= currentStep
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) Color(0xFF0084FF)
                        else Color(0xFFB3D9FF).copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$i",
                    color = if (active) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
            if (i < totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(
                            if (i < currentStep) Color(0xFF0084FF)
                            else Color(0xFFB3D9FF).copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1InformasiPelapor(controller: ReportController) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column {
        SectionTitle("Informasi Pelapor")
        LabeledField("Nama Lengkap Pelapor", controller.namaPelapor) { controller.namaPelapor = it }
        LabeledField("Nomor Telepon Aktif", controller.noTelepon) { controller.noTelepon = it }
        LabeledField("Alamat Email", controller.email) { controller.email = it }

        Spacer(Modifier.height(16.dp))
        SectionTitle("Detail Kejadian")
        LabeledReadOnlyField("Jenis Laporan", controller.subJenis)
        LabeledField("Judul Laporan", controller.judulLaporan) { controller.judulLaporan = it }

        // Date Picker Field
        DatePickerField(
            label = "Tanggal Kejadian",
            value = controller.tanggalKejadian,
            onClick = { showDatePicker = true }
        )

        // Time Picker Field
        TimePickerField(
            label = "Waktu Kejadian",
            value = controller.waktuKejadian,
            onClick = { showTimePicker = true }
        )

        LabeledField("Lokasi Kejadian", controller.lokasi) { controller.lokasi = it }
        LabeledField(
            "Kronologi Kejadian",
            controller.kronologi,
            minLines = 4
        ) { controller.kronologi = it }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = java.text.SimpleDateFormat(
                                "dd/MM/yyyy",
                                java.util.Locale("id", "ID")
                            )
                            controller.tanggalKejadian = sdf.format(java.util.Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = Color(0xFF0084FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Pilih Waktu Kejadian", fontWeight = FontWeight.Bold) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour.toString().padStart(2, '0')
                        val minute = timePickerState.minute.toString().padStart(2, '0')
                        controller.waktuKejadian = "$hour:$minute WIB"
                        showTimePicker = false
                    }
                ) {
                    Text("OK", color = Color(0xFF0084FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun Step2DetailSpesifik(controller: ReportController) {
    Column {
        // Kalau subJenis adalah Pencurian Kendaraan, tampilkan form kendaraan
        when (controller.subJenis.lowercase()) {
            "pencurian kendaraan" -> {
                SectionTitle("Detail Kendaraan yang Hilang")
                LabeledField("Jenis Kendaraan", controller.jenisKendaraan) {
                    controller.jenisKendaraan = it
                }
                LabeledField("Merk", controller.merkKendaraan) { controller.merkKendaraan = it }
                LabeledField("Tipe / Model", controller.tipeModel) { controller.tipeModel = it }
                LabeledField("Tahun Pembuatan", controller.tahunPembuatan) {
                    controller.tahunPembuatan = it
                }
                LabeledField("Warna", controller.warna) { controller.warna = it }
                LabeledField("TNKB (Plat Nomor)", controller.tnkb) { controller.tnkb = it }
                LabeledField("Nomor Rangka", controller.noRangka) { controller.noRangka = it }
                LabeledField("Nomor Mesin", controller.noMesin) { controller.noMesin = it }
                LabeledField(
                    "Ciri-ciri Khusus Kendaraan",
                    controller.ciriKhusus,
                    minLines = 3
                ) { controller.ciriKhusus = it }
            }
            else -> {
                SectionTitle("Detail Tambahan")
                Text(
                    "Tidak ada detail spesifik untuk kategori ini. Lanjut ke langkah berikutnya.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun Step3InfoTambahan(controller: ReportController) {
    val photoLauncher1 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { controller.fotoKendaraanUri = it } }

    val photoLauncher2 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { controller.fotoStnkUri = it } }

    val photoLauncher3 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { controller.fotoBuktiUri = it } }

    Column {
        SectionTitle("Informasi Tambahan & Bukti")

        if (controller.subJenis.equals("Pencurian Kendaraan", ignoreCase = true)) {
            YesNoRow(
                "Apakah Kunci Kontak Ikut Hilang?",
                controller.kunciIkutHilang
            ) { controller.kunciIkutHilang = it }
            YesNoRow(
                "Apakah STNK Ada Pada Anda?",
                controller.stnkAda
            ) { controller.stnkAda = it }

            Text("Status BPKB:", fontSize = 13.sp, color = Color.Gray,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            Row {
                listOf("Ada", "Tidak", "Di Leasing").forEach { opt ->
                    Row(
                        modifier = Modifier.clickable { controller.bpkbStatus = opt },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = controller.bpkbStatus == opt,
                            onClick = { controller.bpkbStatus = opt }
                        )
                        Text(opt)
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            LabeledField(
                "Estimasi Nilai Kendaraan (Rp)",
                controller.estimasiNilai
            ) { controller.estimasiNilai = it }
        }

        Spacer(Modifier.height(16.dp))

        PhotoUploadBox(
            label = "Unggah Foto Pendukung",
            uri = controller.fotoKendaraanUri,
            onClick = { photoLauncher1.launch("image/*") }
        )
        Spacer(Modifier.height(12.dp))
        PhotoUploadBox(
            label = "Unggah Foto/Scan Dokumen (opsional)",
            uri = controller.fotoStnkUri,
            onClick = { photoLauncher2.launch("image/*") }
        )
        Spacer(Modifier.height(12.dp))
        PhotoUploadBox(
            label = "Unggah Bukti Lain (CCTV, dll) (opsional)",
            uri = controller.fotoBuktiUri,
            onClick = { photoLauncher3.launch("image/*") }
        )

        Spacer(Modifier.height(16.dp))
        LabeledField("Nama Saksi (Jika Ada)", controller.namaSaksi) { controller.namaSaksi = it }
        LabeledField("Kontak Saksi (Jika Ada)", controller.kontakSaksi) {
            controller.kontakSaksi = it
        }
    }
}

@Composable
private fun Step4Konfirmasi(controller: ReportController) {
    Column {
        SectionTitle("Informasi Kejadian")
        Text(controller.judulLaporan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "${controller.tanggalKejadian} - ${controller.waktuKejadian} - ${controller.lokasi}",
            fontSize = 12.sp, color = Color.Gray
        )
        Spacer(Modifier.height(8.dp))
        Text("Kronologi:", fontSize = 13.sp, color = Color.Gray)
        Text(controller.kronologi, fontSize = 13.sp)

        if (controller.subJenis.equals("Pencurian Kendaraan", ignoreCase = true)) {
            Spacer(Modifier.height(16.dp))
            SectionTitle("Detail Kendaraan")
            SummaryRow("Jenis", "${controller.jenisKendaraan} - ${controller.merkKendaraan} ${controller.tipeModel}")
            SummaryRow("Tahun", controller.tahunPembuatan)
            SummaryRow("Warna", controller.warna)
            SummaryRow("TNKB", controller.tnkb)
            SummaryRow("No Rangka", controller.noRangka)
            SummaryRow("No Mesin", controller.noMesin)
            SummaryRow("Ciri Khusus", controller.ciriKhusus)
        }

        controller.fotoKendaraanUri?.let { uri ->
            Spacer(Modifier.height(16.dp))
            Text("Foto:", fontSize = 13.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { controller.konfirmasiBenar = !controller.konfirmasiBenar },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = controller.konfirmasiBenar,
                onCheckedChange = { controller.konfirmasiBenar = it }
            )
            Text(
                "Saya menyatakan bahwa seluruh informasi yang saya berikan dalam laporan ini adalah benar dan dapat dipertanggungjawabkan.",
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// Helper composables
@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    minLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF0084FF)
            )
        )
    }
}

@Composable
private fun LabeledReadOnlyField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5)
            )
        )
    }
}

@Composable
private fun YesNoRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.clickable { onChange(true) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = value, onClick = { onChange(true) })
                Text("Ya")
            }
            Spacer(Modifier.width(16.dp))
            Row(
                modifier = Modifier.clickable { onChange(false) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = !value, onClick = { onChange(false) })
                Text("Tidak")
            }
        }
    }
}

@Composable
private fun PhotoUploadBox(label: String, uri: android.net.Uri?, onClick: () -> Unit) {
    Column {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (uri != null) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Add, null, tint = Color(0xFF0084FF),
                    modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            readOnly = true,
            enabled = false,
            placeholder = { Text("dd/MM/yyyy") },
            trailingIcon = {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = "Pilih tanggal",
                    tint = Color(0xFF0084FF)
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledContainerColor = Color.White,
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledTrailingIconColor = Color(0xFF0084FF),
                disabledPlaceholderColor = Color.Gray
            )
        )
    }
}

@Composable
private fun TimePickerField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            readOnly = true,
            enabled = false,
            placeholder = { Text("HH:mm WIB") },
            trailingIcon = {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = "Pilih waktu",
                    tint = Color(0xFF0084FF)
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledContainerColor = Color.White,
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledTrailingIconColor = Color(0xFF0084FF),
                disabledPlaceholderColor = Color.Gray
            )
        )
    }
}