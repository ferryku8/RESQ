package com.uxonauts.resq.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// ==========================================
// THEME & COLORS
// ==========================================
val ResqBlue = Color(0xFF0084FF)
val ResqLightBlue = Color(0xFFB3D9FF)
val TextGray = Color(0xFF666666)

// ==========================================
// CONTROLLER (MVC)
// Menggunakan ViewModel agar state bertahan saat rotasi layar
// ==========================================
class AuthController : ViewModel() {
    // State untuk Login
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    // State untuk Pendaftaran (Sign Up)
    var currentSignUpStep by mutableStateOf(1)

    // Step 1: Biodata
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var gender by mutableStateOf("Laki-laki")
    var birthDate by mutableStateOf("")
    var address by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // Step 2: KTP
    var ktpImageUri by mutableStateOf<String?>(null)

    // Step 3: Medis
    var height by mutableStateOf("")
    var weight by mutableStateOf("")
    var bloodType by mutableStateOf("AB-")
    var diseaseHistory by mutableStateOf("")
    var routineMeds by mutableStateOf("")
    var allergies by mutableStateOf("")

    // Step 4: Kontak Darurat (Simplifikasi 1 kontak untuk contoh)
    var ecFirstName by mutableStateOf("")
    var ecLastName by mutableStateOf("")
    var ecRelation by mutableStateOf("Orang Tua")
    var ecPhone by mutableStateOf("")
    var termsAccepted by mutableStateOf(false)

    // Logika Firebase (Placeholder)
    fun doLogin(navController: NavController) {
        println("Memproses login untuk: $loginEmail")
        // TODO: Panggil FirebaseAuth.instance.signInWithEmailAndPassword
        // Jika sukses: navController.navigate("home")
    }

    fun submitRegistration(navController: NavController) {
        println("Menyimpan data pendaftaran ke Firebase...")
        // TODO:
        // 1. FirebaseAuth create user
        // 2. Upload KTP ke Firebase Storage
        // 3. Simpan gabungan data ke Firestore (Users, MedicalInfo, EmergencyContact)
        // Jika sukses: navController.navigate("home")
    }
}

// ==========================================
// NAVIGATION HOST
// ==========================================
@Composable
fun ResqAuthApp() {
    val navController = rememberNavController()
    val authController: AuthController = viewModel()

    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("login") { LoginScreen(navController, authController) }
        composable("signup") { SignUpHostScreen(navController, authController) }
    }
}

// ==========================================
// VIEWS: ONBOARDING
// ==========================================
@Composable
fun OnboardingScreen(navController: NavController) {
    var currentPage by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder Gambar Ilustrasi
        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ResqLightBlue.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when(currentPage) {
                    1 -> Icons.Default.LocalHospital
                    2 -> Icons.Default.Security
                    else -> Icons.Default.ContactEmergency
                },
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = ResqBlue
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = when(currentPage) {
                1 -> "SOS Darurat Cepat"
                2 -> "Laporan Non-Darurat & Info"
                else -> "Profil & Kontak Darurat"
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when(currentPage) {
                1 -> "Tekan tombol SOS saat darurat. Lokasi & info Anda langsung terkirim ke pihak berwenang."
                2 -> "Buat laporan non-darurat seperti kehilangan atau penipuan, serta dapatkan artikel penting."
                else -> "Lengkapi data medis penting dan daftarkan kontak darurat Anda untuk penanganan cepat."
            },
            textAlign = TextAlign.Center,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (currentPage < 3) currentPage++
                else navController.navigate("login") { popUpTo("onboarding") { inclusive = true } }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (currentPage == 3) "Mulai Sekarang" else "Lanjut", fontSize = 16.sp)
        }
    }
}

// ==========================================
// VIEWS: LOGIN
// ==========================================
@Composable
fun LoginScreen(navController: NavController, controller: AuthController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo RESQ Placeholder
        Icon(Icons.Default.MedicalServices, contentDescription = "Logo", modifier = Modifier.size(80.dp), tint = ResqBlue)
        Text("RESQ", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = ResqBlue)
        Text("Rapid Emergency Support & Quick Response", fontSize = 12.sp, color = ResqBlue)

        Spacer(modifier = Modifier.height(48.dp))
        Text("Masuk", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = controller.loginEmail,
            onValueChange = { controller.loginEmail = it },
            placeholder = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = controller.loginPassword,
            onValueChange = { controller.loginPassword = it },
            placeholder = { Text("Kata Sandi") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Lupa kata sandi?",
            color = ResqBlue,
            modifier = Modifier.align(Alignment.End).clickable { /* TODO */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { controller.doLogin(navController) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Masuk", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { navController.navigate("signup") },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Daftar", fontSize = 16.sp, color = ResqBlue)
        }
    }
}

// ==========================================
// VIEWS: SIGN UP HOST & PROGRESS BAR
// ==========================================
@Composable
fun SignUpHostScreen(navController: NavController, controller: AuthController) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.clickable {
                    if (controller.currentSignUpStep > 1) controller.currentSignUpStep--
                    else navController.popBackStack()
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Daftar", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        // Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 1..4) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (i <= controller.currentSignUpStep) ResqBlue else ResqLightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$i", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                if (i < 4) {
                    Box(modifier = Modifier.weight(1f).height(4.dp).background(if (i < controller.currentSignUpStep) ResqBlue else ResqLightBlue))
                }
            }
        }

        // Dynamic Content
        Box(modifier = Modifier.weight(1f).padding(horizontal = 24.dp)) {
            when (controller.currentSignUpStep) {
                1 -> SignUpStep1(controller)
                2 -> SignUpStep2(controller)
                3 -> SignUpStep3(controller)
                4 -> SignUpStep4(controller, navController)
            }
        }
    }
}

