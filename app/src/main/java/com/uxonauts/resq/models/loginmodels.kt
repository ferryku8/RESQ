package com.uxonauts.resq.models

data class User(
    val userId: String = "",
    val namaLengkap: String = "",
    val email: String = "",
    val noTelepon: String = "",
    val jenisKelamin: String = "",
    val alamat: String = "",
    val ktpImageUrl: String = "",
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