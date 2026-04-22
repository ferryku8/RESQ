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

    var step1Errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var step2Errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var step4Error by remember { mutableStateOf<String?>(null) }

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

    fun validateStep1(): Boolean {
        val errors = mutableMapOf<String, String>()

        if (controller.namaPelapor.isBlank())
            errors["nama"] = "Nama pelapor wajib diisi"
        else if (controller.namaPelapor.length < 3)
            errors["nama"] = "Nama pelapor minimal 3 karakter"

        if (controller.noTelepon.isBlank())
            errors["telepon"] = "Nomor telepon wajib diisi"
        else if (controller.noTelepon.length < 10)
            errors["telepon"] = "Nomor telepon minimal 10 digit"
        else if (!controller.noTelepon.matches(Regex("^[0-9+\\-\\s]+$")))
            errors["telepon"] = "Nomor telepon hanya boleh berisi angka"

        if (controller.email.isBlank())
            errors["email"] = "Email wajib diisi"
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(controller.email).matches())
            errors["email"] = "Format email tidak valid (contoh: nama@email.com)"

        if (controller.judulLaporan.isBlank())
            errors["judul"] = "Judul laporan wajib diisi"
        else if (controller.judulLaporan.length < 5)
            errors["judul"] = "Judul laporan minimal 5 karakter"

        if (controller.tanggalKejadian.isBlank())
            errors["tanggal"] = "Tanggal kejadian wajib dipilih"

        if (controller.waktuKejadian.isBlank())
            errors["waktu"] = "Waktu kejadian wajib dipilih"

        if (controller.lokasi.isBlank())
            errors["lokasi"] = "Lokasi kejadian wajib diisi"

        if (controller.kronologi.isBlank())
            errors["kronologi"] = "Kronologi kejadian wajib diisi"
        else if (controller.kronologi.length < 20)
            errors["kronologi"] = "Kronologi minimal 20 karakter agar lebih jelas"

        step1Errors = errors
        return errors.isEmpty()
    }

    fun validateStep2(): Boolean {
        if (!controller.subJenis.equals("Pencurian Kendaraan", ignoreCase = true)) return true
        val errors = mutableMapOf<String, String>()

        if (controller.jenisKendaraan.isBlank()) errors["jenis"] = "Jenis kendaraan wajib diisi"
        if (controller.merkKendaraan.isBlank()) errors["merk"] = "Merk kendaraan wajib diisi"
        if (controller.warna.isBlank()) errors["warna"] = "Warna kendaraan wajib diisi"
        if (controller.tnkb.isBlank()) errors["tnkb"] = "Nomor plat (TNKB) wajib diisi"

        step2Errors = errors
        return errors.isEmpty()
    }

    fun validateStep4(): Boolean {
        if (!controller.konfirmasiBenar) {
            step4Error =
                "Anda harus menyetujui pernyataan kebenaran data sebelum mengirim laporan"
            return false
        }
        step4Error = null
        return true
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Laporan", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (controller.currentStep > 1) {
                            controller.currentStep--
                            step1Errors = emptyMap()
                            step2Errors = emptyMap()
                            step4Error = null
                        } else navController.popBackStack()
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
                1 -> Step1InformasiPelapor(
                    controller, step1Errors,
                    onClearError = { key -> step1Errors = step1Errors - key },
                    onSetError = { key, msg -> step1Errors = step1Errors + (key to msg) }
                )
                2 -> Step2DetailSpesifik(controller, step2Errors) { key ->
                    step2Errors = step2Errors - key
                }
                3 -> Step3InfoTambahan(controller)
                4 -> Step4Konfirmasi(controller, step4Error) { step4Error = null }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    when (controller.currentStep) {
                        1 -> if (validateStep1()) controller.currentStep = 2
                        2 -> if (validateStep2()) controller.currentStep = 3
                        3 -> controller.currentStep = 4
                        4 -> {
                            if (validateStep4()) {
                                controller.submitReport {
                                    navController.popBackStack()
                                    navController.popBackStack()
                                }
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
                        fontSize = 16.sp, fontWeight = FontWeight.Bold
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
private fun Step1InformasiPelapor(
    controller: ReportController,
    errors: Map<String, String>,
    onClearError: (String) -> Unit,
    onSetError: (String, String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column {
        SectionTitle("Informasi Pelapor")
        ValidatedField("Nama Lengkap Pelapor *", controller.namaPelapor, errors["nama"]) {
            controller.namaPelapor = it
            onClearError("nama")
        }
        ValidatedField("Nomor Telepon Aktif *", controller.noTelepon, errors["telepon"]) {
            controller.noTelepon = it
            onClearError("telepon")
        }
        ValidatedField("Alamat Email *", controller.email, errors["email"]) {
            controller.email = it
            onClearError("email")
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("Detail Kejadian")
        LabeledReadOnlyField("Jenis Laporan", controller.subJenis)
        ValidatedField("Judul Laporan *", controller.judulLaporan, errors["judul"]) {
            controller.judulLaporan = it
            onClearError("judul")
        }

        DatePickerField(
            label = "Tanggal Kejadian *",
            value = controller.tanggalKejadian,
            error = errors["tanggal"],
            onClick = { showDatePicker = true }
        )
        TimePickerField(
            label = "Waktu Kejadian *",
            value = controller.waktuKejadian,
            error = errors["waktu"],
            onClick = { showTimePicker = true }
        )

        ValidatedField("Lokasi Kejadian *", controller.lokasi, errors["lokasi"]) {
            controller.lokasi = it
            onClearError("lokasi")
        }
        ValidatedField(
            "Kronologi Kejadian *", controller.kronologi, errors["kronologi"],
            minLines = 4
        ) {
            controller.kronologi = it
            onClearError("kronologi")
        }
    }

    // Date Picker — blokir tanggal masa depan
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = java.text.SimpleDateFormat(
                            "dd/MM/yyyy", java.util.Locale("id", "ID")
                        )
                        controller.tanggalKejadian = sdf.format(java.util.Date(millis))
                        onClearError("tanggal")

                        // Reset waktu kalau tanggal berubah ke hari ini
                        // (supaya validasi jam konsisten)
                        val today = sdf.format(java.util.Date())
                        if (controller.tanggalKejadian == today && controller.waktuKejadian.isNotBlank()) {
                            // Re-validasi waktu yang sudah dipilih
                            val now = java.util.Calendar.getInstance()
                            val currentHour = now.get(java.util.Calendar.HOUR_OF_DAY)
                            val currentMinute = now.get(java.util.Calendar.MINUTE)
                            try {
                                val timeParts = controller.waktuKejadian
                                    .replace(" WIB", "").split(":")
                                val pickedHour = timeParts[0].toInt()
                                val pickedMinute = timeParts[1].toInt()
                                if (pickedHour > currentHour ||
                                    (pickedHour == currentHour && pickedMinute > currentMinute)
                                ) {
                                    controller.waktuKejadian = ""
                                    onSetError(
                                        "waktu",
                                        "Waktu sebelumnya tidak valid untuk hari ini. Pilih ulang waktu."
                                    )
                                }
                            } catch (_: Exception) { }
                        }
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = Color(0xFF0084FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker — blokir jam masa depan kalau tanggal = hari ini
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Pilih Waktu Kejadian", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val selectedHour = timePickerState.hour
                    val selectedMinute = timePickerState.minute

                    // Cek apakah tanggal kejadian adalah HARI INI
                    val todaySdf = java.text.SimpleDateFormat(
                        "dd/MM/yyyy", java.util.Locale("id", "ID")
                    )
                    val today = todaySdf.format(java.util.Date())
                    val isToday = controller.tanggalKejadian == today

                    if (isToday) {
                        val now = java.util.Calendar.getInstance()
                        val currentHour = now.get(java.util.Calendar.HOUR_OF_DAY)
                        val currentMinute = now.get(java.util.Calendar.MINUTE)

                        if (selectedHour > currentHour ||
                            (selectedHour == currentHour && selectedMinute > currentMinute)
                        ) {
                            // Jam masa depan — tolak
                            onSetError(
                                "waktu",
                                "Waktu kejadian tidak boleh melebihi waktu saat ini (${
                                    currentHour.toString().padStart(2, '0')
                                }:${
                                    currentMinute.toString().padStart(2, '0')
                                } WIB)"
                            )
                            showTimePicker = false
                            return@TextButton
                        }
                    }

                    // Valid
                    val hour = selectedHour.toString().padStart(2, '0')
                    val minute = selectedMinute.toString().padStart(2, '0')
                    controller.waktuKejadian = "$hour:$minute WIB"
                    onClearError("waktu")
                    showTimePicker = false
                }) {
                    Text("OK", color = Color(0xFF0084FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun Step2DetailSpesifik(
    controller: ReportController,
    errors: Map<String, String>,
    onClearError: (String) -> Unit
) {
    Column {
        when (controller.subJenis.lowercase()) {
            "pencurian kendaraan" -> {
                SectionTitle("Detail Kendaraan yang Hilang")
                ValidatedField("Jenis Kendaraan *", controller.jenisKendaraan, errors["jenis"]) {
                    controller.jenisKendaraan = it
                    onClearError("jenis")
                }
                ValidatedField("Merk *", controller.merkKendaraan, errors["merk"]) {
                    controller.merkKendaraan = it
                    onClearError("merk")
                }
                LabeledField("Tipe / Model", controller.tipeModel) { controller.tipeModel = it }
                LabeledField("Tahun Pembuatan", controller.tahunPembuatan) {
                    controller.tahunPembuatan = it
                }
                ValidatedField("Warna *", controller.warna, errors["warna"]) {
                    controller.warna = it
                    onClearError("warna")
                }
                ValidatedField("TNKB (Plat Nomor) *", controller.tnkb, errors["tnkb"]) {
                    controller.tnkb = it
                    onClearError("tnkb")
                }
                LabeledField("Nomor Rangka", controller.noRangka) { controller.noRangka = it }
                LabeledField("Nomor Mesin", controller.noMesin) { controller.noMesin = it }
                LabeledField(
                    "Ciri-ciri Khusus Kendaraan", controller.ciriKhusus, minLines = 3
                ) { controller.ciriKhusus = it }
            }
            else -> {
                SectionTitle("Detail Tambahan")
                Text(
                    "Tidak ada detail spesifik untuk kategori ini. Lanjut ke langkah berikutnya.",
                    color = Color.Gray, fontSize = 13.sp
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

            Text(
                "Status BPKB:", fontSize = 13.sp, color = Color.Gray,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
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
                "Estimasi Nilai Kendaraan (Rp)", controller.estimasiNilai
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
private fun Step4Konfirmasi(
    controller: ReportController,
    error: String?,
    onClearError: () -> Unit
) {
    Column {
        SectionTitle("Ringkasan Laporan")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "Informasi Kejadian", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFF0084FF)
                )
                Spacer(Modifier.height(8.dp))
                Text(controller.judulLaporan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${controller.tanggalKejadian} • ${controller.waktuKejadian} • ${controller.lokasi}",
                    fontSize = 12.sp, color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
                Text("Kronologi:", fontSize = 12.sp, color = Color.Gray)
                Text(controller.kronologi, fontSize = 13.sp)
            }
        }

        if (controller.subJenis.equals("Pencurian Kendaraan", ignoreCase = true)) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Detail Kendaraan", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF0084FF)
                    )
                    Spacer(Modifier.height(8.dp))
                    SummaryRow(
                        "Kendaraan",
                        "${controller.jenisKendaraan} ${controller.merkKendaraan} ${controller.tipeModel}"
                    )
                    SummaryRow("Tahun", controller.tahunPembuatan)
                    SummaryRow("Warna", controller.warna)
                    SummaryRow("TNKB", controller.tnkb)
                    if (controller.noRangka.isNotBlank()) SummaryRow(
                        "No Rangka", controller.noRangka
                    )
                    if (controller.noMesin.isNotBlank()) SummaryRow(
                        "No Mesin", controller.noMesin
                    )
                    if (controller.ciriKhusus.isNotBlank()) SummaryRow(
                        "Ciri Khusus", controller.ciriKhusus
                    )
                }
            }
        }

        controller.fotoKendaraanUri?.let { uri ->
            Spacer(Modifier.height(12.dp))
            Text("Foto Bukti:", fontSize = 13.sp, color = Color.Gray)
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

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (error != null) Modifier
                        .border(1.dp, Color.Red, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                    else Modifier
                )
                .clickable {
                    controller.konfirmasiBenar = !controller.konfirmasiBenar
                    onClearError()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = controller.konfirmasiBenar,
                onCheckedChange = {
                    controller.konfirmasiBenar = it
                    onClearError()
                }
            )
            Text(
                "Saya menyatakan bahwa seluruh informasi yang saya berikan dalam laporan ini adalah benar dan dapat dipertanggungjawabkan.",
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                error, color = Color.Red, fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// === HELPER COMPOSABLES ===

@Composable
private fun SectionTitle(text: String) {
    Text(
        text, fontSize = 20.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ValidatedField(
    label: String,
    value: String,
    error: String?,
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
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF0084FF),
                errorBorderColor = Color.Red
            ),
            singleLine = minLines == 1
        )
    }
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
                Icon(
                    Icons.Default.Add, null, tint = Color(0xFF0084FF),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(modifier = Modifier.padding(vertical = 2.dp)) {
            Text("$label: ", fontSize = 13.sp, color = Color.Gray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    error: String? = null,
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
                Icon(Icons.Default.CalendarMonth, "Pilih tanggal", tint = Color(0xFF0084FF))
            },
            shape = RoundedCornerShape(10.dp),
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledContainerColor = Color.White,
                disabledBorderColor = if (error != null) Color.Red else Color(0xFFE0E0E0),
                disabledTrailingIconColor = Color(0xFF0084FF),
                disabledPlaceholderColor = Color.Gray
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerField(
    label: String,
    value: String,
    error: String? = null,
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
                Icon(Icons.Default.AccessTime, "Pilih waktu", tint = Color(0xFF0084FF))
            },
            shape = RoundedCornerShape(10.dp),
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledContainerColor = Color.White,
                disabledBorderColor = if (error != null) Color.Red else Color(0xFFE0E0E0),
                disabledTrailingIconColor = Color(0xFF0084FF),
                disabledPlaceholderColor = Color.Gray
            )
        )
    }
}