package com.uxonauts.resq.controllers

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.uxonauts.resq.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthController : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // State Loading & Error untuk UI
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // State Login
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    // State Sign Up - Navigasi & Step 1-4 (Sama seperti sebelumnya)
    var currentSignUpStep by mutableStateOf(1)
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var gender by mutableStateOf("Laki-laki")
    var address by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var ktpImageUri by mutableStateOf<String?>(null)
    var height by mutableStateOf("")
    var weight by mutableStateOf("")
    var bloodType by mutableStateOf("AB-")
    var diseaseHistory by mutableStateOf("")
    var routineMeds by mutableStateOf("")
    var allergies by mutableStateOf("")
    var ecFirstName by mutableStateOf("")
    var ecLastName by mutableStateOf("")
    var ecRelation by mutableStateOf("Orang Tua")
    var ecPhone by mutableStateOf("")
    var termsAccepted by mutableStateOf(false)

    fun doLogin(navController: NavController) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // 1. Panggil Firebase Auth
                auth.signInWithEmailAndPassword(loginEmail, loginPassword).await()

                // 2. Jika sukses, arahkan ke Home dan hapus histori backstack login
                navController.navigate("home") {
                    popUpTo(0) // Menghapus tumpukan layar agar tidak bisa di-back ke login
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Gagal masuk. Periksa kembali email dan kata sandi Anda."
            } finally {
                isLoading = false
            }
        }
    }

    fun submitRegistration(navController: NavController) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // 1. Buat Akun di Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid ?: throw Exception("Gagal mendapatkan ID Pengguna")

                // 2. Bungkus data dari UI ke dalam Data Classes (Models)
                val newUser = User(
                    userId = uid,
                    namaLengkap = "$firstName $lastName".trim(),
                    email = email,
                    noTelepon = phone,
                    jenisKelamin = gender,
                    alamat = address
                )

                val newMedInfo = MedicalInfo(
                    userId = uid,
                    golDarah = bloodType,
                    tinggiBadan = height.toIntOrNull() ?: 0,
                    beratBadan = weight.toIntOrNull() ?: 0,
                    riwayatPenyakit = diseaseHistory,
                    obatRutin = routineMeds,
                    alergi = allergies
                )

                val newContact = EmergencyContact(
                    contactId = UUID.randomUUID().toString(),
                    userId = uid,
                    namaLengkap = "$ecFirstName $ecLastName".trim(),
                    hubungan = ecRelation,
                    noTelepon = ecPhone
                )

                // 3. Simpan data-data tersebut ke Cloud Firestore
                // Menggunakan UID sebagai nama dokumen agar mudah dicari nanti
                db.collection("users").document(uid).set(newUser).await()
                db.collection("medical_info").document(uid).set(newMedInfo).await()
                db.collection("emergency_contacts").document(UUID.randomUUID().toString()).set(newContact).await()

                // 4. Arahkan ke Halaman Utama
                navController.navigate("home") { popUpTo(0) }

            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Terjadi kesalahan saat pendaftaran."
            } finally {
                isLoading = false
            }
        }
    }
}