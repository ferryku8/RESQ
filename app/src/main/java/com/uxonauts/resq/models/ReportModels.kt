package com.uxonauts.resq.models

import com.google.firebase.Timestamp

data class Report(
    val reportId: String = "",
    val userId: String = "",
    // Informasi Pelapor
    val namaPelapor: String = "",
    val noTelepon: String = "",
    val email: String = "",
    // Kategori
    val jenisLaporan: String = "", // "Kejahatan & Keamanan", dll
    val subJenis: String = "", // "Pencurian Kendaraan", dll
    // Detail kejadian umum
    val judul: String = "",
    val kronologi: String = "",
    val lokasi: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val tanggalKejadian: String = "", // "25/04/2025"
    val waktuKejadian: String = "", // "12:26"
    // Detail spesifik per kategori (flexible)
    val details: Map<String, Any> = emptyMap(),
    // Lampiran
    val photos: List<String> = emptyList(),
    val namaSaksi: String = "",
    val kontakSaksi: String = "",
    // Routing ke petugas
    val targetRoles: List<Int> = emptyList(),
    val status: String = "Menunggu", // Menunggu, Diterima, Diproses, Selesai, Ditolak
    val acceptedBy: String = "",
    val acceptedByName: String = "",
    val tanggalLapor: Timestamp? = null
)

// Kategori utama dan sub-kategori
data class LaporanKategori(
    val id: String,
    val nama: String,
    val subKategori: List<String>
)

object KategoriLaporan {
    val list = listOf(
        LaporanKategori(
            "kejahatan",
            "Kejahatan & Keamanan",
            listOf("Pencurian Kendaraan", "Pencurian Rumah", "Gang", "Perusakan Barang", "Gangguan Keamanan", "KDRT")
        ),
        LaporanKategori(
            "lalulintas",
            "Lalu Lintas & Kendaraan",
            listOf("Kecelakaan Ringan", "Pelanggaran Lalu Lintas", "Jalan Rusak", "Lampu Lalu Lintas Mati")
        ),
        LaporanKategori(
            "publik",
            "Publik & Lingkungan",
            listOf("Sampah Menumpuk", "Pohon Tumbang", "Saluran Tersumbat", "Banjir")
        ),
        LaporanKategori(
            "siber",
            "Siber & Online",
            listOf("Penipuan Online", "Pencurian Data", "Cyberbullying", "Konten Negatif")
        ),
        LaporanKategori(
            "bencana",
            "Pasca Bencana Alam",
            listOf("Kerusakan Rumah", "Kebutuhan Logistik", "Lokasi Terisolasi")
        ),
        LaporanKategori(
            "keluhan",
            "Keluhan Layanan Publik",
            listOf("Layanan Kesehatan", "Layanan Administrasi", "Fasilitas Umum Rusak")
        )
    )
}