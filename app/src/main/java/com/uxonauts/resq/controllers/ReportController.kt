package com.uxonauts.resq.controllers

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.uxonauts.resq.models.Report
import com.uxonauts.resq.utils.CategoryRoleMapper
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ReportController : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage by lazy { FirebaseStorage.getInstance() }

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var currentStep by mutableStateOf(1) // 1-4
    var namaPelapor by mutableStateOf("")
    var noTelepon by mutableStateOf("")
    var email by mutableStateOf("")
    var jenisLaporan by mutableStateOf("") // "Kejahatan & Keamanan"
    var subJenis by mutableStateOf("") // "Pencurian Kendaraan"
    var judulLaporan by mutableStateOf("")
    var tanggalKejadian by mutableStateOf("")
    var waktuKejadian by mutableStateOf("")
    var lokasi by mutableStateOf("")
    var kronologi by mutableStateOf("")
    var jenisKendaraan by mutableStateOf("Motor")
    var merkKendaraan by mutableStateOf("")
    var tipeModel by mutableStateOf("")
    var tahunPembuatan by mutableStateOf("")
    var warna by mutableStateOf("")
    var tnkb by mutableStateOf("")
    var noRangka by mutableStateOf("")
    var noMesin by mutableStateOf("")
    var ciriKhusus by mutableStateOf("")
    var kunciIkutHilang by mutableStateOf(false)
    var stnkAda by mutableStateOf(true)
    var bpkbStatus by mutableStateOf("Ada") // Ada / Tidak / Di Leasing
    var estimasiNilai by mutableStateOf("")
    var fotoKendaraanUri by mutableStateOf<Uri?>(null)
    var fotoStnkUri by mutableStateOf<Uri?>(null)
    var fotoBuktiUri by mutableStateOf<Uri?>(null)
    var namaSaksi by mutableStateOf("")
    var kontakSaksi by mutableStateOf("")
    var konfirmasiBenar by mutableStateOf(false)
    val userReports = mutableStateListOf<Report>()

    init {
        prefillUserData()
    }

    private fun prefillUserData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    namaPelapor = doc.getString("namaLengkap") ?: ""
                    noTelepon = doc.getString("noTelepon") ?: ""
                    email = doc.getString("email") ?: ""
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setKategori(jenis: String, sub: String) {
        jenisLaporan = jenis
        subJenis = sub
        currentStep = 1
    }

    fun isStep1Valid(): Boolean {
        return namaPelapor.isNotBlank() &&
                noTelepon.isNotBlank() &&
                email.isNotBlank() &&
                judulLaporan.isNotBlank() &&
                tanggalKejadian.isNotBlank() &&
                waktuKejadian.isNotBlank() &&
                lokasi.isNotBlank() &&
                kronologi.isNotBlank()
    }

    fun isStep2Valid(): Boolean {
        return when (subJenis.lowercase()) {
            "pencurian kendaraan" -> merkKendaraan.isNotBlank() &&
                    tipeModel.isNotBlank() &&
                    tahunPembuatan.isNotBlank() &&
                    warna.isNotBlank()
            else -> true // kategori lain: step 2 opsional, langsung lolos
        }
    }

    fun isStep3Valid(): Boolean = true // opsional

    fun submitReport(onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid ?: run {
            errorMessage = "Anda harus login"
            return
        }
        if (!konfirmasiBenar) {
            errorMessage = "Harap konfirmasi kebenaran laporan"
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val reportId = UUID.randomUUID().toString()
                val photoUrls = mutableListOf<String>()
                fotoKendaraanUri?.let { uri ->
                    val url = uploadPhoto(reportId, "kendaraan", uri)
                    if (url.isNotEmpty()) photoUrls.add(url)
                }
                fotoStnkUri?.let { uri ->
                    val url = uploadPhoto(reportId, "stnk", uri)
                    if (url.isNotEmpty()) photoUrls.add(url)
                }
                fotoBuktiUri?.let { uri ->
                    val url = uploadPhoto(reportId, "bukti", uri)
                    if (url.isNotEmpty()) photoUrls.add(url)
                }
                val details = buildDetailsMap()

                val targetRoles = CategoryRoleMapper.getReportTargetRoles(subJenis)

                val report = hashMapOf(
                    "reportId" to reportId,
                    "userId" to uid,
                    "namaPelapor" to namaPelapor,
                    "noTelepon" to noTelepon,
                    "email" to email,
                    "jenisLaporan" to jenisLaporan,
                    "subJenis" to subJenis,
                    "judul" to judulLaporan,
                    "kronologi" to kronologi,
                    "lokasi" to lokasi,
                    "latitude" to 0.0,
                    "longitude" to 0.0,
                    "tanggalKejadian" to tanggalKejadian,
                    "waktuKejadian" to waktuKejadian,
                    "details" to details,
                    "photos" to photoUrls,
                    "namaSaksi" to namaSaksi,
                    "kontakSaksi" to kontakSaksi,
                    "targetRoles" to targetRoles,
                    "status" to "Menunggu",
                    "acceptedBy" to "",
                    "acceptedByName" to "",
                    "tanggalLapor" to Timestamp.now()
                )

                db.collection("reports").document(reportId).set(report).await()
                successMessage = "Laporan berhasil dikirim"
                resetForm()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Gagal kirim laporan: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun buildDetailsMap(): Map<String, Any> {
        return when (subJenis.lowercase()) {
            "pencurian kendaraan" -> mapOf(
                "jenisKendaraan" to jenisKendaraan,
                "merk" to merkKendaraan,
                "tipeModel" to tipeModel,
                "tahunPembuatan" to tahunPembuatan,
                "warna" to warna,
                "tnkb" to tnkb,
                "noRangka" to noRangka,
                "noMesin" to noMesin,
                "ciriKhusus" to ciriKhusus,
                "kunciIkutHilang" to kunciIkutHilang,
                "stnkAda" to stnkAda,
                "bpkbStatus" to bpkbStatus,
                "estimasiNilai" to estimasiNilai
            )
            else -> emptyMap()
        }
    }

    private suspend fun uploadPhoto(reportId: String, label: String, uri: Uri): String {
        return try {
            val ref = storage.reference.child("reports/$reportId/$label.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            ""
        }
    }

    fun fetchUserReports() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val snap = db.collection("reports")
                    .whereEqualTo("userId", uid)
                    .get().await()
                userReports.clear()
                for (doc in snap.documents) {
                    userReports.add(
                        Report(
                            reportId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            namaPelapor = doc.getString("namaPelapor") ?: "",
                            jenisLaporan = doc.getString("jenisLaporan") ?: "",
                            subJenis = doc.getString("subJenis") ?: "",
                            judul = doc.getString("judul") ?: "",
                            lokasi = doc.getString("lokasi") ?: "",
                            status = doc.getString("status") ?: "Menunggu",
                            tanggalLapor = doc.getTimestamp("tanggalLapor")
                        )
                    )
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
            } finally {
                isLoading = false
            }
        }
    }

    private fun resetForm() {
        currentStep = 1
        judulLaporan = ""
        tanggalKejadian = ""
        waktuKejadian = ""
        lokasi = ""
        kronologi = ""
        merkKendaraan = ""
        tipeModel = ""
        tahunPembuatan = ""
        warna = ""
        tnkb = ""
        noRangka = ""
        noMesin = ""
        ciriKhusus = ""
        kunciIkutHilang = false
        stnkAda = true
        bpkbStatus = "Ada"
        estimasiNilai = ""
        fotoKendaraanUri = null
        fotoStnkUri = null
        fotoBuktiUri = null
        namaSaksi = ""
        kontakSaksi = ""
        konfirmasiBenar = false
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}