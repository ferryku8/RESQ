package com.uxonauts.resq.controllers

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.uxonauts.resq.models.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class AuthController : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var isKtpValidating by mutableStateOf(false)

    // State Login
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    // State Navigasi
    var currentSignUpStep by mutableStateOf(1)

    // State PIN & Biometrik
    var pinCode by mutableStateOf("")
    var isBiometricEnabled by mutableStateOf(false)

    // Step 1: Biodata
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var gender by mutableStateOf("Laki-laki")
    var dateOfBirth by mutableStateOf("")
    var address by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // Step 2: KTP
    var ktpImageUri by mutableStateOf<Uri?>(null)

    // Step 3: Medis
    var height by mutableStateOf("")
    var weight by mutableStateOf("")
    var bloodType by mutableStateOf("")
    var diseaseHistory by mutableStateOf("")
    var routineMeds by mutableStateOf("")
    var allergies by mutableStateOf("")

    // Step 4: Kontak Darurat
    var ecFirstName by mutableStateOf("")
    var ecLastName by mutableStateOf("")
    var ecRelation by mutableStateOf("Keluarga")
    var ecPhone by mutableStateOf("")
    var termsAccepted by mutableStateOf(false)

    val bloodTypeOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val ecRelationOptions = listOf("Keluarga", "Pasangan", "Teman", "Lainnya")

    val savedContacts = mutableStateListOf<EmergencyContact>()

    // Validasi Step 1
    fun isStep1Valid(): Boolean {
        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        return firstName.isNotBlank() &&
                gender.isNotBlank() &&
                dateOfBirth.isNotBlank() &&
                address.isNotBlank() &&
                phone.isNotBlank() && isEmailValid &&
                password.length >= 6
    }

    // Validasi Step 2
    fun isStep2Valid(): Boolean = ktpImageUri != null && !isKtpValidating

    // Validasi Step 3
    fun isStep3Valid(): Boolean {
        return height.isNotBlank() && weight.isNotBlank() && bloodType.isNotBlank()
    }

    // Validasi Kontak Darurat
    fun isStep4ContactValid(): Boolean {
        return ecFirstName.isNotBlank() && ecRelation.isNotBlank() && ecPhone.isNotBlank()
    }

    fun isSelfNumber(): Boolean {
        val myPhone = phone.replace(Regex("[^0-9]"), "")
        val contactPhone = ecPhone.replace(Regex("[^0-9]"), "")
        return myPhone == contactPhone && myPhone.isNotEmpty()
    }

    fun updatePin(newPin: String) {
        if (newPin.length <= 6) {
            pinCode = newPin
        }
    }

    private fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun validateKtpImage(context: Context, uri: Uri) {
        isKtpValidating = true
        errorMessage = null
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text.uppercase()
                    val isValidKtp = text.contains("NIK") || text.contains("PROVINSI") ||
                            text.contains("KABUPATEN") || text.contains("KOTA") ||
                            text.contains("KARTU TANDA PENDUDUK")
                    if (isValidKtp) {
                        ktpImageUri = uri
                    } else {
                        ktpImageUri = null
                        errorMessage = "Foto tidak terdeteksi sebagai KTP yang valid."
                    }
                    isKtpValidating = false
                }
                .addOnFailureListener {
                    ktpImageUri = null
                    isKtpValidating = false
                }
        } catch (e: Exception) {
            isKtpValidating = false
        }
    }

    fun addEmergencyContact() {
        if (isStep4ContactValid() && !isSelfNumber()) {
            savedContacts.add(
                EmergencyContact(
                    contactId = UUID.randomUUID().toString(),
                    userId = "",
                    namaLengkap = "$ecFirstName $ecLastName".trim(),
                    hubungan = ecRelation,
                    noTelepon = ecPhone
                )
            )
            ecFirstName = ""
            ecLastName = ""
            ecRelation = "Keluarga"
            ecPhone = ""
        }
    }

    fun doLogin(navController: NavController) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                auth.signInWithEmailAndPassword(loginEmail, loginPassword).await()
                navController.navigate("home") { popUpTo(0) }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
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
                // 1. Buat Akun Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid ?: throw Exception("Gagal mendapatkan ID")

                // 2. Upload KTP ke Firebase Storage (AMAN: Cek null dan try-catch)
                var downloadUrl = ""
                if (ktpImageUri != null) {
                    try {
                        val storageRef = storage.reference.child("ktp_images/$uid.jpg")
                        storageRef.putFile(ktpImageUri!!).await()
                        downloadUrl = storageRef.downloadUrl.await().toString()
                    } catch (e: Exception) {
                        Log.e("UploadKTP", "Gagal upload KTP: ${e.message}")
                        // Lanjutkan pendaftaran meskipun upload gagal (opsional, bisa diubah)
                    }
                }

                // 3. Simpan Data User Utama
                val newUser = User(
                    userId = uid,
                    namaLengkap = "$firstName $lastName".trim(),
                    email = email,
                    noTelepon = phone,
                    jenisKelamin = gender,
                    alamat = address,
                    ktpImageUrl = downloadUrl,
                    tglLahir = try {
                        java.text.SimpleDateFormat("d/M/yyyy", java.util.Locale.getDefault()).parse(dateOfBirth)
                    } catch (e: Exception) { null }
                )
                db.collection("users").document(uid).set(newUser).await()

                // 4. Simpan Data Medis
                val newMedInfo = MedicalInfo(
                    userId = uid,
                    golDarah = bloodType,
                    tinggiBadan = height.toIntOrNull() ?: 0,
                    beratBadan = weight.toIntOrNull() ?: 0,
                    riwayatPenyakit = diseaseHistory,
                    obatRutin = routineMeds,
                    alergi = allergies
                )
                db.collection("medical_info").document(uid).set(newMedInfo).await()

                // 5. Simpan Kontak Darurat
                val allContactsToSave = savedContacts.toMutableList()
                if (isStep4ContactValid() && !isSelfNumber()) {
                    allContactsToSave.add(
                        EmergencyContact(
                            contactId = UUID.randomUUID().toString(),
                            userId = uid,
                            namaLengkap = "$ecFirstName $ecLastName".trim(),
                            hubungan = ecRelation,
                            noTelepon = ecPhone
                        )
                    )
                }

                for (contact in allContactsToSave) {
                    db.collection("emergency_contacts").document(contact.contactId).set(contact.copy(userId = uid)).await()
                }

                navController.navigate("pin_setup") { popUpTo("signup") { inclusive = true } }
            } catch (e: Exception) {
                errorMessage = "Gagal Pendaftaran: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun finalizeRegistration(navController: NavController) {
        viewModelScope.launch {
            isLoading = true
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val hashedPin = hashPin(pinCode)

                val updates = mapOf<String, Any>(
                    "pin" to hashedPin,
                    "biometricEnabled" to isBiometricEnabled
                )

                db.collection("users").document(uid).update(updates).await()
                navController.navigate("home") { popUpTo(0) }
            } catch (e: Exception) {
                errorMessage = "Gagal menyimpan keamanan: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}