// ==========================================
// SIGN UP - STEP 1: Biodata
// ==========================================
@Composable
fun SignUpStep1(controller: AuthController) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Biodata Diri", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Nama Lengkap")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = controller.firstName, onValueChange = { controller.firstName = it }, modifier = Modifier.weight(1f), placeholder = { Text("Nama Depan") })
                OutlinedTextField(value = controller.lastName, onValueChange = { controller.lastName = it }, modifier = Modifier.weight(1f), placeholder = { Text("Nama Belakang") })
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Jenis Kelamin")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = controller.gender == "Laki-laki", onClick = { controller.gender = "Laki-laki" })
                Text("Laki-laki")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = controller.gender == "Perempuan", onClick = { controller.gender = "Perempuan" })
                Text("Perempuan")
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.address, onValueChange = { controller.address = it }, label = { Text("Alamat") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = controller.phone, onValueChange = { controller.phone = it }, label = { Text("Nomor Telepon") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = controller.email, onValueChange = { controller.email = it }, label = { Text("Alamat Email") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = controller.password, onValueChange = { controller.password = it }, label = { Text("Kata Sandi") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { controller.currentSignUpStep = 2 },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Lanjutkan", fontSize = 16.sp) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// SIGN UP - STEP 2: KTP
// ==========================================
@Composable
fun SignUpStep2(controller: AuthController) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Verifikasi Identitas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Verifikasi KTP membantu kami memastikan setiap pengguna terdaftar secara valid. Keamanan data Anda adalah prioritas kami.",
            textAlign = TextAlign.Center, color = TextGray, fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Placeholder area KTP
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(2.dp, ResqLightBlue, RoundedCornerShape(12.dp))
                .clickable { /* TODO: Launch Image Picker */ },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CreditCard, contentDescription = "KTP", modifier = Modifier.size(64.dp), tint = ResqBlue)
                Text("Ketuk untuk Mengunggah KTP", color = ResqBlue)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { controller.currentSignUpStep = 3 },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Lanjutkan", fontSize = 16.sp) }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// SIGN UP - STEP 3: Medis
// ==========================================
@Composable
fun SignUpStep3(controller: AuthController) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Laporan Medis", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Memberikan info medis memungkinkan tim penolong memberikan tindakan terbaik.", textAlign = TextAlign.Center, color = TextGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = controller.height, onValueChange = { controller.height = it }, modifier = Modifier.weight(1f), label = { Text("Tinggi Badan (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = controller.weight, onValueChange = { controller.weight = it }, modifier = Modifier.weight(1f), label = { Text("Berat Badan (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.bloodType, onValueChange = { controller.bloodType = it }, label = { Text("Golongan Darah") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.diseaseHistory, onValueChange = { controller.diseaseHistory = it }, label = { Text("Riwayat Penyakit") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.routineMeds, onValueChange = { controller.routineMeds = it }, label = { Text("Obat-obatan Rutin") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.allergies, onValueChange = { controller.allergies = it }, label = { Text("Alergi") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { controller.currentSignUpStep = 4 },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Lanjutkan", fontSize = 16.sp) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// SIGN UP - STEP 4: Kontak Darurat
// ==========================================
@Composable
fun SignUpStep4(controller: AuthController, navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Kontak Darurat", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Nama Lengkap")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = controller.ecFirstName, onValueChange = { controller.ecFirstName = it }, modifier = Modifier.weight(1f), placeholder = { Text("Nama Depan") })
                OutlinedTextField(value = controller.ecLastName, onValueChange = { controller.ecLastName = it }, modifier = Modifier.weight(1f), placeholder = { Text("Nama Belakang") })
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.ecRelation, onValueChange = { controller.ecRelation = it }, label = { Text("Hubungan") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = controller.ecPhone, onValueChange = { controller.ecPhone = it }, label = { Text("Nomor Telepon") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

            Spacer(modifier = Modifier.height(16.dp))
            Text("+ Tambahkan Nomor Darurat", color = ResqBlue, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth().clickable { /* TODO: Add to list */ }, textAlign = TextAlign.End)

            Spacer(modifier = Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = controller.termsAccepted, onCheckedChange = { controller.termsAccepted = it })
                Text("Saya menyetujui ", fontSize = 12.sp)
                Text("Syarat dan Ketentuan", fontSize = 12.sp, color = ResqBlue)
                Text(" yang berlaku.", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { controller.submitRegistration(navController) },
                enabled = controller.termsAccepted,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResqBlue),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Mulai", fontSize = 16.sp) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}