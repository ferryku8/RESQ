package com.uxonauts.resq.controllers

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.uxonauts.resq.models.EmergencyContact
import com.uxonauts.resq.models.MedicalInfo
import com.uxonauts.resq.models.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileController : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage by lazy { FirebaseStorage.getInstance() }

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var user by mutableStateOf(User())
    var medicalInfo by mutableStateOf(MedicalInfo())
    var emergencyContacts by mutableStateOf<List<EmergencyContact>>(emptyList())
    var profileImageUrl by mutableStateOf("")
    var isUploadingPhoto by mutableStateOf(false)
    var biodataExpanded by mutableStateOf(false)
    var medicalExpanded by mutableStateOf(false)
    var emergencyExpanded by mutableStateOf(false)
    var editNamaLengkap by mutableStateOf("")
    var editNoTelepon by mutableStateOf("")
    var editAlamat by mutableStateOf("")
    var editJenisKelamin by mutableStateOf("")
    var editGolDarah by mutableStateOf("")
    var editTinggi by mutableStateOf("")
    var editBerat by mutableStateOf("")
    var editRiwayat by mutableStateOf("")
    var editObatRutin by mutableStateOf("")
    var editAlergi by mutableStateOf("")

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val userDoc = db.collection("users").document(uid).get().await()
                userDoc.toObject(User::class.java)?.let {
                    user = it
                    profileImageUrl = userDoc.getString("profileImageUrl") ?: ""
                    editNamaLengkap = it.namaLengkap
                    editNoTelepon = it.noTelepon
                    editAlamat = it.alamat
                    editJenisKelamin = it.jenisKelamin
                }
                val medDoc = db.collection("medical_info").document(uid).get().await()
                medDoc.toObject(MedicalInfo::class.java)?.let {
                    medicalInfo = it
                    editGolDarah = it.golDarah
                    editTinggi = it.tinggiBadan.toString()
                    editBerat = it.beratBadan.toString()
                    editRiwayat = it.riwayatPenyakit
                    editObatRutin = it.obatRutin
                    editAlergi = it.alergi
                }
                val contactsSnap = db.collection("emergency_contacts")
                    .whereEqualTo("userId", uid).get().await()
                emergencyContacts = contactsSnap.toObjects(EmergencyContact::class.java)
            } catch (e: Exception) {
                errorMessage = "Gagal memuat profil: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isUploadingPhoto = true
            errorMessage = null
            try {
                val ref = storage.reference.child("profile_images/$uid.jpg")
                ref.putFile(uri).await()
                val url = ref.downloadUrl.await().toString()
                db.collection("users").document(uid)
                    .update("profileImageUrl", url).await()
                profileImageUrl = url
                successMessage = "Foto profil berhasil diperbarui"
            } catch (e: Exception) {
                errorMessage = "Gagal upload foto: ${e.localizedMessage}"
            } finally {
                isUploadingPhoto = false
            }
        }
    }

    fun saveBiodata() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val updates = mapOf(
                    "namaLengkap" to editNamaLengkap,
                    "noTelepon" to editNoTelepon,
                    "alamat" to editAlamat,
                    "jenisKelamin" to editJenisKelamin
                )
                db.collection("users").document(uid).update(updates).await()
                user = user.copy(
                    namaLengkap = editNamaLengkap,
                    noTelepon = editNoTelepon,
                    alamat = editAlamat,
                    jenisKelamin = editJenisKelamin
                )
                successMessage = "Biodata berhasil disimpan"
            } catch (e: Exception) {
                errorMessage = "Gagal simpan: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveMedicalInfo() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val newMed = MedicalInfo(
                    userId = uid,
                    golDarah = editGolDarah,
                    tinggiBadan = editTinggi.toIntOrNull() ?: 0,
                    beratBadan = editBerat.toIntOrNull() ?: 0,
                    riwayatPenyakit = editRiwayat,
                    obatRutin = editObatRutin,
                    alergi = editAlergi
                )
                db.collection("medical_info").document(uid).set(newMed).await()
                medicalInfo = newMed
                successMessage = "Info kesehatan disimpan"
            } catch (e: Exception) {
                errorMessage = "Gagal simpan: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteEmergencyContact(contactId: String) {
        viewModelScope.launch {
            try {
                db.collection("emergency_contacts").document(contactId).delete().await()
                emergencyContacts = emergencyContacts.filter { it.contactId != contactId }
                successMessage = "Kontak dihapus"
            } catch (e: Exception) {
                errorMessage = "Gagal hapus: ${e.localizedMessage}"
            }
        }
    }

    fun addEmergencyContact(nama: String, hubungan: String, noTelepon: String) {
        val uid = auth.currentUser?.uid ?: return
        if (nama.isBlank() || noTelepon.isBlank()) {
            errorMessage = "Nama dan nomor telepon wajib diisi"
            return
        }
        viewModelScope.launch {
            try {
                val id = java.util.UUID.randomUUID().toString()
                val contact = EmergencyContact(id, uid, nama, hubungan, noTelepon)
                db.collection("emergency_contacts").document(id).set(contact).await()
                emergencyContacts = emergencyContacts + contact
                successMessage = "Kontak ditambahkan"
            } catch (e: Exception) {
                errorMessage = "Gagal tambah: ${e.localizedMessage}"
            }
        }
    }

    fun logout(navController: NavController) {
        auth.signOut()
        navController.navigate("login") { popUpTo(0) }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}