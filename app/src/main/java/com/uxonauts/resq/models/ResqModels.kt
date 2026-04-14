package com.uxonauts.resq.models

data class User(
    val userId: String = "",
    val namaLengkap: String = "",
    val email: String = "",
    val noTelepon: String = "",
    val jenisKelamin: String = "",
    val tglLahir: java.util.Date? = null,
    val alamat: String = "",
    val ktpImageUrl: String = "",
    val profileImageUrl: String = "",  // ← TAMBAH INI
    val pin: String = ""
)

data class MedicalInfo(
    val userId: String = "",
    val golDarah: String = "",
    val tinggiBadan: Int = 0,
    val beratBadan: Int = 0,
    val riwayatPenyakit: String = "",
    val obatRutin: String = "",
    val alergi: String = ""
)

data class EmergencyContact(
    val contactId: String = "",
    val userId: String = "",
    val namaLengkap: String = "",
    val hubungan: String = "",
    val noTelepon: String = ""
)

data class SosAlert(
    val alertId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val category: String = "", // Kecelakaan, Kebakaran, Darurat Medis, dll
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val status: String = "active", // active, accepted, on_the_way, arrived, completed, cancelled
    val targetRoles: List<Int> = emptyList(), // [2,3] = polisi + medis
    val acceptedBy: String = "", // UID petugas yang menerima
    val acceptedByName: String = "",
    val acceptedByRole: Int = 0,
    val petugasLat: Double = 0.0,
    val petugasLng: Double = 0.0,
    val medicalInfo: Map<String, String> = emptyMap(),
    val timestamp: com.google.firebase.Timestamp? = null
)