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
import com.uxonauts.resq.utils.KtpValidator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class AuthController : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage by lazy { FirebaseStorage.getInstance() }

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var currentSignUpStep by mutableStateOf(1)
    var pinCode by mutableStateOf("")
    var isBiometricEnabled by mutableStateOf(false)
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var gender by mutableStateOf("Laki-laki")
    var dateOfBirth by mutableStateOf("")
    var address by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var ktpImageUri by mutableStateOf<Uri?>(null)
    var isKtpValidating by mutableStateOf(false)
    var isKtpValid by mutableStateOf(false)
    var height by mutableStateOf("")
    var weight by mutableStateOf("")
    var bloodType by mutableStateOf("")
    var diseaseHistory by mutableStateOf("")
    var routineMeds by mutableStateOf("")
    var allergies by mutableStateOf("")
    var ecFirstName by mutableStateOf("")
    var ecLastName by mutableStateOf("")
    var ecRelation by mutableStateOf("Keluarga")
    var ecPhone by mutableStateOf("")
    var termsAccepted by mutableStateOf(false)

    val bloodTypeOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val ecRelationOptions = listOf("Keluarga", "Pasangan", "Teman", "Lainnya")

    val savedContacts = mutableStateListOf<EmergencyContact>()
    fun isStep1Valid(): Boolean {
        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        return firstName.isNotBlank() &&
                gender.isNotBlank() &&
                dateOfBirth.isNotBlank() &&
                address.isNotBlank() &&
                phone.isNotBlank() && isEmailValid &&
                password.length >= 6
    }
    fun isStep2Valid(): Boolean = ktpImageUri != null && isKtpValid && !isKtpValidating
    fun isStep3Valid(): Boolean {
        return height.isNotBlank() && weight.isNotBlank() && bloodType.isNotBlank()
    }
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

    /**
     * Validasi foto KTP menggunakan ML Kit OCR.
     * Cek apakah gambar mengandung kata kunci KTP Indonesia.
     * Kalau valid → ktpImageUri di-set, isKtpValid = true
     * Kalau tidak valid → ktpImageUri di-reset null, errorMessage tampil
     */
    fun validateKtpImage(context: Context, uri: Uri) {
        isKtpValidating = true
        isKtpValid = false
        errorMessage = null
        ktpImageUri = uri  // Set dulu untuk preview loading

        KtpValidator.validate(context, uri) { valid, message ->
            isKtpValidating = false
            if (valid) {
                isKtpValid = true
                ktpImageUri = uri
                errorMessage = null
            } else {
                isKtpValid = false
                ktpImageUri = null  // Reset karena bukan KTP
                errorMessage = message
            }
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

    fun doLogin(navController: NavController, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                auth.signInWithEmailAndPassword(loginEmail, loginPassword).await()
                onSuccess()
                navController.navigate("home") { popUpTo(0) }
            } catch (e: Exception) {
                errorMessage = "Login Gagal: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun formatRegistrationError(error: String): String {
        return when {
            error.contains("email address is already", ignoreCase = true) ->
                "Email ini sudah terdaftar. Silakan gunakan email lain atau login dengan akun yang sudah ada."

            error.contains("email address is badly", ignoreCase = true) ->
                "Format email tidak valid. Periksa kembali alamat email Anda."

            error.contains("password", ignoreCase = true) && error.contains("weak", ignoreCase = true) ->
                "Kata sandi terlalu lemah. Gunakan minimal 6 karakter dengan kombinasi huruf dan angka."

            error.contains("network", ignoreCase = true) ->
                "Tidak ada koneksi internet. Periksa jaringan Anda dan coba lagi."

            error.contains("blocked", ignoreCase = true) ||
                    error.contains("unusual activity", ignoreCase = true) ->
                "Terlalu banyak percobaan. Silakan coba lagi dalam beberapa menit."

            else -> "Pendaftaran gagal: $error"
        }
    }

    fun submitRegistration(navController: NavController) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid ?: throw Exception("Gagal mendapatkan ID")
                var downloadUrl = ""
                if (ktpImageUri != null) {
                    try {
                        val storageRef = storage.reference.child("ktp_images/$uid.jpg")
                        storageRef.putFile(ktpImageUri!!).await()
                        downloadUrl = storageRef.downloadUrl.await().toString()
                    } catch (e: StorageException) {
                        Log.e("UploadKTP", "Error Storage: ${e.errorCode} - ${e.message}")
                    } catch (e: Exception) {
                        Log.e("UploadKTP", "Gagal upload KTP umum: ${e.message}")
                    }
                }
                val newUser = User(
                    userId = uid,
                    namaLengkap = "$firstName $lastName".trim(),
                    email = email,
                    noTelepon = phone,
                    jenisKelamin = gender,
                    alamat = address,
                    ktpImageUrl = downloadUrl,
                    tglLahir = try {
                        java.text.SimpleDateFormat(
                            "d/M/yyyy",
                            java.util.Locale.getDefault()
                        ).parse(dateOfBirth)
                    } catch (e: Exception) {
                        null
                    }
                )
                db.collection("users").document(uid).set(newUser).await()
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
                    db.collection("emergency_contacts").document(contact.contactId)
                        .set(contact.copy(userId = uid)).await()
                }

                navController.navigate("pin_setup") { popUpTo("signup") { inclusive = true } }
            } catch (e: Exception) {
            errorMessage = formatRegistrationError(e.localizedMessage ?: "Terjadi kesalahan")
            Log.e("RegisterError", e.toString())
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